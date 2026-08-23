from fastapi import APIRouter, Depends
from pydantic import BaseModel
from sqlalchemy.ext.asyncio import AsyncSession

from app.infra.db import get_db
from app.services import recommend_service, generate_service

router = APIRouter(prefix="/ai", tags=["recommend", "generate"])


class RecommendRequest(BaseModel):
    user_id: str
    goal: str


class GenerateRequest(BaseModel):
    user_id: str
    goal: str
    domain_id: str


@router.post("/recommend")
async def recommend(req: RecommendRequest, db: AsyncSession = Depends(get_db)):
    return await recommend_service.recommend(db, req.user_id, req.goal)


@router.post("/generate-quest")
async def generate_quest(req: GenerateRequest, db: AsyncSession = Depends(get_db)):
    return await generate_service.generate_quest(db, req.user_id, req.goal, req.domain_id)
