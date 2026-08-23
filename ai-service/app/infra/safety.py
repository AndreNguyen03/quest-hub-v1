"""Safety layer: input validation, prompt injection detection, output policy."""
import re
from dataclasses import dataclass
from typing import Any

# Patterns that may indicate prompt injection attempts
INJECTION_PATTERNS = [
    r"(?i)ignore\s+(?:previous|above|all)\s+instructions?",
    r"(?i)forget\s+(?:everything|all|previous)",
    r"(?i)you\s+are\s+(?:now|from\s+now\s+on)\s+(?:a|an)\s+\w+",
    r"(?i)act\s+as\s+(?:a|an)\s+\w+",
    r"(?i)pretend\s+to\s+be\s+(?:a|an)\s+\w+",
    r"(?i)system\s*:\s*",
    r"(?i)assistant\s*:\s*",
    r"(?i)user\s*:\s*",
    r"(?i)<\|.*?\|>",
    r"(?i)\[INST\].*?\[/INST\]",
    r"(?i)###\s*(?:system|user|assistant)\s*###",
    r"(?i)ignore\s+the\s+rules?",
    r"(?i)bypass\s+(?:the\s+)?(?:safety|guardrail|filter|restriction)",
    r"(?i)do\s+not\s+(?:follow|obey|comply)",
    r"(?i)disregard\s+(?:previous|all)\s+(?:instructions?|prompts?)",
    r"(?i)new\s+(?:instructions?|rules?|guidelines?)",
    r"(?i)output\s+(?:only|just)\s+(?:the|this)",
    r"(?i)print\s+(?:your|the)\s+(?:system\s+)?prompt",
    r"(?i)show\s+me\s+(?:your|the)\s+(?:system\s+)?prompt",
    r"(?i)what\s+(?:is|are)\s+(?:your|the)\s+(?:system\s+)?prompt",
    r"(?i)reveal\s+(?:your|the)\s+(?:system\s+)?prompt",
    r"(?i)leak\s+(?:your|the)\s+(?:system\s+)?prompt",
]

# Compile patterns for performance
_COMPILED_PATTERNS = [re.compile(p) for p in INJECTION_PATTERNS]

# PII patterns
PII_PATTERNS = {
    "email": re.compile(r"\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b"),
    "phone": re.compile(r"\b(?:\+?\d{1,3}[-.\s]?)?\(?\d{3}\)?[-.\s]?\d{3}[-.\s]?\d{4}\b"),
    "ssn": re.compile(r"\b\d{3}-\d{2}-\d{4}\b"),
    "credit_card": re.compile(r"\b(?:\d{4}[-\s]?){3}\d{4}\b"),
    "ip_address": re.compile(r"\b(?:\d{1,3}\.){3}\d{1,3}\b"),
}

MAX_INPUT_LENGTH = 10000
MAX_EVIDENCE_LENGTH = 50000


@dataclass
class SafetyResult:
    safe: bool
    violations: list[str]
    sanitized_input: str | None = None


def detect_prompt_injection(text: str) -> list[str]:
    """Detect potential prompt injection patterns in text."""
    violations = []
    for pattern in _COMPILED_PATTERNS:
        if pattern.search(text):
            violations.append(f"prompt_injection:{pattern.pattern[:50]}")
    return violations


def detect_pii(text: str) -> list[str]:
    """Detect PII in text."""
    violations = []
    for pii_type, pattern in PII_PATTERNS.items():
        if pattern.search(text):
            violations.append(f"pii:{pii_type}")
    return violations


def sanitize_pii(text: str) -> str:
    """Redact PII from text."""
    sanitized = text
    for pii_type, pattern in PII_PATTERNS.items():
        sanitized = pattern.sub(f"[REDACTED_{pii_type.upper()}]", sanitized)
    return sanitized


def validate_input_length(text: str, max_length: int = MAX_INPUT_LENGTH) -> list[str]:
    """Validate input length."""
    violations = []
    if len(text) > max_length:
        violations.append(f"input_too_long:{len(text)}>{max_length}")
    return violations


def validate_json_output(output: str, expected_keys: list[str] | None = None) -> tuple[bool, Any, list[str]]:
    """Validate JSON output structure."""
    violations = []
    try:
        import json
        parsed = json.loads(output)
        if expected_keys:
            for key in expected_keys:
                if key not in parsed:
                    violations.append(f"missing_key:{key}")
        return True, parsed, violations
    except json.JSONDecodeError as e:
        violations.append(f"invalid_json:{str(e)}")
        return False, None, violations


def check_output_policy(output: str, operation: str) -> list[str]:
    """Check output against policy rules."""
    violations = []

    # Check for PII in output
    violations.extend(detect_pii(output))

    # Operation-specific checks
    if operation == "grade":
        # Grade output should be valid JSON with specific keys
        valid, parsed, json_violations = validate_json_output(output, ["status", "score", "feedback", "criteria"])
        violations.extend(json_violations)
        if valid:
            # Validate status value
            if parsed.get("status") not in ("PASS", "FAIL", "NEEDS_REVISION"):
                violations.append("grade_invalid_status")
            # Validate score range
            score = parsed.get("score")
            if not isinstance(score, (int, float)) or not (0 <= score <= 100):
                violations.append("grade_invalid_score")

    elif operation == "recommend":
        valid, parsed, json_violations = validate_json_output(output, ["quests", "can_generate"])
        violations.extend(json_violations)

    elif operation == "generate":
        valid, parsed, json_violations = validate_json_output(output, ["title", "chapters"])
        violations.extend(json_violations)

    elif operation == "coach":
        # Coach output is free text but should not contain PII
        pass

    return violations


def validate_and_sanitize_input(
    text: str,
    operation: str,
    max_length: int | None = None,
    redact_pii: bool = True,
) -> SafetyResult:
    """Comprehensive input validation and sanitization."""
    max_len = max_length or MAX_INPUT_LENGTH
    violations = []

    # Length check
    violations.extend(validate_input_length(text, max_len))

    # Prompt injection detection
    violations.extend(detect_prompt_injection(text))

    # PII detection
    pii_violations = detect_pii(text)
    violations.extend(pii_violations)

    # Sanitize if needed
    sanitized = text
    if redact_pii and pii_violations:
        sanitized = sanitize_pii(text)

    # Determine if safe (allow but log violations)
    safe = len(violations) == 0 or all(not v.startswith("prompt_injection") for v in violations)

    return SafetyResult(
        safe=safe,
        violations=violations,
        sanitized_input=sanitized if sanitized != text else None,
    )


def validate_output(output: str, operation: str) -> SafetyResult:
    """Validate output against policy."""
    violations = check_output_policy(output, operation)

    # Also check for PII in output
    violations.extend(detect_pii(output))

    safe = len(violations) == 0

    return SafetyResult(safe=safe, violations=violations)