from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.infra.db import get_db
from app.schemas.grade import GradeRequest, GradeResponse
from app.services import grade_service

router = APIRouter(prefix="/ai", tags=["grading"])


@router.post("/grade", response_model=GradeResponse)
async def grade(req: GradeRequest, db: AsyncSession = Depends(get_db)):
    return await grade_service.grade(db, req.user_id, req.personal_task_id, req.evidence)
