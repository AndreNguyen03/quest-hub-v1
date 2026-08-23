"""Shared pytest fixtures."""
import pytest
from unittest.mock import AsyncMock, MagicMock


def make_llm_response(content: str):
    """Build a fake OpenAI chat completion response."""
    msg = MagicMock()
    msg.content = content
    msg.tool_calls = None
    choice = MagicMock()
    choice.message = msg
    resp = MagicMock()
    resp.choices = [choice]
    return resp
