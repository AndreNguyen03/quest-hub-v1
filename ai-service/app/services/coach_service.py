"""AI Coach service — tool-use loop + SSE streaming (token-level with cancellation)."""
import asyncio
import json
import time
import uuid
from collections.abc import AsyncGenerator
from datetime import timedelta
from typing import Optional

from fastapi import HTTPException, Request
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.config import settings
from app.infra import rate_limiter
from app.infra.llm import MODEL, llm
from app.infra.observability import observe
from app.infra.safety import validate_and_sanitize_input
from app.models.coach import CoachMessage, CoachSession
from app.prompts.loader import load_prompt, get_prompt_temperature
from app.schemas.coach import CreateSessionRequest, SessionResponse
from app.tools.definitions import COACH_TOOLS
from app.tools.executor import execute as execute_tool

_SYSTEM_PROMPT = load_prompt("coach_system_v1").template.safe_substitute()

# Track active streams for cancellation
_active_streams: dict[str, asyncio.Event] = {}


def _register_stream(stream_id: str) -> asyncio.Event:
    """Register a new stream and return its cancellation event."""
    event = asyncio.Event()
    _active_streams[stream_id] = event
    return event


def _unregister_stream(stream_id: str) -> None:
    """Unregister a stream."""
    _active_streams.pop(stream_id, None)


def cancel_stream(stream_id: str) -> bool:
    """Cancel a stream by ID. Returns True if stream was found and cancelled."""
    event = _active_streams.get(stream_id)
    if event:
        event.set()
        return True
    return False


# ── Session CRUD ──────────────────────────────────────────────────────────────

async def create_session(db: AsyncSession, req: CreateSessionRequest) -> SessionResponse:
    allowed = await rate_limiter.check(
        db,
        table="coach_sessions",
        user_id=req.user_id,
        window=timedelta(days=1),
        limit=settings.coach_session_limit_per_day,
    )
    if not allowed:
        raise HTTPException(status_code=429, detail=f"Session limit {settings.coach_session_limit_per_day}/day reached")

    session = CoachSession(
        id=str(uuid.uuid4()),
        user_id=req.user_id,
        title=req.title,
        personal_quest_id=req.personal_quest_id,
    )
    db.add(session)
    await db.commit()
    await db.refresh(session)
    return SessionResponse.model_validate(session)


async def list_sessions(db: AsyncSession, user_id: str, status: str | None) -> list[SessionResponse]:
    q = select(CoachSession).where(CoachSession.user_id == user_id)
    if status:
        q = q.where(CoachSession.status == status)
    q = q.order_by(CoachSession.updated_at.desc())
    result = await db.execute(q)
    return [SessionResponse.model_validate(s) for s in result.scalars()]


async def get_session(db: AsyncSession, session_id: str) -> CoachSession:
    s = await db.get(CoachSession, session_id)
    if not s:
        raise HTTPException(status_code=404, detail="Session not found")
    return s


# ── Streaming chat ────────────────────────────────────────────────────────────

async def stream_message(
    db: AsyncSession,
    session_id: str,
    user_id: str,
    content: str,
    request: Optional[Request] = None,
    stream_id: Optional[str] = None,
) -> AsyncGenerator[str, None]:
    """
    Tool-use loop with SSE token-level streaming:
    1. Append user message to history
    2. Call LLM — if tool_calls returned, execute and loop
    3. When end_turn, stream tokens as SSE events
    Supports cancellation via request.disconnect or stream_id.
    """
    sid = stream_id or str(uuid.uuid4())[:8]
    cancel_event = _register_stream(sid)

    async def check_cancelled() -> bool:
        """Check if stream was cancelled via event or client disconnect."""
        if cancel_event.is_set():
            return True
        if request and await request.is_disconnected():
            cancel_event.set()
            return True
        return False

    try:
        async with observe("coach_stream_message", user_id=user_id, session_id=session_id, model=MODEL, prompt_version=load_prompt("coach_system_v1").version) as ctx:
            # Rate limit
            allowed = await rate_limiter.check(
                db,
                table="coach_messages",
                user_id=user_id,
                window=timedelta(days=1),
                limit=settings.coach_message_limit_per_day,
            )
            if not allowed:
                raise HTTPException(status_code=429, detail="Message limit reached")

            # Safety: validate and sanitize user input
            safety_result = validate_and_sanitize_input(
                content,
                operation="coach",
                max_length=10000,
                redact_pii=True,
            )
            if not safety_result.safe:
                from app.infra.observability import logger
                logger.warning("coach_input_safety_violations", user_id=user_id, session_id=session_id, violations=safety_result.violations)
            content_to_use = safety_result.sanitized_input or content

            session = await get_session(db, session_id)

            # Persist user message
            user_msg = CoachMessage(
                id=str(uuid.uuid4()),
                session_id=session_id,
                role="user",
                content=content_to_use,
            )
            db.add(user_msg)
            await db.flush()

            # Build message history for LLM
            history_result = await db.execute(
                select(CoachMessage)
                .where(CoachMessage.session_id == session_id)
                .order_by(CoachMessage.created_at)
            )
            history: list[dict] = [{"role": "system", "content": _SYSTEM_PROMPT}]
            for msg in history_result.scalars():
                if msg.role == "tool":
                    history.append({"role": "tool", "content": msg.content, "tool_call_id": (msg.tool_calls or {}).get("id", "")})
                else:
                    entry: dict = {"role": msg.role}
                    if msg.content:
                        entry["content"] = msg.content
                    if msg.tool_calls and msg.role == "assistant":
                        entry["tool_calls"] = msg.tool_calls
                    history.append(entry)

            # Tool-use loop (non-streaming)
            full_response = ""
            total_prompt_tokens = 0
            total_completion_tokens = 0
            while True:
                if await check_cancelled():
                    yield f"data: {json.dumps({'cancelled': True})}\n\n"
                    return

                llm_start = time.perf_counter()
                response = await llm.chat.completions.create(
                    model=MODEL,
                    messages=history,
                    tools=COACH_TOOLS,
                    tool_choice="auto",
                    temperature=get_prompt_temperature("coach_system_v1"),
                    stream=False,
                )
                llm_ms = (time.perf_counter() - llm_start) * 1000

                # Record token usage
                usage = response.usage
                if usage:
                    total_prompt_tokens += usage.prompt_tokens
                    total_completion_tokens += usage.completion_tokens
                    ctx.record_llm_call(
                        model=MODEL,
                        prompt_tokens=usage.prompt_tokens,
                        completion_tokens=usage.completion_tokens,
                        latency_ms=llm_ms,
                    )

                msg = response.choices[0].message

                if msg.tool_calls:
                    # Execute each tool and collect results
                    tool_results = []
                    for tc in msg.tool_calls:
                        if await check_cancelled():
                            yield f"data: {json.dumps({'cancelled': True})}\n\n"
                            return

                        tool_start = time.perf_counter()
                        result_str = await execute_tool(db, tc.function.name, tc.function.arguments)
                        tool_ms = (time.perf_counter() - tool_start) * 1000
                        ctx.record_tool_call(tc.function.name, tool_ms)

                        tool_results.append({
                            "tool_call_id": tc.id,
                            "role": "tool",
                            "content": result_str,
                        })
                        # Persist tool call + result
                        db.add(CoachMessage(
                            id=str(uuid.uuid4()),
                            session_id=session_id,
                            role="tool",
                            content=result_str,
                            tool_calls={"id": tc.id, "name": tc.function.name},
                        ))

                    # Append assistant + tool results to history and loop
                    history.append({"role": "assistant", "tool_calls": [
                        {"id": tc.id, "type": "function", "function": {"name": tc.function.name, "arguments": tc.function.arguments}}
                        for tc in msg.tool_calls
                    ]})
                    history.extend(tool_results)
                    continue

                # Final text response — stream tokens via SSE
                full_response = msg.content or ""
                break

            # Stream the already-generated full_response as word chunks.
            # We do NOT make a second LLM call — full_response is already complete.
            if full_response and not await check_cancelled():
                stream_start = time.perf_counter()
                words = full_response.split(" ")
                for i in range(0, len(words), 4):
                    if await check_cancelled():
                        yield f"data: {json.dumps({'cancelled': True})}\n\n"
                        return
                    chunk = " ".join(words[i:i + 4])
                    if i + 4 < len(words):
                        chunk += " "
                    yield f"data: {json.dumps({'delta': chunk})}\n\n"
                    await asyncio.sleep(0)  # yield control to event loop between chunks
                ctx.latency.add("streaming", (time.perf_counter() - stream_start) * 1000)

            yield "data: [DONE]\n\n"

            # Persist assistant message
            db.add(CoachMessage(
                id=str(uuid.uuid4()),
                session_id=session_id,
                role="assistant",
                content=full_response,
            ))
            await db.commit()

    finally:
        _unregister_stream(sid)


async def stream_message_simple(
    db: AsyncSession,
    session_id: str,
    user_id: str,
    content: str,
) -> AsyncGenerator[str, None]:
    """
    Simplified version for direct calls without request context.
    Uses word-chunk fallback if token streaming unavailable.
    """
    async with observe("coach_stream_message", user_id=user_id, session_id=session_id, model=MODEL, prompt_version=load_prompt("coach_system_v1").version) as ctx:
        # Rate limit
        allowed = await rate_limiter.check(
            db,
            table="coach_messages",
            user_id=user_id,
            window=timedelta(days=1),
            limit=settings.coach_message_limit_per_day,
        )
        if not allowed:
            raise HTTPException(status_code=429, detail="Message limit reached")

        # Safety: validate and sanitize user input
        safety_result = validate_and_sanitize_input(
            content,
            operation="coach",
            max_length=10000,
            redact_pii=True,
        )
        if not safety_result.safe:
            from app.infra.observability import logger
            logger.warning("coach_input_safety_violations", user_id=user_id, session_id=session_id, violations=safety_result.violations)
        content_to_use = safety_result.sanitized_input or content

        session = await get_session(db, session_id)

        # Persist user message
        user_msg = CoachMessage(
            id=str(uuid.uuid4()),
            session_id=session_id,
            role="user",
            content=content_to_use,
        )
        db.add(user_msg)
        await db.flush()

        # Build message history for LLM
        history_result = await db.execute(
            select(CoachMessage)
            .where(CoachMessage.session_id == session_id)
            .order_by(CoachMessage.created_at)
        )
        history: list[dict] = [{"role": "system", "content": _SYSTEM_PROMPT}]
        for msg in history_result.scalars():
            if msg.role == "tool":
                history.append({"role": "tool", "content": msg.content, "tool_call_id": (msg.tool_calls or {}).get("id", "")})
            else:
                entry: dict = {"role": msg.role}
                if msg.content:
                    entry["content"] = msg.content
                if msg.tool_calls and msg.role == "assistant":
                    entry["tool_calls"] = msg.tool_calls
                history.append(entry)

        # Tool-use loop (non-streaming)
        full_response = ""
        while True:
            response = await llm.chat.completions.create(
                model=MODEL,
                messages=history,
                tools=COACH_TOOLS,
                tool_choice="auto",
                temperature=get_prompt_temperature("coach_system_v1"),
                stream=False,
            )

            # Record token usage
            usage = response.usage
            if usage:
                ctx.record_llm_call(
                    model=MODEL,
                    prompt_tokens=usage.prompt_tokens,
                    completion_tokens=usage.completion_tokens,
                    latency_ms=0,
                )

            msg = response.choices[0].message

            if msg.tool_calls:
                tool_results = []
                for tc in msg.tool_calls:
                    tool_start = time.perf_counter()
                    result_str = await execute_tool(db, tc.function.name, tc.function.arguments)
                    tool_ms = (time.perf_counter() - tool_start) * 1000
                    ctx.record_tool_call(tc.function.name, tool_ms)

                    tool_results.append({
                        "tool_call_id": tc.id,
                        "role": "tool",
                        "content": result_str,
                    })
                    db.add(CoachMessage(
                        id=str(uuid.uuid4()),
                        session_id=session_id,
                        role="tool",
                        content=result_str,
                        tool_calls={"id": tc.id, "name": tc.function.name},
                    ))

                history.append({"role": "assistant", "tool_calls": [
                    {"id": tc.id, "type": "function", "function": {"name": tc.function.name, "arguments": tc.function.arguments}}
                    for tc in msg.tool_calls
                ]})
                history.extend(tool_results)
                continue

            full_response = msg.content or ""
            break

        # Fallback: word-chunk streaming (for compatibility)
        words = full_response.split(" ")
        chunk_size = 5
        for i in range(0, len(words), chunk_size):
            chunk = " ".join(words[i:i + chunk_size])
            if i + chunk_size < len(words):
                chunk += " "
            yield f"data: {json.dumps({'delta': chunk})}\n\n"

        yield "data: [DONE]\n\n"

        # Persist assistant message
        db.add(CoachMessage(
            id=str(uuid.uuid4()),
            session_id=session_id,
            role="assistant",
            content=full_response,
        ))
        await db.commit()
