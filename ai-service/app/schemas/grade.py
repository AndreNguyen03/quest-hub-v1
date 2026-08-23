from pydantic import BaseModel, Field


class GradeRequest(BaseModel):
    user_id: str
    personal_task_id: str
    evidence: str = Field(..., min_length=1, description="Text or URL submitted as evidence")


class CriterionFeedback(BaseModel):
    name: str
    score: float
    feedback: str


class GradeResponse(BaseModel):
    grade_id: str
    status: str              # PASS | FAIL | NEEDS_REVISION
    score: float             # 0–100
    feedback: str            # overall feedback
    criteria: list[CriterionFeedback]
