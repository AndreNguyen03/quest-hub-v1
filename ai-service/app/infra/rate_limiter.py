"""DB-based rate limiting — counts rows in the relevant table within a time window."""
from datetime import timedelta

from sqlalchemy import func, select, text
from sqlalchemy.ext.asyncio import AsyncSession


async def check(
    db: AsyncSession,
    *,
    table: str,
    user_id: str,
    window: timedelta,
    limit: int,
) -> bool:
    """Return True if user is within limit, False if exceeded."""
    result = await db.execute(
        text(
            f"SELECT COUNT(*) FROM {table}"
            " WHERE user_id = :uid AND created_at > NOW() - :window"
        ),
        {"uid": user_id, "window": window},
    )
    count = result.scalar_one()
    return count < limit
