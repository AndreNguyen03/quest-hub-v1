---
name: go-backend
description: Project-specific implementation skill for a Go/Gin backend. Apply when modifying this Go/Gin codebase. Preserve its routing, controller, service, repository, infrastructure, dependency-injection, validation, response, job, WebSocket, and configuration conventions.
---

# Go Backend — AI Implementation Skill

## 1. Scope

Apply this skill to the Go/Gin backend.

Repository context:

- Go 1.23
- Gin HTTP API
- PostgreSQL + GORM
- Redis
- manual constructor-based dependency injection
- Asynq background jobs
- gocron scheduling
- gorilla/websocket
- project-specific integrations under `infra/`

This is a **project-specific skill**. Do not treat its structure as a universal Go standard.

## 2. Priority Rules

When modifying the repository:

1. Preserve the existing request flow.
2. Preserve existing package responsibilities.
3. Inspect analogous code before creating new code.
4. Follow established constructor/factory patterns.
5. Keep infrastructure dependencies behind the appropriate package boundary.
6. Make the smallest coherent change.
7. Do not restructure unrelated packages.

Do not introduce a new framework or DI container without an explicit requirement.

## 3. Request Architecture

Expected flow:

```text
HTTP Request
    ↓
Router / Middleware
    ↓
Controller
    ↓
Service
    ↓
Repository
    ↓
Database / Redis
```

Infrastructure-specific capabilities such as workers, WebSocket, messaging, storage, push, or third-party integrations remain under `infra/`.

## 4. Repository Structure

Current project structure:

```text
<repo-root>/
├── main.go
├── router/
├── middleware/
├── controller/
├── service/
├── repository/
├── schemas/
├── infra/
├── helper/
├── util/
├── docs/
├── fonts/
├── Makefile
├── Dockerfile
└── docker-compose.yaml
```

Responsibilities:

### `router/`

- route registration
- middleware attachment
- route grouping

### `middleware/`

- authentication
- authorization
- request-level cross-cutting concerns

### `controller/`

- bind request
- validate input
- invoke service
- convert service result to HTTP response

Controllers must not contain business workflows or persistence logic.

### `service/`

- application/business workflow orchestration
- coordinate repositories and infrastructure interfaces
- own service-level decisions

### `repository/`

- persistence access
- database/cache queries
- repository interfaces and implementations according to current project conventions

### `schemas/`

- request/response DTOs
- transport-facing validation models

### `infra/`

Technical integrations such as:

```text
db
ws
task
worker
bucket
fcm
agora
fpt
otp
crawler
rabbitmq
```

### `helper/`

Use only for project-approved cross-cutting response/conversion helpers.

Do not create new generic dumping-ground helpers.

### `util/`

Existing technical utilities such as configuration, logging, encryption, token handling, and specialized transformations.

Do not move business logic into `util`.

## 5. Dependency Injection

Current project uses manual constructor injection.

Preferred pattern:

```go
func NewRepository(db *gorm.DB, redis *redis.Client) IRepository
func NewService(repo IRepository) IService
func NewController(service IService) *Controller
```

Repository and service containers/factories remain part of the current project convention.

When adding a component:

1. define its interface when required by the current layer pattern
2. implement the concrete type
3. add constructor
4. register it in the appropriate container/factory
5. inject dependencies explicitly
6. wire it into the application bootstrap

Do not introduce reflection-based DI unless explicitly required.

## 6. Interfaces

Use interfaces at dependency boundaries where consumers depend on behavior rather than implementation.

The current repository commonly defines service/repository interfaces beside their implementations.

Follow this existing convention.

Avoid creating interfaces for:

- one-use internal helpers
- data structures
- trivial pure functions

when no substitution or boundary benefit exists.

## 7. Validation

Controller flow:

```text
Bind
→ Validate
→ Service
```

Validate input before invoking business logic.

Keep transport validation concerns in controller/schema boundaries.

Do not rely on controller validation alone for business invariants.

## 8. Response / Error Convention

Use the project's response helpers.

Success/error responses remain consistent with:

```text
helper.GinResponse(...)
helper.ErrorResponseWithMessage(...)
```

User-facing error messages remain in Vietnamese according to current project convention.

Do not expose internal stack traces or infrastructure errors directly to clients.

## 9. Domain / Business Logic

Business workflows belong in services rather than controllers.

Repositories should not become a second service layer.

Keep database query mechanics in repositories.

Keep application/business decisions in services.

Avoid generic `helper` or `util` functions that contain domain behavior.

## 10. Database / Cache

Use GORM/repository boundaries for persistence.

Keep transaction handling close to the service/use-case that owns the workflow.

Redis should be used through the established repository/infrastructure boundary appropriate to the operation.

Do not access database clients directly from controllers.

## 11. Background Jobs

Background work uses the existing Asynq/gocron architecture.

Separate:

```text
task definition
worker/processor
business service
```

A task should invoke the same application/service logic used by other entry points where appropriate.

Do not duplicate business logic inside worker handlers.

## 12. WebSocket

WebSocket concerns remain under the existing `infra/ws` boundary.

Keep:

- connection handling
- hub/client lifecycle
- transport behavior

separate from business services.

A WebSocket handler should call application/service logic rather than embedding business rules.

## 13. External Integrations

Third-party integrations such as FCM, Cloudinary, Agora, Twilio, RabbitMQ, and AI providers belong behind infrastructure-specific packages.

Do not spread vendor SDK calls throughout controllers/services when the current project already provides an integration boundary.

Keep credentials/configuration outside business logic.

## 14. Configuration

Use the repository's existing configuration mechanism.

Do not scatter environment-variable parsing throughout application code.

Configuration should be loaded during bootstrap and passed into components through constructors/config objects as appropriate.

## 15. Time

Current project convention uses UTC.

Preserve UTC semantics for:

- persistence
- jobs
- scheduling
- API serialization

Do not introduce local-time assumptions into business logic.

## 16. Documentation / API

Maintain Swagger annotations on HTTP handlers according to the existing project standard.

Regenerate Swagger artifacts when API contracts change.

Exported Go identifiers require appropriate Go documentation.

## 17. Adding a New Feature / Domain

Use this sequence:

1. inspect an analogous existing feature
2. add schema models
3. add repository interface/implementation
4. add service interface/implementation
5. register repository/service in current containers/factories
6. add controller
7. add router registration
8. attach correct middleware
9. add infrastructure integration only if required
10. update Swagger if API changed
11. test the complete request path

Do not create a new package hierarchy for a simple feature if the repository already has an established place for it.

## 18. Testing

At minimum test the relevant boundary:

- service unit tests
- repository/integration tests where persistence behavior matters
- HTTP/controller tests where request/response behavior matters
- worker/task tests where background behavior matters
- WebSocket tests where protocol behavior matters

Prefer testing business behavior at the service boundary instead of testing only controllers.

## 19. AI Behavior Rules

When asked to implement a feature:

- determine the request entry point
- identify the service responsible for the workflow
- identify required repository/infrastructure dependencies
- inspect a nearby analogous implementation
- follow the existing factory/container wiring
- preserve response/error conventions
- preserve middleware/security requirements
- update Swagger when contracts change
- run relevant tests

Never fabricate:

- routes
- database tables
- repository fields
- external integrations
- token formats
- worker names

when the repository/task does not establish them.

## 20. Operational Commands

```bash
go mod download
cp app.env.example app.env
make dev_server
curl http://127.0.0.1:8080/health_check
```

Use the project's existing Makefile/tasks before introducing alternate commands.

## 21. Non-Goals

This skill does not establish a universal Go architecture.

It does not mandate:

- Clean Architecture
- Hexagonal Architecture
- DDD
- a new DI framework
- microservices
- generic `internal/` restructuring
- a different ORM
- a different web framework

Preserve the repository's current architecture unless the task explicitly requests an architectural change.
