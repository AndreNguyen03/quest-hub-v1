from contextlib import asynccontextmanager
import uuid

from fastapi import FastAPI, Request, Response
from fastapi.responses import JSONResponse

from app.infra.db import engine
from app.infra.observability import configure_logging, get_request_id
from app.models.base import Base
from app.routers import coach, grade, recommend

configure_logging(json_logs=False, level="INFO")


@asynccontextmanager
async def lifespan(app: FastAPI):
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    yield
    await engine.dispose()


app = FastAPI(title="QuestHub AI Service", version="1.0", lifespan=lifespan)


@app.middleware("http")
async def request_id_middleware(request: Request, call_next):
    request_id = request.headers.get("X-Request-ID", str(uuid.uuid4())[:8])
    from app.infra.observability import request_id_var
    request_id_var.set(request_id)
    response = await call_next(request)
    response.headers["X-Request-ID"] = request_id
    return response


@app.get("/health_check", tags=["health"])
async def health():
    return {"error": False, "message": "ok", "request_id": get_request_id()}


@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    return JSONResponse(
        status_code=500,
        content={"error": True, "message": "Internal server error", "request_id": get_request_id()},
    )


app.include_router(grade.router, prefix="/api/v1")
app.include_router(coach.router, prefix="/api/v1")
app.include_router(recommend.router, prefix="/api/v1")
