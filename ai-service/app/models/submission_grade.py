import uuid
from datetime import datetime

from sqlalchemy import DateTime, Float, Integer, String, Text, func
from sqlalchemy.dialects.postgresql import JSONB, UUID
from sqlalchemy.orm import Mapped, mapped_column

from app.models.base import Base


class SubmissionGrade(Base):
    __tablename__ = "submission_grades"

    id: Mapped[str] = mapped_column(UUID(as_uuid=False), primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id: Mapped[str] = mapped_column(UUID(as_uuid=False), index=True)
    personal_task_id: Mapped[str] = mapped_column(UUID(as_uuid=False))
    quest_id: Mapped[str] = mapped_column(UUID(as_uuid=False))
    attempt_no: Mapped[int] = mapped_column(Integer, default=1)
    status: Mapped[str] = mapped_column(String)           # PASS | FAIL | NEEDS_REVISION
    score: Mapped[float] = mapped_column(Float)           # 0–100
    feedback: Mapped[str] = mapped_column(Text)
    rubric_snapshot: Mapped[dict] = mapped_column(JSONB)  # snapshot at grade time
    model: Mapped[str] = mapped_column(String)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())
