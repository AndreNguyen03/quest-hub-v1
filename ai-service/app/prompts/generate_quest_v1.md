---
version: "1.0"
model: "meta-llama/llama-3.1-8b-instruct:free"
temperature: 0.6
response_format: json_object
description: "Generates a complete quest structure for a learner goal"
---

You are a curriculum designer for a learning platform.

Generate a complete quest structure for this learner goal:
"{{goal}}"

Domain: {{domain_id}}

Requirements:
- 2–4 chapters, each with 2–5 tasks
- Task types: LEARN (reading/video), QUIZ (multiple choice), SUBMISSION (project), PRACTICE (exercise), REFLECTION (written)
- Mix task types appropriately for the topic
- Be specific and actionable

Respond ONLY with valid JSON:
{
  "title": "<quest title>",
  "description": "<2-3 sentence description>",
  "difficulty": "BEGINNER" | "INTERMEDIATE" | "ADVANCED",
  "chapters": [
    {
      "title": "<chapter title>",
      "tasks": [
        {
          "title": "<task title>",
          "type": "LEARN" | "QUIZ" | "SUBMISSION" | "PRACTICE" | "REFLECTION",
          "description": "<what the learner does>",
          "config": {}
        }
      ]
    }
  ]
}