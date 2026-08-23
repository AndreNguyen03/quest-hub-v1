"""Tool executor — dispatches tool calls to read-only SQL queries."""
import json
from datetime import date, timedelta

from sqlalchemy import func, select, text
from sqlalchemy.ext.asyncio import AsyncSession


async def execute(db: AsyncSession, tool_name: str, arguments: str) -> str:
    args = json.loads(arguments)
    match tool_name:
        case "get_progress":
            return await _get_progress(db, args["personal_quest_id"])
        case "get_streak":
            return await _get_streak(db, args["user_id"])
        case "get_achievements":
            return await _get_achievements(db, args["user_id"])
        case "get_upcoming_tasks":
            return await _get_upcoming_tasks(db, args["personal_quest_id"])
        case _:
            return json.dumps({"error": f"unknown tool: {tool_name}"})


async def _get_progress(db: AsyncSession, personal_quest_id: str) -> str:
    result = await db.execute(
        text(
            "SELECT"
            "  COUNT(*) FILTER (WHERE status = 'COMPLETED') AS completed,"
            "  COUNT(*) AS total"
            " FROM personal_tasks"
            " WHERE personal_quest_id = :pqid"
        ),
        {"pqid": personal_quest_id},
    )
    row = result.mappings().one_or_none()
    if not row:
        return json.dumps({"error": "quest not found"})
    completed, total = row["completed"], row["total"]
    pct = round(completed / total * 100) if total else 0
    return json.dumps({"completed": completed, "total": total, "percent": pct})


async def _get_streak(db: AsyncSession, user_id: str) -> str:
    # Streak = consecutive days (today backwards) with at least one task completed
    result = await db.execute(
        text(
            "SELECT DATE(created_at) AS day"
            " FROM task_completions"
            " WHERE user_id = :uid"
            " GROUP BY day"
            " ORDER BY day DESC"
        ),
        {"uid": user_id},
    )
    days = [row.day for row in result]

    streak = 0
    expected = date.today()
    for day in days:
        if day == expected or day == expected - timedelta(days=1) and streak == 0:
            streak += 1
            expected = day - timedelta(days=1)
        else:
            break

    return json.dumps({"streak_days": streak})


async def _get_achievements(db: AsyncSession, user_id: str) -> str:
    result = await db.execute(
        text(
            "SELECT a.title, ua.unlocked_at"
            " FROM user_achievements ua"
            " JOIN achievements a ON a.id = ua.achievement_id"
            " WHERE ua.user_id = :uid"
            " ORDER BY ua.unlocked_at DESC"
        ),
        {"uid": user_id},
    )
    items = [{"title": r.title, "unlocked_at": str(r.unlocked_at)} for r in result]
    return json.dumps({"achievements": items})


async def _get_upcoming_tasks(db: AsyncSession, personal_quest_id: str) -> str:
    result = await db.execute(
        text(
            "SELECT t.title, t.type, pt.status"
            " FROM personal_tasks pt"
            " JOIN tasks t ON t.id = pt.task_id"
            " WHERE pt.personal_quest_id = :pqid AND pt.status != 'COMPLETED'"
            " ORDER BY pt.position"
            " LIMIT 3"
        ),
        {"pqid": personal_quest_id},
    )
    items = [{"title": r.title, "type": r.type, "status": r.status} for r in result]
    return json.dumps({"upcoming_tasks": items})
