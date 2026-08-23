"""Quest recommendation — ES full-text search + LLM rerank."""
import json
import time
from datetime import timedelta

from fastapi import HTTPException
from sqlalchemy.ext.asyncio import AsyncSession

from app.config import settings
from app.infra import rate_limiter
from app.infra.elastic import QUEST_INDEX, es
from app.infra.llm import MODEL, llm
from app.infra.observability import observe
from app.infra.safety import validate_and_sanitize_input
from app.prompts.loader import load_prompt, get_prompt_temperature, get_prompt_response_format

_RERANK_PROMPT = load_prompt("recommend_rerank_v1")


async def recommend(db: AsyncSession, user_id: str, goal: str) -> dict:
    async with observe("recommend_quests", user_id=user_id, model=MODEL, prompt_version=_RERANK_PROMPT.version) as ctx:
        allowed = await rate_limiter.check(
            db,
            table="submission_grades",
            user_id=user_id,
            window=timedelta(hours=1),
            limit=settings.recommend_limit_per_hour,
        )
        if not allowed:
            raise HTTPException(status_code=429, detail="Recommend limit reached")

        # Safety: validate and sanitize goal input
        safety_result = validate_and_sanitize_input(
            goal,
            operation="recommend",
            max_length=1000,
            redact_pii=True,
        )
        if not safety_result.safe:
            from app.infra.observability import logger
            logger.warning("recommend_input_safety_violations", user_id=user_id, violations=safety_result.violations)
        goal_to_use = safety_result.sanitized_input or goal

        # Elasticsearch search
        es_start = time.perf_counter()
        try:
            es_response = await es.search(
                index=QUEST_INDEX,
                body={
                    "query": {
                        "multi_match": {
                            "query": goal_to_use,
                            "fields": ["title^3", "description", "tags"],
                            "fuzziness": "AUTO",
                        }
                    },
                    "_source": ["id", "title", "description", "difficulty"],
                    "size": 10,
                },
            )
            hits = es_response["hits"]["hits"]
            quests = [{"id": h["_source"]["id"], "title": h["_source"]["title"], "description": h["_source"].get("description", "")} for h in hits]
        except Exception:
            quests = []
        ctx.record_db_call((time.perf_counter() - es_start) * 1000)

        if not quests:
            return {"quests": [], "can_generate": True, "message": "No matching quests found. We can generate one for you!"}

        # LLM rerank
        llm_start = time.perf_counter()
        response = await llm.chat.completions.create(
            model=MODEL,
            messages=[{"role": "user", "content": _RERANK_PROMPT.format(
                goal=goal_to_use,
                quests_json=json.dumps(quests, ensure_ascii=False, indent=2),
            )}],
            response_format={"type": get_prompt_response_format("recommend_rerank_v1")},
            temperature=get_prompt_temperature("recommend_rerank_v1"),
        )
        llm_ms = (time.perf_counter() - llm_start) * 1000

        usage = response.usage
        if usage:
            ctx.record_llm_call(
                model=MODEL,
                prompt_tokens=usage.prompt_tokens,
                completion_tokens=usage.completion_tokens,
                latency_ms=llm_ms,
            )

        try:
            return json.loads(response.choices[0].message.content)
        except (json.JSONDecodeError, KeyError):
            return {"quests": quests[:3], "can_generate": len(quests) < 3, "message": None}
