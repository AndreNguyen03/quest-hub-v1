from fastapi import APIRouter, Depends, Query, Request
from fastapi.responses import StreamingResponse
from sqlalchemy.ext.asyncio import AsyncSession

from app.infra.db import get_db
from app.schemas.coach import CreateSessionRequest, SendMessageRequest, SessionResponse
from app.services import coach_service

router = APIRouter(prefix="/ai/coach", tags=["coach"])


@router.post("/sessions", response_model=SessionResponse)
async def create_session(req: CreateSessionRequest, db: AsyncSession = Depends(get_db)):
    return await coach_service.create_session(db, req)


@router.get("/sessions", response_model=list[SessionResponse])
async def list_sessions(
    user_id: str,
    status: str | None = Query(None),
    db: AsyncSession = Depends(get_db),
):
    return await coach_service.list_sessions(db, user_id, status)


@router.get("/sessions/{session_id}", response_model=SessionResponse)
async def get_session(session_id: str, db: AsyncSession = Depends(get_db)):
    session = await coach_service.get_session(db, session_id)
    from app.schemas.coach import SessionResponse
    return SessionResponse.model_validate(session)


@router.post("/sessions/{session_id}/messages")
async def send_message(
    session_id: str,
    req: SendMessageRequest,
    request: Request,
    db: AsyncSession = Depends(get_db),
):
    import uuid
    stream_id = str(uuid.uuid4())[:8]
    return StreamingResponse(
        coach_service.stream_message(db, session_id, req.user_id, req.content, request=request, stream_id=stream_id),
        media_type="text/event-stream",
        headers={"Cache-Control": "no-cache", "X-Accel-Buffering": "no", "X-Stream-ID": stream_id},
    )
