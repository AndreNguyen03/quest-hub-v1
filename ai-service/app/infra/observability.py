"""Observability: structured logging, token tracking, latency metrics, cost estimation."""
import json
import time
import uuid
from contextlib import asynccontextmanager
from dataclasses import dataclass, field
from datetime import datetime
from functools import wraps
from typing import Any, Callable, Optional
from contextvars import ContextVar

import structlog

# Cost per 1K tokens (USD) - update per model/provider
MODEL_COSTS = {
    "meta-llama/llama-3.1-8b-instruct:free": {"input": 0.0, "output": 0.0},
    "openai/gpt-4o-mini": {"input": 0.00015, "output": 0.0006},
    "openai/gpt-4o": {"input": 0.0025, "output": 0.01},
    "anthropic/claude-3.5-sonnet": {"input": 0.003, "output": 0.015},
}

request_id_var: ContextVar[str] = ContextVar("request_id", default="")
user_id_var: ContextVar[str] = ContextVar("user_id", default="")
session_id_var: ContextVar[str] = ContextVar("session_id", default="")

logger = structlog.get_logger()


@dataclass
class TokenUsage:
    prompt_tokens: int = 0
    completion_tokens: int = 0
    total_tokens: int = 0

    def add(self, other: "TokenUsage") -> "TokenUsage":
        return TokenUsage(
            prompt_tokens=self.prompt_tokens + other.prompt_tokens,
            completion_tokens=self.completion_tokens + other.completion_tokens,
            total_tokens=self.total_tokens + other.total_tokens,
        )


@dataclass
class LatencyMetrics:
    total_ms: float = 0.0
    llm_ms: float = 0.0
    tool_ms: float = 0.0
    db_ms: float = 0.0
    breakdown: dict[str, float] = field(default_factory=dict)

    def add(self, key: str, ms: float) -> None:
        self.breakdown[key] = self.breakdown.get(key, 0.0) + ms
        self.total_ms += ms


@dataclass
class CostEstimate:
    usd: float = 0.0
    model: str = ""

    @classmethod
    def from_usage(cls, usage: TokenUsage, model: str) -> "CostEstimate":
        costs = MODEL_COSTS.get(model, {"input": 0.0, "output": 0.0})
        input_cost = (usage.prompt_tokens / 1000) * costs["input"]
        output_cost = (usage.completion_tokens / 1000) * costs["output"]
        return cls(usd=round(input_cost + output_cost, 6), model=model)


class ObservabilityContext:
    """Thread-local context for a single request/workflow."""

    def __init__(
        self,
        request_id: str | None = None,
        user_id: str | None = None,
        session_id: str | None = None,
    ):
        self.request_id = request_id or str(uuid.uuid4())[:8]
        self.user_id = user_id
        self.session_id = session_id
        self.token_usage = TokenUsage()
        self.latency = LatencyMetrics()
        self.start_time = time.perf_counter()
        self.metadata: dict[str, Any] = {}

    def __enter__(self):
        request_id_var.set(self.request_id)
        if self.user_id:
            user_id_var.set(self.user_id)
        if self.session_id:
            session_id_var.set(self.session_id)
        return self

    def __exit__(self, *args):
        request_id_var.set("")
        user_id_var.set("")
        session_id_var.set("")

    def record_llm_call(
        self,
        model: str,
        prompt_tokens: int,
        completion_tokens: int,
        latency_ms: float,
    ) -> CostEstimate:
        usage = TokenUsage(prompt_tokens=prompt_tokens, completion_tokens=completion_tokens)
        self.token_usage = self.token_usage.add(usage)
        self.latency.llm_ms += latency_ms
        self.latency.add("llm", latency_ms)
        return CostEstimate.from_usage(usage, model)

    def record_tool_call(self, tool_name: str, latency_ms: float) -> None:
        self.latency.tool_ms += latency_ms
        self.latency.add(f"tool:{tool_name}", latency_ms)

    def record_db_call(self, latency_ms: float) -> None:
        self.latency.db_ms += latency_ms
        self.latency.add("db", latency_ms)

    def log_completion(self, operation: str, status: str = "success", **extra) -> None:
        elapsed_ms = (time.perf_counter() - self.start_time) * 1000
        cost = CostEstimate.from_usage(self.token_usage, self.metadata.get("model", ""))

        logger.info(
            "ai_operation_complete",
            request_id=self.request_id,
            user_id=self.user_id,
            session_id=self.session_id,
            operation=operation,
            status=status,
            elapsed_ms=round(elapsed_ms, 2),
            llm_ms=round(self.latency.llm_ms, 2),
            tool_ms=round(self.latency.tool_ms, 2),
            db_ms=round(self.latency.db_ms, 2),
            prompt_tokens=self.token_usage.prompt_tokens,
            completion_tokens=self.token_usage.completion_tokens,
            total_tokens=self.token_usage.total_tokens,
            estimated_cost_usd=cost.usd,
            model=cost.model,
            **extra,
        )


def get_request_id() -> str:
    return request_id_var.get("")


def get_user_id() -> str:
    return user_id_var.get("")


def get_session_id() -> str:
    return session_id_var.get("")


@asynccontextmanager
async def observe(operation: str, user_id: str | None = None, session_id: str | None = None, **metadata):
    """Async context manager for observing an AI operation."""
    ctx = ObservabilityContext(user_id=user_id, session_id=session_id)
    ctx.metadata.update(metadata)
    with ctx:
        try:
            yield ctx
            ctx.log_completion(operation, status="success")
        except Exception as e:
            ctx.log_completion(operation, status="error", error_type=type(e).__name__, error_message=str(e))
            raise


def track_latency(phase: str):
    """Decorator to track latency of a function."""
    def decorator(func: Callable) -> Callable:
        @wraps(func)
        async def async_wrapper(*args, **kwargs):
            start = time.perf_counter()
            try:
                return await func(*args, **kwargs)
            finally:
                ms = (time.perf_counter() - start) * 1000
                request_id = get_request_id()
                logger.debug("phase_latency", request_id=request_id, phase=phase, ms=round(ms, 2))

        @wraps(func)
        def sync_wrapper(*args, **kwargs):
            start = time.perf_counter()
            try:
                return func(*args, **kwargs)
            finally:
                ms = (time.perf_counter() - start) * 1000
                request_id = get_request_id()
                logger.debug("phase_latency", request_id=request_id, phase=phase, ms=round(ms, 2))

        import asyncio
        return async_wrapper if asyncio.iscoroutinefunction(func) else sync_wrapper
    return decorator


def configure_logging(json_logs: bool = False, level: str = "INFO") -> None:
    """Configure structlog for structured logging."""
    processors = [
        structlog.contextvars.merge_contextvars,
        structlog.processors.add_log_level,
        structlog.processors.TimeStamper(fmt="iso"),
        structlog.processors.JSONRenderer() if json_logs else structlog.dev.ConsoleRenderer(),
    ]
    structlog.configure(
        processors=processors,
        wrapper_class=structlog.make_filtering_bound_logger(level),
        context_class=dict,
        logger_factory=structlog.PrintLoggerFactory(),
        cache_logger_on_first_use=True,
    )