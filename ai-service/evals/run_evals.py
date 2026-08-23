"""Evaluation harness — runs golden dataset evaluations with LLM-as-judge."""
import asyncio
import json
import os
import statistics
from dataclasses import dataclass
from pathlib import Path
from typing import Any

from openai import AsyncOpenAI

from app.config import settings
from app.infra.llm import llm as default_llm


EVALS_DIR = Path(__file__).parent / "evals"


@dataclass
class EvalResult:
    passed: bool
    score: float
    details: dict[str, Any]
    judge_reasoning: str | None = None


class LLMAssistantJudge:
    """LLM-as-judge for evaluating AI outputs."""

    def __init__(self, client: AsyncOpenAI | None = None, model: str | None = None):
        self.client = client or default_llm
        self.model = model or settings.ai_model

    async def judge_grade(self, expected: dict, actual: dict) -> EvalResult:
        """Judge a grading result against expected."""
        prompt = f"""You are an expert evaluator. Compare the AI grader's output with the expected result.

Expected:
- Status: {expected.get('status')}
- Min/Max Score: {expected.get('min_score', 'N/A')} / {expected.get('max_score', 'N/A')}
- Criteria expectations: {json.dumps(expected.get('criteria', {}), indent=2)}

Actual AI Output:
- Status: {actual.get('status')}
- Score: {actual.get('score')}
- Feedback: {actual.get('feedback')}
- Criteria: {json.dumps(actual.get('criteria', []), indent=2)}

Evaluate if the actual output is reasonable given the expected. Consider:
1. Status matches expected (PASS/FAIL/NEEDS_REVISION)
2. Score is within expected range
3. Criteria scores align with expectations

Respond with JSON:
{{
  "passed": true/false,
  "score": 0.0-1.0,
  "reasoning": "explanation"
}}"""

        response = await self.client.chat.completions.create(
            model=self.model,
            messages=[{"role": "user", "content": prompt}],
            response_format={"type": "json_object"},
            temperature=0.1,
        )

        result = json.loads(response.choices[0].message.content)
        return EvalResult(
            passed=result["passed"],
            score=result["score"],
            details={"expected": expected, "actual": actual},
            judge_reasoning=result.get("reasoning"),
        )

    async def judge_coach(self, expected: dict, actual_tools_used: list, actual_response: str) -> EvalResult:
        """Judge a coach response."""
        prompt = f"""You are an expert evaluator. Evaluate the AI coach's response.

User Profile: {json.dumps(expected.get('user_profile', {}), indent=2)}
User Message: {expected.get('user_message')}

Expected Behavior:
- Should use tools: {expected.get('expected_behavior', {}).get('uses_tools', [])}
- Response should contain: {expected.get('expected_behavior', {}).get('response_contains', [])}
- Response should NOT contain: {expected.get('expected_behavior', {}).get('does_not_contain', [])}

Actual:
- Tools used: {actual_tools_used}
- Response: {actual_response}

Respond with JSON:
{{
  "passed": true/false,
  "score": 0.0-1.0,
  "reasoning": "explanation"
}}"""

        response = await self.client.chat.completions.create(
            model=self.model,
            messages=[{"role": "user", "content": prompt}],
            response_format={"type": "json_object"},
            temperature=0.1,
        )

        result = json.loads(response.choices[0].message.content)
        return EvalResult(
            passed=result["passed"],
            score=result["score"],
            details={"expected": expected, "actual_tools": actual_tools_used, "actual_response": actual_response},
            judge_reasoning=result.get("reasoning"),
        )

    async def judge_recommend(self, expected: dict, actual: dict) -> EvalResult:
        """Judge a recommendation result."""
        prompt = f"""You are an expert evaluator. Evaluate the quest recommendation.

Goal: {expected.get('goal')}
Available Quests: {json.dumps(expected.get('available_quests', []), indent=2)}

Expected:
- Top match ID: {expected.get('expected', {}).get('top_match_id', 'N/A')}
- Can generate: {expected.get('expected', {}).get('can_generate', 'N/A')}
- Message should contain: {expected.get('expected', {}).get('message_contains', [])}

Actual:
{json.dumps(actual, indent=2)}

Respond with JSON:
{{
  "passed": true/false,
  "score": 0.0-1.0,
  "reasoning": "explanation"
}}"""

        response = await self.client.chat.completions.create(
            model=self.model,
            messages=[{"role": "user", "content": prompt}],
            response_format={"type": "json_object"},
            temperature=0.1,
        )

        result = json.loads(response.choices[0].message.content)
        return EvalResult(
            passed=result["passed"],
            score=result["score"],
            details={"expected": expected, "actual": actual},
            judge_reasoning=result.get("reasoning"),
        )

    async def judge_generate(self, expected: dict, actual: dict) -> EvalResult:
        """Judge a quest generation result."""
        prompt = f"""You are an expert evaluator. Evaluate the generated quest structure.

Goal: {expected.get('goal')}
Domain: {expected.get('domain_id')}

Expected structure:
{json.dumps(expected.get('expected', {}), indent=2)}

Actual generated quest:
{json.dumps(actual, indent=2)}

Check:
1. Has title, description, difficulty
2. Has 2-4 chapters
3. Each chapter has 2-5 tasks
4. Task types match expected
5. Difficulty matches expected

Respond with JSON:
{{
  "passed": true/false,
  "score": 0.0-1.0,
  "reasoning": "explanation"
}}"""

        response = await self.client.chat.completions.create(
            model=self.model,
            messages=[{"role": "user", "content": prompt}],
            response_format={"type": "json_object"},
            temperature=0.1,
        )

        result = json.loads(response.choices[0].message.content)
        return EvalResult(
            passed=result["passed"],
            score=result["score"],
            details={"expected": expected, "actual": actual},
            judge_reasoning=result.get("reasoning"),
        )


def load_golden_dataset(filename: str) -> list[dict]:
    """Load golden dataset from JSONL file."""
    path = EVALS_DIR / filename
    if not path.exists():
        return []
    with open(path, encoding="utf-8") as f:
        return [json.loads(line) for line in f if line.strip()]


async def run_grade_evals(judge: LLMAssistantJudge) -> dict:
    """Run grade evaluation on golden dataset."""
    from app.services.grade_service import grade
    from unittest.mock import AsyncMock, MagicMock

    dataset = load_golden_dataset("grade_golden.jsonl")
    results = []

    for item in dataset:
        # Create mock DB
        db = AsyncMock()
        pt = MagicMock()
        pt.task_id = "task-1"
        pt.quest_id = "quest-1"
        task = MagicMock()
        task.title = item["task_title"]
        task.description = item["task_description"]
        task.config = {"rubric": item["rubric"]}
        db.get = AsyncMock(side_effect=lambda model, pk: pt if "PersonalTask" in str(model) else task)
        scalar_result = MagicMock()
        scalar_result.scalar_one = MagicMock(return_value=0)
        db.execute = AsyncMock(return_value=scalar_result)
        db.add = MagicMock()
        db.flush = AsyncMock()
        db.commit = AsyncMock()

        try:
            result = await grade(db, "user-1", "pt-1", item["evidence"])
            actual = {
                "status": result.status,
                "score": result.score,
                "feedback": result.feedback,
                "criteria": [{"name": c.name, "score": c.score, "feedback": c.feedback} for c in result.criteria],
            }
            eval_result = await judge.judge_grade(item["expected"], actual)
            results.append(eval_result)
        except Exception as e:
            results.append(EvalResult(passed=False, score=0.0, details={"error": str(e), "item": item}))

    passed = sum(1 for r in results if r.passed)
    avg_score = statistics.mean(r.score for r in results) if results else 0
    return {
        "total": len(results),
        "passed": passed,
        "failed": len(results) - passed,
        "pass_rate": passed / len(results) if results else 0,
        "avg_score": avg_score,
        "results": [{"passed": r.passed, "score": r.score, "reasoning": r.judge_reasoning} for r in results],
    }


async def run_recommend_evals(judge: LLMAssistantJudge) -> dict:
    """Run recommend evaluation on golden dataset."""
    from app.services.recommend_service import recommend
    from unittest.mock import AsyncMock, MagicMock

    dataset = load_golden_dataset("recommend_golden.jsonl")
    results = []

    for item in dataset:
        db = AsyncMock()
        # Mock ES response
        es_response = {
            "hits": {
                "hits": [
                    {"_source": q} for q in item["available_quests"]
                ]
            }
        }
        es = MagicMock()
        es.search = AsyncMock(return_value=es_response)

        # Patch the es module
        import app.infra.elastic as elastic_module
        original_es = elastic_module.es
        elastic_module.es = es

        try:
            result = await recommend(db, "user-1", item["goal"])
            eval_result = await judge.judge_recommend(item, result)
            results.append(eval_result)
        except Exception as e:
            results.append(EvalResult(passed=False, score=0.0, details={"error": str(e), "item": item}))
        finally:
            elastic_module.es = original_es

    passed = sum(1 for r in results if r.passed)
    avg_score = statistics.mean(r.score for r in results) if results else 0
    return {
        "total": len(results),
        "passed": passed,
        "failed": len(results) - passed,
        "pass_rate": passed / len(results) if results else 0,
        "avg_score": avg_score,
        "results": [{"passed": r.passed, "score": r.score, "reasoning": r.judge_reasoning} for r in results],
    }


async def run_generate_evals(judge: LLMAssistantJudge) -> dict:
    """Run generate evaluation on golden dataset."""
    from app.services.generate_service import generate_quest
    from unittest.mock import AsyncMock, MagicMock

    dataset = load_golden_dataset("generate_golden.jsonl")
    results = []

    for item in dataset:
        db = AsyncMock()

        # Mock httpx client
        import httpx
        original_client = httpx.AsyncClient

        class MockClient:
            def __init__(self, *args, **kwargs):
                pass
            async def __aenter__(self):
                return self
            async def __aexit__(self, *args):
                pass
            async def post(self, *args, **kwargs):
                resp = MagicMock()
                resp.status_code = 201
                resp.json = MagicMock(return_value={"id": "generated-quest-1"})
                return resp

        httpx.AsyncClient = MockClient

        try:
            result = await generate_quest(db, "user-1", item["goal"], item["domain_id"])
            eval_result = await judge.judge_generate(item, result)
            results.append(eval_result)
        except Exception as e:
            results.append(EvalResult(passed=False, score=0.0, details={"error": str(e), "item": item}))
        finally:
            httpx.AsyncClient = original_client

    passed = sum(1 for r in results if r.passed)
    avg_score = statistics.mean(r.score for r in results) if results else 0
    return {
        "total": len(results),
        "passed": passed,
        "failed": len(results) - passed,
        "pass_rate": passed / len(results) if results else 0,
        "avg_score": avg_score,
        "results": [{"passed": r.passed, "score": r.score, "reasoning": r.judge_reasoning} for r in results],
    }


async def run_all_evals() -> dict:
    """Run all evaluations."""
    judge = LLMAssistantJudge()

    print("Running grade evaluations...")
    grade_results = await run_grade_evals(judge)

    print("Running recommend evaluations...")
    recommend_results = await run_recommend_evals(judge)

    print("Running generate evaluations...")
    generate_results = await run_generate_evals(judge)

    all_passed = grade_results["passed"] + recommend_results["passed"] + generate_results["passed"]
    all_total = grade_results["total"] + recommend_results["total"] + generate_results["total"]

    return {
        "summary": {
            "total": all_total,
            "passed": all_passed,
            "failed": all_total - all_passed,
            "pass_rate": all_passed / all_total if all_total else 0,
        },
        "grade": grade_results,
        "recommend": recommend_results,
        "generate": generate_results,
    }


if __name__ == "__main__":
    import sys
    results = asyncio.run(run_all_evals())
    print(json.dumps(results, indent=2))

    # Exit with error code if pass rate below threshold
    if results["summary"]["pass_rate"] < 0.7:
        sys.exit(1)