"""Publishes events into the shared outbox_events table."""
import json
import uuid

from sqlalchemy import text
from sqlalchemy.ext.asyncio import AsyncSession


async def publish(db: AsyncSession, event_type: str, payload: dict) -> None:
    await db.execute(
        text(
            "INSERT INTO outbox_events (id, aggregate_type, aggregate_id, event_type, payload, status)"
            " VALUES (:id, 'ai', :agg_id, :event_type, :payload, 'PENDING')"
        ),
        {
            "id": str(uuid.uuid4()),
            "agg_id": str(uuid.uuid4()),
            "event_type": event_type,
            "payload": json.dumps(payload),
        },
    )
