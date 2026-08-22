---
name: enterprise-react-native-architecture
description: Production-grade architecture, package boundaries, dependency rules, native-platform isolation, state, persistence, security, testing, observability, and scalability guidance for standalone React Native repositories.
---

# Enterprise React Native Architecture Skill

## 1. Purpose

Design and review standalone React Native applications for long-term scalability, maintainability, testability, native-platform flexibility, and independent mobile release management.

Prefer modern Expo-based architecture when project requirements are compatible with it, while keeping application architecture independent from any single SDK vendor.

This skill is architectural guidance, not mandatory boilerplate.

## 2. Core Principles

### 2.1 Navigation boundary

Treat the router directory as the navigation/composition boundary.

With Expo Router:

```text
app/
```

contains route declarations and navigation composition.

Keep business rules outside the route tree.

A route should primarily:
- resolve navigation parameters
- compose a screen
- establish navigation/layout boundaries
- invoke feature-level behavior

### 2.2 Dependency direction

Prefer:

```text
Navigation / UI
        ↓
Feature / Application
        ↓
Domain
        ↑
Infrastructure / Platform
```

Rules:
- UI can depend on feature/application APIs
- feature/application can depend on domain contracts
- infrastructure implements application/domain interfaces
- domain has no React Native dependency
- platform-specific code stays at the infrastructure/platform boundary

## 3. Recommended Repository Structure

A scalable standalone repository can evolve toward:

```text
mobile-app/
├── app/                          # Expo Router navigation boundary
│   ├── (auth)/
│   ├── (tabs)/
│   ├── _layout.tsx
│   └── index.tsx
│
├── src/
│   ├── components/               # domain-agnostic design-system primitives
│   ├── features/                 # business capabilities / vertical slices
│   │   ├── authentication/
│   │   ├── orders/
│   │   └── profile/
│   ├── domain/                   # pure TypeScript business rules
│   ├── services/                 # application workflows/orchestration
│   ├── infrastructure/           # network, persistence, telemetry, external SDK adapters
│   ├── platform/                 # iOS/Android-specific bridges
│   ├── state/                    # client-owned global state
│   ├── config/                   # validated application configuration
│   └── shared/                   # genuinely reusable, domain-agnostic modules
│
├── tests/
│   ├── unit/
│   ├── integration/
│   └── e2e/
│
├── app.json / app.config.*
├── package.json
├── tsconfig.json
└── lockfile
```

Do not create every directory automatically. Add a boundary when the codebase has a real responsibility for it.

## 4. Feature Modules

Example:

```text
src/features/orders/
├── screens/
├── components/
├── hooks/
├── operations/
├── schemas/
├── state/
├── tests/
└── index.ts
```

Keep feature-private implementation private.

Cross-feature communication should use explicit public capabilities rather than private-file imports.

Do not allow `features/` to become another technical layer. Each feature should represent a meaningful business capability.

## 5. Navigation vs Screen Architecture

Separate:

```text
app/
```

from:

```text
features/*/screens/
```

The route tree decides:
- which screen is reachable
- navigation structure
- tabs/stacks/groups
- route parameters

Feature screen components decide:
- feature UI composition
- feature hooks
- feature interaction
- application operation invocation

## 6. Domain Layer

Use pure TypeScript for business rules that benefit from framework independence.

Allowed:
- entities
- value objects
- business rules
- calculations
- state transformations
- domain validation

Prohibited dependencies:

```text
react
react-native
expo-router
UI libraries
native SDKs
storage SDKs
HTTP clients
analytics SDKs
```

Do not build formal DDD aggregates or ports for trivial CRUD screens.

## 7. Application Services

Application services coordinate workflows such as:
- checkout
- authentication
- synchronization
- offline mutation processing
- push-notification handling
- complex multi-step operations

They may depend on domain logic and infrastructure interfaces.

They should not own screen rendering.

## 8. Native Infrastructure Boundary

Isolate platform capabilities such as:

```text
Secure storage
SQLite
Push notifications
Camera
Biometrics
Location
Background tasks
Deep links
Analytics
Crash reporting
Network transport
```

Wrap vendor/native SDKs behind adapters when vendor replacement, testability, or policy isolation matters.

Do not invoke native SDKs directly from domain logic.

## 9. Platform-Specific Code

Use a dedicated platform boundary when behavior genuinely differs by OS.

Possible approaches:

```text
feature.ts
feature.ios.ts
feature.android.ts
```

or:

```text
platform/
├── ios/
├── android/
└── shared/
```

Keep platform branching near the capability that differs.

Do not spread `Platform.OS` checks throughout business logic.

## 10. Expo vs Bare React Native

Prefer Expo/CNG when project requirements are compatible because it can simplify native configuration and build reproducibility.

Use bare/native customization when required by:
- unsupported native capabilities
- incompatible legacy native libraries
- specialized native integration
- organization-specific native infrastructure

This is a runtime/build decision, not an application-domain architecture decision.

## 11. State Architecture

Classify state first.

### Local UI state

Examples: modal state, input values, tab selection, transient presentation flags.

Prefer local React state unless sharing is actually required.

### Server state

Examples: profile data, orders, catalog, remote query results.

Use a dedicated server-state mechanism where appropriate, especially for caching, refetching, reconnect behavior, and mutation handling.

Avoid copying remote entities into a global client store solely for convenience.

### Global client state

Examples: theme, active workspace, media player, client-owned session flags.

Use a lightweight global store only when state is genuinely application-wide.

### Persistent/offline state

Separate:
- secure credentials
- preferences
- server cache
- offline domain data
- offline mutation queues

## 12. Persistence & Offline Architecture

Do not store all persistence concerns in one storage mechanism.

Use secure OS-backed storage for sensitive credentials.

Use an appropriate local database for structured offline data.

Use a server-state cache for server-owned data rather than treating the database as a generic global state store.

Offline synchronization should have explicit rules:

```text
enqueue mutation
→ persist operation
→ retry
→ resolve conflict
→ update local/server state
```

Do not hide complex offline synchronization inside UI hooks.

## 13. API & Repository Architecture

### HTTP Client

Owns transport mechanics:
- base URL
- authentication transport
- timeout
- parsing
- network failures
- retry policy where appropriate

### Repository

Owns access to application data sources when an abstraction provides real value.

Possible sources include:
- REST
- GraphQL
- local SQLite
- cache
- remote + local hybrid

### Application Service

Coordinates business workflows.

Do not create repositories and services for every simple endpoint automatically.

## 14. Runtime Contract Validation

Validate external data at runtime.

Important boundaries:
- API responses
- deep-link payloads
- push notification payloads
- persisted data migrations
- external SDK responses
- feature configuration

TypeScript compile-time types do not validate runtime payloads.

A schema tool such as Zod may be used where appropriate.

## 15. Authentication & Security

Rules:
- use secure OS-backed storage for sensitive authentication credentials
- use OAuth 2.0 Authorization Code + PKCE for suitable public-client flows
- treat deep-link data as untrusted input
- validate authentication responses before updating application state
- implement token refresh/rotation according to backend protocol
- avoid persistent secrets in ordinary key/value storage
- never embed backend private credentials in the mobile application

Do not confuse client obfuscation with secrecy. Anything shipped to the mobile client should be treated as potentially observable.

## 16. Testing Strategy

Use layered tests.

### Unit

Test domain logic, transformations, validators, and deterministic application logic.

### Integration

Test application services, repositories, schema validation, persistence integrations, and API boundaries.

### Component

Use React Native Testing Library for user-visible behavior and interaction.

### Native integration

Test capabilities such as deep links, notifications, camera, biometrics, permissions, and platform-specific behavior.

### E2E

Use a device/emulator-based solution such as Maestro or Detox when full mobile journeys must be validated.

Prefer staging/test backend environments rather than replacing the entire system with mocks.

## 17. Observability

Treat telemetry as infrastructure.

Capture where useful:
- crashes
- uncaught JS errors
- native crashes/ANRs
- network errors
- API latency
- important business events
- app version
- build number
- OS version
- device context

Do not scatter direct calls to one vendor SDK throughout feature and domain code when portability matters.

## 18. Configuration

Centralize and validate configuration.

Separate:

```text
public application configuration
environment/build configuration
secrets
runtime feature flags
```

Do not scatter environment-variable access through components, hooks, and domain logic.

## 19. Package & Dependency Governance

For a standalone mobile repository:
- commit the lockfile
- standardize Node/package-manager versions
- separate runtime and development dependencies
- keep native packages aligned with the selected React Native/Expo toolchain
- remove unused packages
- review native compatibility before upgrades
- reproduce builds in CI

Do not adopt a package solely because it is popular.

## 20. Cross-Repository API Contract

The web and mobile repositories are intentionally independent.

Prefer an API contract as the synchronization boundary:

```text
Backend
   ↓
OpenAPI / Protobuf contract
   ↓
Web client generation
   ↓
Mobile client generation
```

A versioned private type package can be an alternative when appropriate.

Avoid:
- Git submodules for routine application synchronization
- manual copy/paste of API models
- duplicated contract definitions that drift silently

The mobile repository must not depend on the web repository.

## 21. Architecture Evolution

### Stage 1 — Initial application

Use:

```text
app/
src/features/
src/components/
```

Keep abstractions light.

### Stage 2 — Growing application

Introduce:
- explicit feature boundaries
- runtime validation
- infrastructure isolation
- dedicated server-state management
- stronger testing boundaries

### Stage 3 — Multi-team application

Introduce:
- strict feature ownership
- explicit public feature APIs
- architectural linting
- stronger infrastructure/platform boundaries
- independent feature testing

### Stage 4 — Multiple applications/products

Extract separate deployable modules only when justified by:
- independent release cadence
- independent scaling
- security isolation
- organizational ownership
- product boundary

Do not create multiple mobile applications merely because one repository is large.

## 22. Anti-Patterns

Reject or refactor:
- God screens
- God hooks
- God stores
- API calls spread through screen components
- native SDK calls inside business logic
- global store containing every API response
- giant `utils/`
- giant shared component folders
- uncontrolled `Platform.OS` branching
- business logic inside navigation route files
- excessive repository/use-case ceremony
- premature Clean Architecture
- cross-feature private imports
- duplicated API contracts
- treating mobile storage as secure by default
- embedding private backend secrets into the application

## 23. Decision Procedure

When designing or reviewing a React Native application:
1. Establish the navigation boundary.
2. Identify meaningful business features.
3. Separate feature UI from navigation declarations.
4. Classify state.
5. Isolate native/platform capabilities.
6. Isolate persistence and transport.
7. Validate untrusted runtime boundaries.
8. Define dependency direction.
9. Keep domain logic framework-independent where it provides value.
10. Add architectural enforcement as complexity grows.
11. Evolve incrementally instead of starting with maximum ceremony.

## 24. Non-Goals

This skill does not mandate:
- Expo for every project
- a specific navigation library
- Redux
- Zustand
- TanStack Query
- Axios
- SQLite
- a specific analytics provider
- a specific crash-reporting provider
- Clean Architecture for every screen
- DDD for every domain
- a monorepo

Choose tools after architecture and runtime requirements are understood.
