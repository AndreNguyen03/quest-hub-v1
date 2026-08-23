"""Quest generation — LLM generates structure, then calls Java monolith API."""
import json
import time
from datetime import timedelta

import httpx
from fastapi import HTTPException
from sqlalchemy.ext.asyncio import AsyncSession

from app.config import settings
from app.infra import rate_limiter
from app.infra.llm import MODEL, llm
from app.infra.observability import observe
from app.infra.safety import validate_and_sanitize_input, validate_output
from app.prompts.loader import load_prompt, get_prompt_temperature, get_prompt_response_format

_GENERATE_PROMPT = load_prompt("generate_quest_v1")


async def generate_quest(db: AsyncSession, user_id: str, goal: str, domain_id: str) -> dict:
    async with observe("generate_quest", user_id=user_id, model=MODEL, prompt_version=_GENERATE_PROMPT.version) as ctx:
        allowed = await rate_limiter.check(
            db,
            table="submission_grades",
            user_id=user_id,
            window=timedelta(days=1),
            limit=settings.generate_limit_per_day,
        )
        if not allowed:
            raise HTTPException(status_code=429, detail=f"Generate limit {settings.generate_limit_per_day}/day reached")

        # Safety: validate and sanitize goal input
        safety_result = validate_and_sanitize_input(
            goal,
            operation="generate",
            max_length=1000,
            redact_pii=True,
        )
        if not safety_result.safe:
            from app.infra.observability import logger
            logger.warning("generate_input_safety_violations", user_id=user_id, violations=safety_result.violations)
        goal_to_use = safety_result.sanitized_input or goal

        # Generate quest structure with LLM
        llm_start = time.perf_counter()
        response = await llm.chat.completions.create(
            model=MODEL,
            messages=[{"role": "user", "content": _GENERATE_PROMPT.format(goal=goal_to_use, domain_id=domain_id)}],
            response_format={"type": get_prompt_response_format("generate_quest_v1")},
            temperature=get_prompt_temperature("generate_quest_v1"),
        )
        llm_ms = (time.perf_counter() - llm_start) * 1000

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
        output_safety = validate_output(raw, operation="generate")
        if not output_safety.safe:
            from app.infra.observability import logger
            logger.warning("generate_output_safety_violations", user_id=user_id, violations=output_safety.violations)

        try:
            quest_data = json.loads(raw)
        except (json.JSONDecodeError, KeyError):
            raise HTTPException(status_code=502, detail="LLM returned invalid quest structure")

        # Call Java monolith to create the quest as DRAFT
        http_start = time.perf_counter()
        async with httpx.AsyncClient(base_url=settings.monolith_base_url, timeout=30) as client:
            resp = await client.post(
                "/api/v1/quests",
                json={
                    "creatorId": user_id,
                    "domainId": domain_id,
                    "title": quest_data["title"],
                    "description": quest_data.get("description", ""),
                    "difficulty": quest_data.get("difficulty", "BEGINNER"),
                    "chapters": quest_data.get("chapters", []),
                    "aiGenerated": True,
                },
            )
        ctx.record_db_call((time.perf_counter() - http_start) * 1000)

        if resp.status_code not in (200, 201):
            raise HTTPException(status_code=502, detail=f"Monolith rejected quest: {resp.text}")

        created = resp.json()
        return {
            "quest_id": created.get("id"),
            "title": quest_data["title"],
            "status": "DRAFT",
            "message": "Quest generated and saved as draft. Review and publish when ready.",
        }
