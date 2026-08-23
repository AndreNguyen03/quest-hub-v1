"""Unit tests for AI Coach tool executor — mock SQL results."""
import json
from datetime import date
from unittest.mock import AsyncMock, MagicMock, patch

import pytest


def make_db_with_result(rows):
    """Build a mock AsyncSession where execute() returns the given rows."""
    db = AsyncMock()
    result = MagicMock()
    result.mappings.return_value.one_or_none.return_value = rows[0] if rows else None
    result.scalars.return_value = rows
    result.__iter__ = MagicMock(return_value=iter(rows))
    db.execute = AsyncMock(return_value=result)
    return db


@pytest.mark.asyncio
async def test_get_progress_returns_percent():
    db = AsyncMock()
    row = {"completed": 3, "total": 10}
    result = MagicMock()
    result.mappings.return_value.one_or_none.return_value = row
    db.execute = AsyncMock(return_value=result)

    from app.tools.executor import execute
    raw = await execute(db, "get_progress", json.dumps({"personal_quest_id": "pq-1"}))
    data = json.loads(raw)

    assert data["completed"] == 3
    assert data["total"] == 10
    assert data["percent"] == 30


@pytest.mark.asyncio
async def test_get_progress_zero_total():
    db = AsyncMock()
    row = {"completed": 0, "total": 0}
    result = MagicMock()
    result.mappings.return_value.one_or_none.return_value = row
    db.execute = AsyncMock(return_value=result)

    from app.tools.executor import execute
    raw = await execute(db, "get_progress", json.dumps({"personal_quest_id": "pq-1"}))
    data = json.loads(raw)
    assert data["percent"] == 0


@pytest.mark.asyncio
async def test_get_achievements_returns_list():
    db = AsyncMock()
    rows = [
        MagicMock(title="First Quest", unlocked_at="2026-08-01"),
        MagicMock(title="Ten Tasks", unlocked_at="2026-08-10"),
    ]
    result = MagicMock()
    result.__iter__ = MagicMock(return_value=iter(rows))
    db.execute = AsyncMock(return_value=result)

    from app.tools.executor import execute
    raw = await execute(db, "get_achievements", json.dumps({"user_id": "u-1"}))
    data = json.loads(raw)

    assert len(data["achievements"]) == 2
    assert data["achievements"][0]["title"] == "First Quest"


@pytest.mark.asyncio
async def test_get_upcoming_tasks_returns_next_three():
    db = AsyncMock()
    rows = [
        MagicMock(title="Read docs", type="LEARN", status="ACTIVE"),
        MagicMock(title="Do exercise", type="PRACTICE", status="ACTIVE"),
        MagicMock(title="Write quiz", type="QUIZ", status="ACTIVE"),
    ]
    result = MagicMock()
    result.__iter__ = MagicMock(return_value=iter(rows))
    db.execute = AsyncMock(return_value=result)

    from app.tools.executor import execute
    raw = await execute(db, "get_upcoming_tasks", json.dumps({"personal_quest_id": "pq-1"}))
    data = json.loads(raw)

    assert len(data["upcoming_tasks"]) == 3
    assert data["upcoming_tasks"][0]["title"] == "Read docs"


@pytest.mark.asyncio
async def test_unknown_tool_returns_error():
    db = AsyncMock()
    from app.tools.executor import execute
    raw = await execute(db, "do_something_evil", json.dumps({}))
    data = json.loads(raw)
    assert "error" in data


@pytest.mark.asyncio
async def test_get_streak_consecutive_days():
    """Streak counts consecutive days from today backwards."""
    db = AsyncMock()
    today = date.today()
    rows = [
        MagicMock(day=today),
        MagicMock(day=today.replace(day=today.day - 1)),
        MagicMock(day=today.replace(day=today.day - 2)),
    ]
    result = MagicMock()
    result.__iter__ = MagicMock(return_value=iter(rows))
    db.execute = AsyncMock(return_value=result)

    from app.tools.executor import execute
    raw = await execute(db, "get_streak", json.dumps({"user_id": "u-1"}))
    data = json.loads(raw)
    assert data["streak_days"] >= 1
