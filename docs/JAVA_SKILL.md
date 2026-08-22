---
name: java-backend
description: Project-specific implementation skill for a Java Spring Boot modular-monolith backend. Apply when modifying backend/src/main/java or related backend tests. Preserve the existing modular-monolith, bounded-context, DDD, Spring Modulith, persistence, transaction, API, and outbox conventions described below.
---

# Java Backend — AI Implementation Skill

## 1. Scope

Apply this skill when working on the Java backend.

Repository context:

- Spring Boot 3.x
- modular monolith
- bounded contexts: `identity`, `quest`, `marketplace`, `world`, `admin`
- shared kernel
- side applications consume events through transactional outbox
- source of architectural truth: `docs/ddd-convention.md`

This is a **project-specific skill**. Do not generalize these conventions to unrelated repositories.

## 2. Priority Rules

When making changes, follow this order:

1. Preserve existing domain and module boundaries.
2. Preserve existing public contracts unless the task explicitly changes them.
3. Follow existing project conventions before introducing new abstractions.
4. Keep framework/infrastructure concerns out of the domain.
5. Enforce module boundaries through Spring Modulith.
6. Add the minimum code necessary for the requested behavior.
7. Do not rename or restructure unrelated code.

Before creating a new package/class, inspect nearby existing implementations and follow their established pattern.

Never invent a new architectural pattern when an equivalent project pattern already exists.

## 3. Architecture

Target dependency direction:

```text
Presentation
    ↓
Application
    ↓
Domain
    ↑
Infrastructure
```

Domain is framework-independent.

Domain must not depend on:

- Spring
- JPA/Jakarta Persistence
- HTTP
- controllers
- infrastructure SDKs
- Redis
- Elasticsearch

Infrastructure implements technical concerns and domain/application interfaces.

## 4. Bounded Context / Module Structure

A module follows:

```text
modules/<bounded-context>/
├── domain/<aggregate>/
├── application/
│   ├── usecase/
│   ├── command/
│   ├── query/
│   ├── dto/
│   ├── event/
│   └── api/
├── infrastructure/persistence/<aggregate>/
└── presentation/rest/
```

Shared infrastructure lives under:

```text
shared/
├── domain/
├── presentation/
├── infrastructure/
├── outbox/
└── annotation/
```

Do not move domain-specific code into `shared` merely for reuse convenience.

## 5. Domain Rules

Aggregates own business behavior.

Value Objects represent domain concepts that deserve explicit invariants.

Domain services are pure/stateless when behavior does not naturally belong to one aggregate.

Do not make domain classes Spring beans by default.

Use domain repository interfaces in the domain layer.

Repositories must not contain business rules.

## 6. Application Layer

Use cases orchestrate application workflows.

A use case may:

```text
load aggregate
→ invoke domain behavior
→ persist
→ publish event
```

A use case must not become the primary home for domain business rules.

Commands and queries are immutable inputs.

Application DTOs represent module-facing data contracts and must not expose infrastructure entities directly.

## 7. Persistence

Use:

```text
domain repository interface
        ↑
Jpa<Aggregate>Repository
        ↓
Spring Data repository / database
```

JPA entities are infrastructure concerns.

Map between JPA entities and domain aggregates explicitly.

SQL / `@Query` belongs to the persistence repository layer, not the application layer.

Do not expose JPA entities outside infrastructure.

## 8. Inter-Module Communication

A module may consume another module only through its explicit public contract:

```text
<module>.application.api.<Module>PublicApi
<module>.application.dto
```

Never import another module's:

- domain entities
- JPA entities
- repository
- use case
- controller
- private implementation

Cross-module writes should prefer events/outbox when eventual consistency is acceptable and coupling must remain low.

Do not introduce events automatically when the operation requires a strongly consistent synchronous result.

Spring Modulith must remain green after module changes.

## 9. Transaction Rules

Write use cases should define transaction boundaries explicitly.

Existing project convention:

```java
@Transactional(
    isolation = Isolation.DEFAULT,
    rollbackFor = Exception.class,
    propagation = Propagation.REQUIRED
)
```

Read-only queries additionally use `readOnly = true`.

Do not change transaction semantics casually.

Outbox publication for transactional domain changes must occur in the same database transaction as the write that produced the event.

## 10. API / Presentation

Controllers are transport adapters.

Typical flow:

```text
HTTP input
→ bind/validate
→ command/query
→ use case/query handler
→ DTO
→ ApiResponse
```

Controllers should not contain business rules or persistence logic.

Global exception mapping remains in `GlobalExceptionHandler`.

Business errors use the project's semantic error model:

```text
BusinessException
ErrorCodes
ResponseStatus
```

Do not leak framework HTTP concerns into the domain.

## 11. Naming / Code Conventions

Use ubiquitous-language names.

Avoid generic names such as:

```text
Helper
Util
Manager
Common
Misc
```

unless an existing project convention gives the name a precise meaning.

Follow existing explicit-import and Javadoc conventions.

Do not introduce wildcard imports.

## 12. Adding a New Aggregate

Use this sequence:

1. Inspect an existing aggregate with similar behavior.
2. Add domain aggregate/value objects/repository interface.
3. Add persistence entity, mapper, Spring Data repository, and domain repository implementation.
4. Add command/query and application use case.
5. Add application DTO if required.
6. Add controller only if an HTTP contract is required.
7. Add event/outbox only when integration requires it.
8. Update module public API only when another module needs the capability.
9. Update Modulith boundary declarations when required.
10. Add/adjust unit and integration tests.
11. Run the project test suite.

Do not create layers that are not needed by the feature.

## 13. Testing

Expected test levels:

- domain/use-case unit tests with JUnit 5
- integration tests with Testcontainers where infrastructure behavior matters
- Spring Modulith architecture tests
- end-to-end/API tests where required

Do not mock pure domain objects unnecessarily.

Always protect module boundaries with `ModulithTest`.

## 14. Change Safety

Before changing existing code:

- inspect usages
- inspect tests
- inspect module dependencies
- inspect package-info / Modulith declarations
- inspect event contracts if events are involved

When changing a public module contract, update consumers and architecture declarations together.

Never silently broaden a module's allowed dependencies.

## 15. Operational Commands

Fast compile:

```bash
cd backend
.\mvnw.cmd -q -DskipTests compile
```

Full verification:

```bash
.\mvnw.cmd test
```

A change is not considered complete if the required architecture/integration tests fail.

## 16. AI Behavior Rules

When asked to implement a feature:

- first identify the bounded context
- identify the aggregate(s) involved
- identify whether the change is read/write
- identify external-module interactions
- inspect an existing analogous implementation
- implement within the smallest existing architectural boundary
- preserve contracts
- run relevant tests

When uncertain, prefer the existing repository's nearest valid pattern over creating a new abstraction.

Never fabricate architecture, module names, event names, database tables, or APIs not present in the repository or task.
