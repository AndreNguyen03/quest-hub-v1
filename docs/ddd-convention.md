# DDD Architecture Convention for AI-Assisted Development

## Purpose

This document defines the architectural rules, package organization, naming conventions, and development principles for Domain-Driven Design (DDD) projects.

The goal is to provide a consistent structure that is understandable by both developers and AI coding assistants.

---

# AI Interpretation Rules

This document defines architectural conventions only.

All package names, aggregate names, entity names, repository names, service names, and events appearing in examples are placeholders.

The AI must:

1. Infer the domain model from the current bounded context.
2. Use the ubiquitous language of the current context.
3. Preserve architectural conventions.
4. Never reuse example names unless they actually exist in the current context.
5. Treat all examples as illustrative only.

Examples demonstrate structure, not domain terminology.

---

# Core Principles

## Business First

The domain model is the center of the system.

Business concepts drive the architecture.

Packages should communicate business meaning rather than technical implementation details.

Preferred:

```text
customer
order
payment
inventory
```

Avoid:

```text
entity
model
data
object
common
```

---

## Framework Independence

The Domain Layer must remain independent from frameworks.

Forbidden inside Domain:

```java
@RestController
@Service
@Repository
@Entity
@Component
```

The domain should remain pure Java.

---

## Explicit Ubiquitous Language

All names must originate from the business language of the current bounded context.

Use domain terminology.

Avoid technical naming.

Preferred:

```java
<Customer>
<Order>
<Invoice>
```

Avoid:

```java
CustomerData
OrderRecord
InvoiceTable
```

---

# Layer Architecture

```text
Presentation
      ↓
Application
      ↓
Domain

Infrastructure
      ↓
Domain
```

Dependency rule:

```text
Presentation → Application → Domain

Infrastructure → Domain
```

Forbidden:

```text
Domain → Application
Domain → Infrastructure
Domain → Framework
```

---

# Bounded Context Structure

Each bounded context owns its own model and implementation.

```text
com.company.project

├── <bounded-context>
├── <bounded-context>
├── <bounded-context>
└── <bounded-context>
```

Each bounded context owns:

* Domain Model
* Application Logic
* Persistence Implementation
* API Layer

No entity sharing between bounded contexts.

Communication occurs through contracts, events, or APIs.

---

# Context Structure

```text
<bounded-context>

├── domain
│
├── application
│
├── infrastructure
│
└── presentation
```

---

# Domain Layer

The Domain Layer contains business rules.

---

## Domain Package Convention

### Rule

Organize packages by business capability and aggregate boundary.

Do not organize packages by technical type.

Preferred:

```text
domain

├── <aggregate>
├── <aggregate>
├── service
├── event
└── exception
```

Avoid:

```text
domain

├── model
├── entity
├── repository
├── aggregate
└── valueobject
```

Business language always takes priority over technical classification.

---

## Aggregate Package Structure

Each aggregate package may contain:

```text
<aggregate>

├── <AggregateRoot>
├── <Entity>
├── <Entity>
├── <ValueObject>
├── <ValueObject>
└── <Repository>
```

Everything belonging to the same aggregate should remain together.

---

## Entity

An Entity has identity and behavior.

Rules:

* Has identity
* Encapsulates behavior
* Protects invariants
* Avoids public setters

Preferred:

```java
aggregate.activate();
aggregate.complete();
aggregate.publish();
```

Avoid:

```java
aggregate.setStatus(ACTIVE);
```

---

## Aggregate Root

Aggregate Root is the only entry point to an aggregate.

External objects interact only with the Aggregate Root.

Allowed:

```java
aggregate.performBusinessOperation();
```

Forbidden:

```java
childEntity.performBusinessOperation();
```

---

## Value Object

Represents immutable concepts.

Rules:

* Immutable
* Equality by value
* No identity

Naming:

```java
<Concept>
```

Avoid:

```java
<Concept>VO
```

---

## Repository

Repository interfaces belong to the Domain Layer.

Example:

```java
public interface <Aggregate>Repository {
}
```

Responsibilities:

* Persist aggregates
* Retrieve aggregates

Repository interfaces live inside the aggregate package.

---

## Domain Event

Represents business facts that already happened.

Naming:

```java
<BusinessAction>
```

Examples:

```java
<AccountCreated>
<OrderCompleted>
```

Avoid:

```java
<AccountCreatedEvent>
```

when the event already resides inside an event package.

---

## Domain Service

Used when business logic does not naturally belong to a specific entity.

Responsibilities:

* Business calculations
* Business decisions
* Business validations

Examples:

```java
calculateReward(...)
calculateRanking(...)
calculateDiscount(...)
```

Domain Services must not:

```java
save(...)
publish(...)
callExternalApi(...)
```

Domain Services contain business rules only.

---

# Application Layer

The Application Layer orchestrates business workflows.

Contains:

```text
application

├── usecase
├── command
├── query
├── dto
└── mapper
```

No business rules should be implemented here.

---

## Use Case

Represents a user intention or business workflow.

Examples:

```java
CreateSomethingUseCase
UpdateSomethingUseCase
CompleteSomethingUseCase
```

Responsibilities:

* Load aggregates
* Invoke domain behavior
* Coordinate domain services
* Persist aggregates
* Publish events
* Manage transactions

Use Cases answer:

> What should the system do?

---

## Command

Represents a write request.

Rules:

* Immutable
* No behavior
* Input data only

Examples:

```java
CreateSomethingCommand
UpdateSomethingCommand
```

Command answers:

> What data should be changed?

---

## Query

Represents a read request.

Rules:

* Immutable
* Read-only
* No side effects

Examples:

```java
GetSomethingQuery
SearchSomethingQuery
ListSomethingQuery
```

Query answers:

> What data should be retrieved?

---

## DTO

Represents data transferred between layers.

Rules:

* No business logic
* Serialization-friendly
* Application concern only

Examples:

```java
SomethingDetailDto
SomethingSummaryDto
```

---

# Infrastructure Layer

Contains technical implementations.

Examples:

```text
infrastructure

├── persistence
├── messaging
├── cache
├── security
└── configuration
```

---

# Persistence Convention

Persistence packages are organized by aggregate persistence concern.

Preferred:

```text
persistence

├── <aggregate>
├── <aggregate>
└── <aggregate>
```

Avoid:

```text
persistence

├── entity
├── repository
├── mapper
```

at the root level.

---

## Small Aggregate

```text
persistence

└── <aggregate>

    ├── <AggregateJpaEntity>
    ├── SpringData<Aggregate>Repository
    ├── Jpa<Aggregate>Repository
    └── <Aggregate>PersistenceMapper
```

---

## Large Aggregate

```text
persistence

└── <aggregate>

    ├── entity
    ├── repository
    └── mapper
```

Create subfolders only when the aggregate becomes difficult to navigate.

---

## Repository Implementation Naming

Pattern:

```text
<Technology><Aggregate>Repository
```

Examples:

```java
JpaCustomerRepository
MongoCustomerRepository
RedisCustomerRepository
```

---

## Persistence Entity Naming

Pattern:

```java
<Aggregate>JpaEntity
```

Examples:

```java
CustomerJpaEntity
OrderJpaEntity
```

Avoid:

```java
CustomerEntity
OrderEntity
```

because they are easily confused with domain entities.

---

# Presentation Layer

Responsible for exposing APIs.

Examples:

```text
REST
GraphQL
WebSocket
gRPC
```

Responsibilities:

* Validation
* Authentication
* Request mapping
* Response mapping
* Calling Use Cases

Business logic is forbidden in Presentation.

---

# Naming Conventions

## Aggregate Root

```java
<AggregateRoot>
```

---

## Entity

```java
<Entity>
```

---

## Value Object

```java
<ValueObject>
```

---

## Repository

```java
<Aggregate>Repository
```

---

## Domain Service

```java
<BusinessCapability>DomainService
```

---

## Use Case

```java
<Action>UseCase
```

---

## Command

```java
<Action>Command
```

---

## Query

```java
<Get|Search|List><Something>Query
```

---

## DTO

```java
<Something>Dto
```

---

## Event

```java
<BusinessAction>
```

---

# Constructor Rule

Entities must never be created in an invalid state.

Preferred:

```java
Aggregate.create(...)
```

or

```java
new Aggregate(...)
```

with all mandatory fields provided.

Avoid:

```java
new Aggregate();
```

---

# Setter Rule

Avoid public setters.

Preferred:

```java
aggregate.changeName(...);
aggregate.activate();
aggregate.complete();
```

Avoid:

```java
aggregate.setName(...);
aggregate.setStatus(...);
```

Business behavior should be expressed through domain methods.

---

# Final Convention

Priority order:

```text
Bounded Context
    ↓
Aggregate
    ↓
Entity
```

Never:

```text
Bounded Context
    ↓
Technical Type
    ↓
Entity
```

Decision:

1. Business Capability First.
2. Aggregate Boundary Second.
3. Technical Classification Third.
4. Framework Independence Always.
5. Domain Rules Stay Inside the Domain.
