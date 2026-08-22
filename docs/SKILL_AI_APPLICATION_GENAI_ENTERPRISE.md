---
name: enterprise-ai-application
description: Production architecture and engineering rules for AI/LLM applications, including RAG, agents, workflows, tools, prompts, memory, safety, evaluation, observability, model gateways, and deployment.
---

# Enterprise AI Application / GenAI Skill

## 1. Purpose

Use this skill when designing, implementing, reviewing, or operating applications built on foundation models or other generative AI systems.

Typical systems include:

- conversational AI
- RAG applications
- AI assistants
- agentic workflows
- tool-using agents
- document intelligence
- LLM-based extraction
- summarization
- workflow automation
- multimodal AI applications

The core lifecycle is:

```text
Problem
→ Data / Knowledge
→ Context Preparation
→ Prompt / Workflow
→ Model Interaction
→ Tools / Actions
→ Evaluation
→ Packaging
→ Deployment
→ Observability
→ Re-indexing / Prompt Evolution / Fine-tuning
→ Versioning
→ Rollback
```

Do not assume every application needs agents, RAG, memory, fine-tuning, or a separate microservice.

## 2. Architecture Principles

### 2.1 Application over model

The AI application is a software system whose model is one dependency.

Do not put business logic entirely into prompts.

Prefer explicit application code for:

- authorization
- workflow control
- tool permissions
- data access
- validation
- state transitions
- retries
- error handling
- observability

### 2.2 Separate orchestration from transport

API handlers should not contain the full agent workflow.

Prefer:

```text
API / Transport
      ↓
Application / Workflow
      ↓
RAG / Tools / Memory
      ↓
Model Gateway
      ↓
Model Provider
```

The exact service boundaries may remain inside one deployable application until scale or ownership requires extraction.

### 2.3 Non-determinism is a system concern

Model output can vary.

Production systems therefore need:

- explicit contracts
- structured outputs
- evaluation
- fallback behavior
- validation
- observability
- safe failure modes

Do not treat prompt quality as the only reliability mechanism.

## 3. Recommended Repository Structure

A practical standalone AI application repository:

```text
ai-application/
├── configs/
│   ├── app.yaml
│   └── model.yaml
├── prompts/
│   └── templates/
├── src/
│   └── ai_app/
│       ├── api/
│       ├── application/
│       ├── agent/
│       ├── workflow/
│       ├── rag/
│       ├── tools/
│       ├── memory/
│       ├── prompts/
│       ├── safety/
│       ├── evaluation/
│       ├── infrastructure/
│       └── observability/
├── tests/
│   ├── unit/
│   ├── integration/
│   └── evals/
├── pyproject.toml
├── Dockerfile
└── README.md
```

Do not instantiate every module automatically.

For a simple LLM application, this may be enough:

```text
src/ai_app/
├── api/
├── application/
├── providers/
├── prompts/
└── observability/
```

Add RAG, tools, memory, agent/workflow, and safety modules when they are real system responsibilities.

## 4. Application Layer

Application services should represent business operations around AI.

Examples:

```text
answer_question
summarize_document
execute_customer_support_workflow
generate_report
```

The application layer should coordinate:

- domain/business rules
- model calls
- retrieval
- tools
- persistence
- telemetry

Do not encode critical authorization or business rules solely in a prompt.

## 5. Agent and Workflow Architecture

Use an explicit workflow/state machine when the system requires:

- multiple model calls
- conditional execution
- tool loops
- retries
- checkpoints
- human approval
- long-running workflows

Possible conceptual structure:

```text
State
 ↓
Node
 ↓
Decision
 ├── Model
 ├── Retrieval
 ├── Tool
 └── Human / Approval
 ↓
Next State
```

A simple single-step LLM feature does not need an agent framework.

Do not introduce autonomous agents where a deterministic workflow is sufficient.

## 6. RAG Architecture

A production RAG system commonly separates:

```text
Ingestion
→ Parsing / Cleaning
→ Chunking
→ Embedding
→ Indexing
→ Retrieval
→ Re-ranking
→ Context Assembly
→ Generation
```

Possible retrieval strategies:

- vector search
- keyword search
- hybrid search
- metadata filtering
- re-ranking

Do not assume semantic chunking, hybrid search, or re-ranking is always required.

Choose based on evaluation results.

## 7. RAG Data Governance

Version and track:

- source documents
- parser/normalizer version
- chunking strategy
- embedding model
- metadata schema
- index configuration
- index snapshot where required

A retrieval result should be traceable to its source.

Preserve metadata such as:

```text
document ID
source
version
section
timestamp
access policy
```

where the application requires it.

## 8. Prompt Architecture

Treat prompts as versioned application artifacts.

Keep separate:

```text
system instructions
task instructions
retrieved context
user input
tool output
```

Do not concatenate uncontrolled content into privileged instructions.

Prompt changes should be evaluated like code changes.

Track:

- prompt version
- model version
- relevant runtime settings
- evaluation result

Do not hard-code critical prompts in scattered application files.

## 9. Tools and Function Calling

Every tool should have an explicit contract.

A tool should define:

- name
- purpose
- input schema
- output schema
- authorization requirements
- timeout
- retry behavior
- side effects
- idempotency where applicable

Treat tool execution as privileged capability.

The model should not receive unrestricted access to internal systems.

## 10. Tool Security

Apply:

- allowlists
- authentication
- authorization
- input validation
- output validation
- rate limits
- timeouts
- audit logging

For destructive operations, consider:

```text
plan
→ validate
→ require approval when necessary
→ execute
```

Do not assume a tool call is safe because the model generated it.

## 11. Memory

Distinguish:

### Conversation state

Short-lived state needed to continue an interaction.

### Persistent user/application memory

Longer-lived information retained intentionally.

### Knowledge

External authoritative information used by retrieval.

Do not use a vector database as a generic replacement for all application state.

Persist only what the product actually needs.

## 12. Guardrails and Safety

Safety should be defense in depth.

Possible layers:

```text
Input validation
→ authentication/authorization
→ prompt/input checks
→ tool policy checks
→ model execution
→ structured output validation
→ grounding/citation checks
→ output policy checks
```

Guardrails reduce risk; they do not guarantee elimination of hallucinations or prompt injection.

Critical authorization must be enforced by deterministic application code, not by model instructions.

## 13. Evaluation

LLM applications require dedicated evaluation sets.

Possible dimensions:

### Task quality

- correctness
- completeness
- relevance
- instruction following

### RAG quality

- retrieval quality
- groundedness
- citation correctness
- context relevance

### Safety

- policy compliance
- prompt-injection resistance
- sensitive-data handling
- unsafe tool execution resistance

### Operational quality

- latency
- TTFT
- tokens
- cost
- timeout/error rates

Use LLM-as-a-Judge as one evaluation mechanism, not universal ground truth.

Combine automated evaluation with deterministic assertions and human review when appropriate.

## 14. Golden Datasets and Regression

Maintain curated evaluation datasets for important workflows.

A production change should be checked against relevant regression cases when feasible.

Changes that may require evaluation:

- model provider/model version
- prompt
- retrieval strategy
- chunking
- embedding model
- reranker
- tool schema
- workflow logic
- safety rules

## 15. Model Gateway

A gateway can provide:

- provider abstraction
- model routing
- fallback
- rate limiting
- usage accounting
- caching where safe
- centralized configuration

A gateway is useful when there are multiple providers/models or meaningful operational requirements.

Do not introduce a gateway solely because it is common in architecture diagrams.

## 16. Deployment Architecture

A simple AI application can be:

```text
Client
  ↓
API / AI Application
  ├── Workflow
  ├── RAG
  ├── Tools
  ├── Memory
  └── Model Client
       ↓
    LLM Provider
```

A larger system may evolve toward:

```text
Client
 ↓
API Gateway
 ↓
AI Application / Orchestrator
 ├── RAG
 ├── Tool Services
 ├── Memory
 └── Model Gateway
       ↓
   Model Providers / Self-hosted Inference
```

Split services only when independent scaling, ownership, latency, security, or failure isolation justify the operational cost.

## 17. Streaming

Use streaming when user experience benefits from incremental output.

Possible transports:

```text
SSE
WebSocket
streaming HTTP
```

Streaming should not bypass:

- authorization
- telemetry
- cancellation
- timeout handling
- output validation requirements

## 18. Error Handling and Reliability

Model systems need explicit failure handling.

Plan for:

- model timeout
- provider outage
- malformed structured output
- tool timeout
- retrieval failure
- vector-store failure
- context overflow
- rate limiting
- partial workflow failure

Use:

- bounded retries
- exponential backoff where appropriate
- fallback models/providers where justified
- circuit-breaking where needed
- graceful degradation
- idempotent tool operations

Do not endlessly retry model or tool failures.

## 19. Observability

Capture the execution graph sufficiently to answer:

```text
What request happened?
Which model/version was used?
Which prompt/configuration was used?
Which documents were retrieved?
Which tools were called?
How many tokens were consumed?
How long did each step take?
What failed?
What did it cost?
```

Use distributed tracing and structured telemetry when system complexity warrants it.

Do not log sensitive prompts, documents, secrets, or personal data indiscriminately.

## 20. Cost and Performance

Measure:

- request latency
- TTFT
- generation latency
- token usage
- model cost
- retrieval latency
- tool latency
- cache hit rate where relevant

Optimize only after measuring bottlenecks.

Possible techniques:

- model routing
- caching
- prompt reduction
- retrieval optimization
- batching
- streaming
- smaller models
- quantization for self-hosted models

## 21. CI/CD

CI should validate:

- application tests
- schemas
- tool contracts
- integration behavior
- evaluation subsets
- prompt changes where feasible
- security checks

CD should deploy immutable application artifacts and versioned configuration.

For high-risk workflows, use evaluation gates before promotion.

## 22. Packaging and Python Runtime

Keep application runtime dependencies separate from optional heavy experimentation dependencies when possible.

A common repository-level separation is:

```text
application runtime
development/test dependencies
optional evaluation/tooling dependencies
```

Use `pyproject.toml` and a reproducible lock strategy.

The choice of uv, Poetry, Hatch, or another package manager is an implementation decision, not an architectural principle.

## 23. Versioning

Track independently:

```text
application code
prompt version
workflow/agent configuration
model/provider version
embedding model
reranker
retrieval/index version
tool schemas
evaluation dataset version
```

A production response should be traceable to the versions that generated it.

## 24. Knowledge Re-indexing

RAG knowledge may need re-indexing because of:

- source document changes
- parser changes
- chunking changes
- embedding model changes
- metadata changes
- retrieval strategy changes

Treat indexing as a reproducible pipeline, not a manual database operation.

## 25. Fine-Tuning

Do not fine-tune by default.

Use prompt/context engineering first when the problem is:

- missing context
- task instructions
- retrieval
- structured output
- workflow design

Consider fine-tuning when there is demonstrated value in:

- behavioral consistency
- domain-specific task performance
- style
- specialized output patterns

Fine-tuning must have evaluation evidence.

## 26. Testing

Use:

```text
Unit
Integration
Evaluation
End-to-End
```

### Unit

Test deterministic orchestration, parsers, validators, routing rules, and data transformations.

### Integration

Test:

- model adapter
- vector store
- database
- tools
- external providers

### Evaluation

Test semantic/system quality on curated datasets.

### E2E

Test the complete application workflow.

Do not treat LLM evaluation as a replacement for software tests.

## 27. Security

Protect:

- API credentials
- model provider keys
- retrieved confidential data
- user conversation data
- tool credentials
- system prompts where sensitive

Enforce:

- authentication
- authorization
- tenant isolation
- data access controls
- tool permissions
- rate limits
- auditability

Never trust model-generated authorization decisions.

## 28. Architecture Evolution

### Stage 1 — Simple AI feature

Use a modular application:

```text
api/
application/
prompts/
providers/
observability/
```

### Stage 2 — RAG/Workflow product

Introduce:

```text
rag/
workflow/
tools/
evaluation/
safety/
```

### Stage 3 — High-scale AI platform

Introduce only where justified:

- model gateway
- independent RAG service
- dedicated tool services
- specialized inference service
- distributed tracing
- centralized evaluation platform

Do not split each conceptual package into a microservice automatically.

## 29. Anti-Patterns

Avoid:

- putting all business logic in prompts
- giant agent loops
- autonomous agents for deterministic workflows
- raw model output used as trusted structured data
- tools without authorization
- vector DB used as generic application storage
- storing all conversation state forever
- prompt changes without regression evaluation
- no model/provider version tracking
- no retrieval provenance
- no timeout/retry boundaries
- logging sensitive context indiscriminately
- microservice explosion
- fine-tuning without evidence
- assuming guardrails eliminate hallucination

## 30. Decision Procedure

When reviewing an AI application:

1. Identify whether the problem actually needs an LLM.
2. Classify the workflow as deterministic, model-assisted, RAG, or agentic.
3. Keep business rules in application code.
4. Decide whether retrieval is actually required.
5. Define model and provider boundaries.
6. Define tool permissions and side effects.
7. Classify conversation state, memory, and knowledge separately.
8. Establish evaluation datasets and success metrics.
9. Add observability before scaling.
10. Design explicit failure and fallback behavior.
11. Version prompts, models, retrieval, tools, and workflows.
12. Split services only when operational boundaries justify it.

## 31. Non-Goals

This skill does not mandate:

- LangGraph
- LlamaIndex
- LangChain
- RAG
- vector databases
- agent architectures
- LiteLLM
- a specific LLM provider
- Kubernetes
- microservices
- fine-tuning
- a specific observability vendor
