"""Grading service — calls LLM with task rubric, stores result, publishes event."""
import json
import time
import uuid
from datetime import timedelta

from fastapi import HTTPException
from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.config import settings
from app.infra import outbox, rate_limiter
from app.infra.llm import MODEL, llm
from app.infra.observability import observe
from app.infra.safety import validate_and_sanitize_input, validate_output, SafetyResult
from app.models.submission_grade import SubmissionGrade
from app.models.task import PersonalTask, Task
from app.prompts.loader import load_prompt, get_prompt_temperature, get_prompt_response_format
from app.schemas.grade import GradeResponse, CriterionFeedback

_GRADE_PROMPT = load_prompt("grade_v1")


async def grade(db: AsyncSession, user_id: str, personal_task_id: str, evidence: str) -> GradeResponse:
    async with observe("grade_submission", user_id=user_id, model=MODEL, prompt_version=_GRADE_PROMPT.version) as ctx:
        # Rate limit check
        allowed = await rate_limiter.check(
            db,
            table="submission_grades",
            user_id=user_id,
            window=timedelta(days=1),
            limit=settings.grade_limit_per_day,
        )
        if not allowed:
            raise HTTPException(status_code=429, detail=f"Grade limit {settings.grade_limit_per_day}/day reached")

        # Load personal_task → task (with rubric)
        pt = await db.get(PersonalTask, personal_task_id)
        if not pt:
            raise HTTPException(status_code=404, detail="Personal task not found")

        task = await db.get(Task, pt.task_id)
        if not task:
            raise HTTPException(status_code=404, detail="Task not found")

        rubric = task.config.get("rubric", {})
        criteria = rubric.get("criteria", [])
        pass_threshold = rubric.get("passThreshold", 60)

        if not criteria:
            raise HTTPException(status_code=422, detail="Task has no grading rubric")

        # Count previous attempts
        attempt_result = await db.execute(
            select(func.count()).where(SubmissionGrade.personal_task_id == personal_task_id)
        )
        attempt_no = (attempt_result.scalar_one() or 0) + 1

        # Safety: validate and sanitize evidence input
        safety_result = validate_and_sanitize_input(
            evidence,
            operation="grade",
            max_length=50000,
            redact_pii=True,
        )
        if not safety_result.safe:
            # Log violations but don't block - just sanitize
            from app.infra.observability import logger
            logger.warning("grade_input_safety_violations", user_id=user_id, violations=safety_result.violations)
        evidence_to_use = safety_result.sanitized_input or evidence

        # Call LLM
        prompt = _GRADE_PROMPT.format(
            task_title=task.title,
            task_description=task.description or "",
            rubric_json=json.dumps(criteria, ensure_ascii=False, indent=2),
            pass_threshold=pass_threshold,
            evidence=evidence_to_use,
        )

        llm_start = time.perf_counter()
        response = await llm.chat.completions.create(
            model=MODEL,
            messages=[{"role": "user", "content": prompt}],
            response_format={"type": get_prompt_response_format("grade_v1")},
            temperature=get_prompt_temperature("grade_v1"),
        )
        llm_ms = (time.perf_counter() - llm_start) * 1000

        # Record token usage
        usage = response.usage
        if usage:
            ctx.record_llm_call(
                model=MODEL,
                prompt_tokens=usage.prompt_tokens,
                completion_tokens=usage.completion_tokens,
                latency_ms=llm_ms,
            )

        raw = response.choices[0].message.content

        # Safety: validate output
        output_safety = validate_output(raw, operation="grade")
        if not output_safety.safe:
            from app.infra.observability import logger
            logger.warning("grade_output_safety_violations", user_id=user_id, violations=output_safety.violations)

        try:
            result = json.loads(raw)
        except json.JSONDecodeError:
            raise HTTPException(status_code=502, detail="LLM returned invalid JSON")

        status = result.get("status", "FAIL")
        score = float(result.get("score", 0))
        feedback = result.get("feedback", "")
        criteria_feedback = [CriterionFeedback(**c) for c in result.get("criteria", [])]

        # Persist grade with rubric snapshot
        grade_id = str(uuid.uuid4())
        grade_row = SubmissionGrade(
            id=grade_id,
            user_id=user_id,
            personal_task_id=personal_task_id,
            quest_id=pt.quest_id,
            attempt_no=attempt_no,
            status=status,
            score=score,
            feedback=feedback,
            rubric_snapshot=rubric,
            model=MODEL,
        )
        db.add(grade_row)

        # Publish event if PASS so Java monolith can complete the task
        if status == "PASS":
            await outbox.publish(db, "submission.graded", {
                "gradeId": grade_id,
                "userId": user_id,
                "personalTaskId": personal_task_id,
                "questId": pt.quest_id,
                "status": status,
                "score": score,
                "feedback": feedback,
                "gradedAt": grade_row.created_at.isoformat() if grade_row.created_at else None,
            })

        await db.commit()

        return GradeResponse(
            grade_id=grade_id,
            status=status,
            score=score,
            feedback=feedback,
            criteria=criteria_feedback,
        )
