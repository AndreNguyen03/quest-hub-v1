"""Unit tests for grade service — mock LLM + mock DB."""
import json
from unittest.mock import AsyncMock, MagicMock, patch

import pytest

from tests.conftest import make_llm_response


# ── Helpers ──────────────────────────────────────────────────────────────────

def make_personal_task(task_id: str = "task-1", quest_id: str = "q-1", user_id: str = "u-1"):
    pt = MagicMock()
    pt.task_id = task_id
    pt.quest_id = quest_id
    pt.user_id = user_id
    return pt


def make_task(rubric: dict | None = None):
    task = MagicMock()
    task.title = "Build a REST API"
    task.description = "Create a CRUD API with FastAPI"
    task.config = {
        "rubric": rubric or {
            "criteria": [
                {"name": "Correctness", "description": "API returns correct responses", "weight": 0.5},
                {"name": "Code quality", "description": "Clean, readable code", "weight": 0.5},
            ],
            "passThreshold": 70,
        }
    }
    return task


def make_db(pt=None, task=None, grade_count: int = 0):
    db = AsyncMock()
    db.get = AsyncMock(side_effect=lambda model, pk: pt if "PersonalTask" in str(model) else task)
    scalar = AsyncMock(return_value=grade_count)
    scalar_result = MagicMock()
    scalar_result.scalar_one = MagicMock(return_value=grade_count)
    db.execute = AsyncMock(return_value=scalar_result)
    db.add = MagicMock()
    db.flush = AsyncMock()
    db.commit = AsyncMock()
    return db


# ── Tests ─────────────────────────────────────────────────────────────────────

@pytest.mark.asyncio
async def test_grade_pass():
    """PASS result is saved and submission.graded event is published."""
    llm_json = json.dumps({
        "status": "PASS",
        "score": 85.0,
        "feedback": "Great work overall.",
        "criteria": [
            {"name": "Correctness", "score": 90, "feedback": "All endpoints work."},
            {"name": "Code quality", "score": 80, "feedback": "Well structured."},
        ],
    })

    db = make_db(pt=make_personal_task(), task=make_task())

    with (
        patch("app.infra.rate_limiter.check", new=AsyncMock(return_value=True)),
        patch("app.infra.llm.llm.chat.completions.create", new=AsyncMock(return_value=make_llm_response(llm_json))),
        patch("app.infra.outbox.publish", new=AsyncMock()) as mock_publish,
    ):
        from app.services.grade_service import grade
        result = await grade(db, "user-1", "pt-1", "https://github.com/user/project")

    assert result.status == "PASS"
    assert result.score == 85.0
    assert len(result.criteria) == 2
    mock_publish.assert_called_once()
    event_payload = mock_publish.call_args[0][2]
    assert event_payload["status"] == "PASS"
    assert event_payload["userId"] == "user-1"


@pytest.mark.asyncio
async def test_grade_fail_does_not_publish_event():
    """FAIL result is saved but submission.graded event is NOT published."""
    llm_json = json.dumps({
        "status": "FAIL",
        "score": 45.0,
        "feedback": "Needs more work.",
        "criteria": [
            {"name": "Correctness", "score": 40, "feedback": "Several endpoints broken."},
            {"name": "Code quality", "score": 50, "feedback": "Hard to read."},
        ],
    })

    db = make_db(pt=make_personal_task(), task=make_task())

    with (
        patch("app.infra.rate_limiter.check", new=AsyncMock(return_value=True)),
        patch("app.infra.llm.llm.chat.completions.create", new=AsyncMock(return_value=make_llm_response(llm_json))),
        patch("app.infra.outbox.publish", new=AsyncMock()) as mock_publish,
    ):
        from app.services.grade_service import grade
        result = await grade(db, "user-1", "pt-1", "Incomplete submission")

    assert result.status == "FAIL"
    mock_publish.assert_not_called()


@pytest.mark.asyncio
async def test_grade_rate_limit_exceeded():
    """429 raised when daily grade limit is exceeded."""
    from fastapi import HTTPException

    db = make_db()

    with patch("app.infra.rate_limiter.check", new=AsyncMock(return_value=False)):
        from app.services.grade_service import grade
        with pytest.raises(HTTPException) as exc:
            await grade(db, "user-1", "pt-1", "evidence")

    assert exc.value.status_code == 429


@pytest.mark.asyncio
async def test_grade_task_no_rubric_raises_422():
    """422 raised when task has no rubric criteria."""
    from fastapi import HTTPException

    db = make_db(pt=make_personal_task(), task=make_task(rubric={"criteria": [], "passThreshold": 70}))

    with patch("app.infra.rate_limiter.check", new=AsyncMock(return_value=True)):
        from app.services.grade_service import grade
        with pytest.raises(HTTPException) as exc:
            await grade(db, "user-1", "pt-1", "evidence")

    assert exc.value.status_code == 422


@pytest.mark.asyncio
async def test_grade_attempt_number_increments():
    """attempt_no = previous grades count + 1."""
    llm_json = json.dumps({
        "status": "PASS", "score": 80.0, "feedback": "Good.",
        "criteria": [{"name": "C", "score": 80, "feedback": "ok"}],
    })

    db = make_db(pt=make_personal_task(), task=make_task(), grade_count=2)
    saved_grades = []

    def capture_add(obj):
        saved_grades.append(obj)

    db.add = MagicMock(side_effect=capture_add)

    with (
        patch("app.infra.rate_limiter.check", new=AsyncMock(return_value=True)),
        patch("app.infra.llm.llm.chat.completions.create", new=AsyncMock(return_value=make_llm_response(llm_json))),
        patch("app.infra.outbox.publish", new=AsyncMock()) as mock_publish,
    ):
        from app.services.grade_service import grade
        await grade(db, "user-1", "pt-1", "evidence")

    grade_rows = [g for g in saved_grades if hasattr(g, "attempt_no")]
    assert grade_rows[0].attempt_no == 3  # 2 previous + 1
