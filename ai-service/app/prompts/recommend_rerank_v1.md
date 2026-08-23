---
version: "1.0"
model: "meta-llama/llama-3.1-8b-instruct:free"
temperature: 0.3
response_format: json_object
description: "Reranks quest search results based on learner's goal"
---

A learner has the following goal:
"{{goal}}"

Here are quest search results from our platform:
{{quests_json}}

Select the most relevant quests and explain briefly why each fits the goal.
Also determine if none of the quests match well (canGenerate=true means we should offer to generate a custom quest).

Respond ONLY with valid JSON:
{
  "quests": [
    {"id": "<quest_id>", "title": "<title>", "reason": "<why it fits>"}
  ],
  "can_generate": true | false,
  "message": "<optional 1-sentence guidance>"
}