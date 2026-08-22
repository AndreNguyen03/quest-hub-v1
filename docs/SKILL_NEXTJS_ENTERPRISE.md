---
name: enterprise-nextjs-architecture
description: Production-grade architecture, package boundaries, dependency rules, testing, security, state, data access, observability, and scalability guidance for standalone Next.js repositories using the App Router.
---

# Enterprise Next.js Architecture Skill

## 1. Purpose

Design and review standalone Next.js applications for long-term scalability, maintainability, testability, flexibility, and independent deployment.

This skill is architectural guidance, not a mandatory folder-name template.

Always separate:
- Next.js framework conventions
- application architecture
- domain/business logic
- infrastructure integrations
- deployment concerns

Do not introduce abstractions only because the application is called enterprise.

## 2. Core Principles

### 2.1 Framework boundary

Treat `app/` as the Next.js routing and composition boundary.

Typical responsibilities:
- route segments
- layouts
- loading/error boundaries
- page entry points
- Route Handlers where required
- framework-specific server composition

Keep reusable business logic outside the routing layer when it is non-trivial.

### 2.2 Dependency direction

Prefer a directed dependency graph:

```text
Route / Presentation
        ↓
Feature / Application
        ↓
Domain
        ↑
Infrastructure
```

Rules:
- presentation may depend on feature/application and domain contracts
- application/feature may depend on domain and infrastructure interfaces
- infrastructure may implement application/domain interfaces
- domain must not depend on React, Next.js, UI libraries, HTTP clients, databases, or vendor SDKs

### 2.3 Feature boundaries

Organize significant business capabilities as feature modules:

```text
features/
  authentication/
  orders/
  billing/
  profile/
```

A feature may contain feature-specific components, hooks, application operations, schemas, selectors, state, and tests.

Keep feature-private implementation private. Expose a small public API when cross-feature interaction is required.

### 2.4 Shared code governance

Do not create unrestricted top-level dumping grounds such as `utils/`, `helpers/`, `common/`, or `misc/`.

Keep code local to its feature until reuse is proven. Shared code should be genuinely domain-agnostic and intentionally reusable.

Prefer explicit categories such as:

```text
shared/ui/
shared/date/
shared/formatting/
shared/validation/
```

Do not move business-specific code into shared modules merely to reduce duplication.

## 3. Next.js Runtime Boundaries

### 3.1 Server Components

Use Server Components primarily for server-side composition and data retrieval.

They may:
- access server-side services
- retrieve protected data
- compose presentation
- establish streaming/Suspense boundaries

Avoid placing substantial business workflows or domain rules directly inside page components.

### 3.2 Client Components

Client Components primarily handle interaction, local UI state, browser APIs, client-only hooks, and presentation behavior.

Do not scatter:
- database access
- vendor SDK calls
- raw infrastructure access
- arbitrary network logic

through Client Components.

### 3.3 Server Actions

Treat Server Actions as transport/application entry points.

A Server Action should typically:
1. authenticate/authorize
2. validate input
3. call an application operation
4. normalize expected errors
5. trigger appropriate cache revalidation
6. return a serializable result

Do not make Server Actions the home of reusable business logic.

### 3.4 Route Handlers

Use Route Handlers when an HTTP endpoint is actually required, such as:
- webhooks
- public/internal HTTP APIs
- binary responses
- custom HTTP semantics

Do not create Route Handlers merely to add an unnecessary API layer between a Server Component and backend data.

### 3.5 Middleware

Keep middleware lightweight.

Suitable responsibilities:
- request interception
- redirects
- coarse session/auth checks
- header/routing concerns

Avoid expensive queries, long-running operations, and complex domain workflows.

## 4. Recommended Repository Structure

A scalable standalone repository can evolve toward:

```text
web-app/
├── app/                         # Next.js routing/composition boundary
│   ├── (public)/
│   ├── (auth)/
│   ├── (dashboard)/
│   ├── api/                     # only when HTTP route handlers are required
│   ├── layout.tsx
│   ├── error.tsx
│   ├── loading.tsx
│   └── page.tsx
│
├── src/
│   ├── components/              # domain-agnostic design-system primitives
│   ├── features/                # business capabilities / vertical slices
│   │   ├── authentication/
│   │   ├── orders/
│   │   └── billing/
│   ├── domain/                  # pure business rules where complexity justifies it
│   ├── services/                # application orchestration/use cases
│   ├── infrastructure/          # concrete HTTP, persistence, telemetry, SDK adapters
│   ├── state/                   # client-only global state when required
│   ├── config/                  # validated application configuration
│   └── shared/                  # genuinely reusable, domain-agnostic modules
│
├── tests/
│   ├── unit/
│   ├── integration/
│   └── e2e/
│
├── package.json
├── pnpm-lock.yaml               # or the repository's chosen lockfile
├── tsconfig.json
└── next.config.*
```

This is a target architecture. Do not create every directory on day one.

## 5. Feature Module Rules

Example:

```text
src/features/orders/
├── components/
├── hooks/
├── schemas/
├── operations/
├── selectors/
├── tests/
└── index.ts
```

Rules:
- keep feature-private implementation private
- expose only intentional public capabilities
- avoid cross-feature imports into private files
- move code to shared only after genuine reuse appears
- keep framework-specific code at the boundary where practical

## 6. Domain Layer

Use a domain layer when business rules have enough complexity to justify isolation.

Appropriate content:
- entities
- value objects
- business rules
- calculations
- domain validation
- domain-level contracts

Avoid full DDD/Clean Architecture ceremony for simple CRUD or mostly-presentational screens.

The domain layer should not import:

```text
react
next
browser-only APIs
UI libraries
HTTP clients
database SDKs
analytics SDKs
```

## 7. Data Access

Distinguish these abstractions:

### API/HTTP Client

Low-level transport:
- base URL
- headers
- authentication transport
- timeout
- serialization
- transport errors

### Repository

A data-source abstraction used when it provides real isolation or multiple strategies.

### Application Service

Coordinates a workflow across repositories, domain rules, and side effects.

### Use Case

A focused application operation.

Do not create repository/service/use-case layers for every trivial CRUD operation merely to follow a pattern.

## 8. Runtime Contract Validation

TypeScript does not validate runtime data.

Validate untrusted boundaries such as:
- backend responses
- Server Action payloads
- webhook payloads
- URL/query inputs
- external service responses

A runtime schema library such as Zod may be used as the implementation mechanism.

Keep runtime validation at the boundary where data becomes trusted application data.

## 9. State Architecture

Classify state before selecting a library.

### Local UI state

Examples: modal state, input state, active tab, temporary UI flags.

Prefer local React state.

### Server state

Examples: users, orders, catalog data, remote query results.

Prefer Server Component data fetching where appropriate and a dedicated server-state cache for highly interactive Client Component workflows.

Do not use global client state as a replacement for server-state management.

### Global client state

Use only for genuinely application-wide client-owned state such as theme, active workspace, media player state, or client-owned session UI state.

### Persistent browser state

Use secure server-managed sessions/cookies for authentication. Use browser persistence only for data whose security properties permit client storage.

## 10. Authentication & Security

Prefer server-managed authentication using secure cookies where the backend architecture supports it.

Rules:
- never expose private credentials to Client Components
- never put secrets in public build variables
- do not store sensitive session credentials in browser localStorage
- validate authorization at sensitive mutation boundaries
- treat all client input as untrusted

Middleware checks do not replace authorization inside protected operations.

## 11. Testing Strategy

Use layered testing:

```text
Unit
Integration
Component
Route/Server boundary
E2E
```

### Unit

Test pure business rules, transformations, validators, and deterministic application logic.

### Integration

Test collaboration between application services, repositories, schemas, and transport boundaries using controlled infrastructure.

### Component

Test user-visible behavior and interaction.

### E2E

Test critical user journeys using a staging or dedicated test backend.

Do not maximize mocking. Mock external boundaries when necessary, but execute real application logic.

## 12. Observability

Keep telemetry behind an application-level abstraction when vendor portability matters.

Capture appropriately:
- errors
- request latency
- route failures
- Server Action failures
- important business events
- frontend performance
- release/version context

Do not place vendor SDK calls throughout domain/business code.

## 13. Configuration

Centralize configuration.

Rules:
- validate required configuration at startup
- distinguish public configuration from secrets
- do not scatter environment-variable reads through application code
- expose typed configuration to consumers
- keep environment-specific values outside business logic

## 14. Dependency Governance

Choose the package manager based on team/tooling requirements rather than popularity.

For standalone applications:
- commit the lockfile
- standardize the Node.js version
- separate runtime and development dependencies correctly
- prohibit undeclared/phantom dependencies
- run clean installation and tests in CI

Use architectural linting when boundaries are mature enough to justify enforcement.

## 15. Architecture Evolution

### Stage 1 — Small application

Use:

```text
app/
src/features/
src/components/
```

Keep abstractions light.

### Stage 2 — Growing product

Introduce:
- stronger feature boundaries
- runtime contract validation
- explicit infrastructure adapters where useful
- dependency-boundary linting

### Stage 3 — Multi-team enterprise

Introduce:
- explicit domain ownership
- public feature interfaces
- stricter dependency rules
- stronger automated architectural checks

### Stage 4 — Physical extraction

Extract separate applications/services only when justified by:
- independent deployment
- independent scaling
- security isolation
- organizational ownership
- materially different runtime requirements

Do not introduce micro-frontends merely because the codebase is large.

## 16. Anti-Patterns

Reject or refactor:
- God components
- God hooks
- God stores
- API calls scattered throughout UI
- business logic inside page components
- direct vendor SDK access from presentation
- global state for server-owned data
- giant `utils/`
- giant `components/`
- excessive repositories/use cases
- premature Clean Architecture
- cross-feature private imports
- circular dependencies
- direct environment access throughout the codebase

## 17. Decision Procedure

When designing or reviewing a Next.js project:
1. Identify the runtime boundary.
2. Identify business capabilities/features.
3. Keep framework routing concerns at `app/`.
4. Keep non-trivial business logic outside route composition.
5. Classify state before selecting state management.
6. Classify external integrations as infrastructure.
7. Validate untrusted boundaries.
8. Define dependency direction.
9. Enforce boundaries when the codebase/team justifies it.
10. Add abstractions only when they reduce coupling or improve testability.
11. Prefer incremental architectural evolution over speculative enterprise complexity.

## 18. Non-Goals

This skill does not mandate:
- a specific state library
- a specific HTTP client
- a specific CSS solution
- a specific auth provider
- a specific observability vendor
- micro-frontends
- full Clean Architecture
- DDD for every feature
- a monorepo

Architecture decisions must follow application requirements, not tool popularity.
