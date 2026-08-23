"""OpenRouter client — OpenAI-compatible, swap model via AI_MODEL env var."""
from openai import AsyncOpenAI

from app.config import settings

# Single shared async client — thread-safe, connection-pooled.
llm = AsyncOpenAI(
    api_key=settings.openrouter_api_key,
    base_url=settings.openrouter_base_url,
)

MODEL = settings.ai_model
