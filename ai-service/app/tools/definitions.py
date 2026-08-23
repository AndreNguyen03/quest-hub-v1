"""Tool definitions in OpenAI tool-calling format."""

COACH_TOOLS = [
    {
        "type": "function",
        "function": {
            "name": "get_progress",
            "description": "Get the learner's progress in a personal quest — tasks completed vs total.",
            "parameters": {
                "type": "object",
                "properties": {
                    "personal_quest_id": {"type": "string", "description": "UUID of the personal quest"}
                },
                "required": ["personal_quest_id"],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "get_streak",
            "description": "Get the learner's current consecutive-day activity streak.",
            "parameters": {
                "type": "object",
                "properties": {
                    "user_id": {"type": "string", "description": "UUID of the user"}
                },
                "required": ["user_id"],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "get_achievements",
            "description": "List achievements the learner has unlocked.",
            "parameters": {
                "type": "object",
                "properties": {
                    "user_id": {"type": "string", "description": "UUID of the user"}
                },
                "required": ["user_id"],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "get_upcoming_tasks",
            "description": "Get the next 3 incomplete tasks in the learner's personal quest.",
            "parameters": {
                "type": "object",
                "properties": {
                    "personal_quest_id": {"type": "string", "description": "UUID of the personal quest"}
                },
                "required": ["personal_quest_id"],
            },
        },
    },
]
