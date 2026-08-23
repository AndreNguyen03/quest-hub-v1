"""Read-only ORM models for quest/task data owned by the Java monolith."""
from sqlalchemy import String, Text
from sqlalchemy.dialects.postgresql import JSONB, UUID
from sqlalchemy.orm import Mapped, mapped_column

from app.models.base import Base


class Task(Base):
    """task row — read rubric from config JSONB."""
    __tablename__ = "tasks"

    id: Mapped[str] = mapped_column(UUID(as_uuid=False), primary_key=True)
    title: Mapped[str] = mapped_column(String)
    type: Mapped[str] = mapped_column(String)        # LEARN | QUIZ | SUBMISSION | PRACTICE | REFLECTION
    config: Mapped[dict] = mapped_column(JSONB)      # contains rubric for SUBMISSION/PRACTICE
    description: Mapped[str | None] = mapped_column(Text, nullable=True)


class PersonalTask(Base):
    """personal_tasks row — tracks learner's task instance."""
    __tablename__ = "personal_tasks"

    id: Mapped[str] = mapped_column(UUID(as_uuid=False), primary_key=True)
    personal_quest_id: Mapped[str] = mapped_column(UUID(as_uuid=False))
    task_id: Mapped[str] = mapped_column(UUID(as_uuid=False))
    user_id: Mapped[str] = mapped_column(UUID(as_uuid=False))
    quest_id: Mapped[str] = mapped_column(UUID(as_uuid=False))
    status: Mapped[str] = mapped_column(String)
