# QuestHub — Tech Stack

Decisions, trade-offs, và open questions đã được resolve

---

## Architecture Strategy — 2 Phases

### Phase 1 · Modular Monolith + DDD

**1 Spring Boot app, nhiều Bounded Contexts**

Toàn bộ backend là 1 deployable unit. Bên trong được tổ chức thành modules theo DDD: `identity/`, `quest/`, `marketplace/`, `world/`, `social/`, `notification/`, `admin/`. Mỗi module có 4 layer: **domain → application → infrastructure → presentation**. Modules không import nhau trực tiếp — giao tiếp qua Domain Events.

**DDD Layers per Module**

- `domain/` — Entities, Value Objects, Domain Events, Repository interfaces (không phụ thuộc framework)
- `application/` — Use Cases / Application Services, Event Handlers
- `infrastructure/` — JPA implementations, Outbox publisher
- `presentation/` — REST Controllers

Dependency chỉ đi 1 chiều: presentation → application → domain.

**Cross-module communication via Outbox**

Khi Quest Module hoàn thành một task, nó INSERT vào `outbox_events` trong cùng 1 DB transaction. Outbox Relay (`@Scheduled`) poll bảng này và publish `ApplicationEvent` tới World/Social/Notification modules. At-least-once delivery, không bao giờ mất event.

**Tại sao Monolith trước?**

Microservices không giải quyết vấn đề — chúng chia vấn đề ra. Bắt đầu bằng Monolith giúp hiểu rõ domain boundaries trước khi tách. Martin Fowler: *"Don't start with microservices — start by building the monolith."* Khi thấy module nào cần scale độc lập → tách ra lúc đó.

---

### Phase 2 · Microservices (future)

**Extract theo module boundaries**

Khi Phase 1 hoạt động tốt, tách từng module thành service riêng. Outbox Relay thay `ApplicationEvent` bằng `RabbitTemplate.send()` — transaction logic không thay đổi. Thêm API Gateway (Go/Fiber). AI Service (Python/FastAPI) được tách ra từ đầu vì độc lập về runtime.

**Học được gì từ quá trình tách?**

Distributed transactions, saga pattern, eventual consistency, network failures, service discovery, circuit breaker. Hiểu *tại sao* microservices khó — không chỉ *cách* làm. Go/Python services được thêm khi cần high concurrency hay AI runtime.

---

## Stack Overview

### Frontend

**Next.js** `14 App Router`
SSR cho Marketplace và Quest Detail pages — SEO critical. App Router + React Server Components giảm client JS bundle.

**React Native + Expo** `SDK 51`
Cross-platform iOS/Android. Expo giảm native config overhead. Focus trên daily tracking UX — phù hợp mobile-first usage pattern.

**Tailwind CSS**
Utility-first, consistent với design tokens. Không cần CSS-in-JS runtime. Web only.

**TanStack Query**
Server state management, cache, optimistic updates. Đặc biệt hữu ích cho progress tracking (real-time-ish feel).

---

### Backend — Java (main services)

**Java 21 + Spring Boot** `3.x`
Main language cho Quest, Marketplace, World, Identity, Admin services. Virtual Threads (Project Loom) giảm overhead concurrency. Strong typing, mature ecosystem, enterprise-grade.

**Spring Security + JJWT**
JWT authentication/authorization. Method-level security (@PreAuthorize). OAuth2 resource server support. Mature, battle-tested.

**Spring Data JPA + Hibernate + Flyway**
JPA cho CRUD. Native queries khi cần performance. Flyway cho DB migrations — versioned SQL files, reproducible schema.

**Spring AMQP (RabbitMQ)**
Chuyển từ BullMQ sang RabbitMQ — học proper message broker. Dead-letter queues, retry policies, routing keys. Java ecosystem tích hợp tốt hơn.

---

### Backend — Go (high-concurrent services)

**Go 1.22 + Fiber** `v2`
**API Gateway** — xử lý mọi incoming request, JWT verify, rate limiting, routing. Go goroutines handle concurrent connections cực tốt với memory footprint thấp.

**Go — Notification Service**
Fan-out notifications tới nhiều users cùng lúc (followers nhận event). Go channel pattern phù hợp — mỗi notification là goroutine nhẹ. Thấy rõ sự khác biệt với Java threads.

**Go — Social/Feed Service**
Feed aggregation từ nhiều users đồng thời — concurrent fan-in pattern. goroutine per follower, merge results. Học cách Go xử lý I/O-bound concurrent workloads.

---

### Backend — Python (AI service)

**Python 3.12 + FastAPI**
AI Service — quest recommendation, generation, submission grading (AI Grader), AI Coach (read-only agent). FastAPI async + auto OpenAPI docs + StreamingResponse cho coach chat. Python là ngôn ngữ tự nhiên cho AI/ML tooling.

**Anthropic Python SDK**
Claude API integration. Streaming responses + **tool calling** cho AI Coach (get_progress / get_streak / get_achievements / get_upcoming_tasks — read-only). Prompt engineering dễ iterate trong Python. Học cách build production AI features.

**LangChain (optional)**
Nếu cần RAG hoặc chain phức tạp hơn (vd: coach trả lời dựa trên nội dung quest — embeddings + vector search). Bắt đầu plain SDK + native tool calling; thêm LangChain khi cần abstraction.

---

### Message Broker

**RabbitMQ** `3.x`
Thay BullMQ. Proper message broker với Exchange → Queue routing (topic, fanout, direct). Persistent messages, acknowledgment, dead-letter queue. Học AMQP protocol thực sự.

**Exchange patterns**
- **Topic exchange** cho event routing: `quest.completed`, `quest.forked` → consumers subscribe theo pattern.
- **Fanout** cho notification broadcast.
Học khi nào dùng loại nào.

---

### Data Layer

**PostgreSQL** `16`
Single DB cho learning project. JSONB cho activity payload. Row-level security khi cần. Strong consistency cho quest/completion data.

**Redis** `7.x via Upstash`
Cache trending/popular lists, refresh tokens, feature flags, search results. Upstash = serverless Redis, không cần manage instance.

**Elasticsearch** `8.x via Bonsai`
Full-text search với relevance ranking. Bonsai = managed ES, free tier đủ cho MVP. Sync từ PostgreSQL qua QuestPublished event.

**RabbitMQ** (message broker)
Message queue chính — topic/fanout/direct exchanges, routing keys, DLQ, persistent messages. Phase 1: Outbox Relay publish Spring ApplicationEvent; Phase 2: Relay gửi qua RabbitTemplate. Không dùng BullMQ nữa.

---

### Container & Orchestration

**Docker + Docker Compose**
Mỗi service có Dockerfile riêng. docker-compose.yml spin up toàn bộ stack local: Java services, Go services, Python AI, Postgres, Redis, RabbitMQ, Elasticsearch, Prometheus, Grafana — 1 lệnh.

**Kubernetes (k3s local / EKS staging)**
Học production orchestration thực sự. k3s trên local hoặc VM nhẹ. Deployment, Service, Ingress, ConfigMap, Secret, HPA. Mỗi microservice là 1 Deployment + Service.

**Helm Charts**
Package K8s manifests thành charts. Học cách template K8s configs cho nhiều environments (dev/staging/prod). Dùng community charts cho Postgres, Redis, RabbitMQ, Elasticsearch.

**GitHub Actions (CI/CD)**
Build Docker images → push DockerHub/GHCR → deploy to K8s via kubectl. PR → run tests → build → deploy to staging. Merge main → deploy to prod. Học GitOps flow.

---

### Kubernetes Management Tools

**Rancher**
GUI platform quản lý cluster. Import k3s cluster vào Rancher để có UI đầy đủ: deploy workloads, xem pods/logs, RBAC, resource quotas. Cách hầu hết team production quản lý K8s thực tế — không ai `kubectl` thuần.

**k9s**
Terminal UI cho K8s — developer's daily driver. Navigate pods, xem logs realtime, exec vào container, describe resources, port-forward — tất cả bằng keyboard. Nhanh hơn `kubectl` rất nhiều cho debug hàng ngày.

**Lens / OpenLens**
Desktop app GUI cho K8s (alternative của Rancher cho local dev). Connect nhiều cluster, xem resource metrics inline, terminal trong pod. Lens là commercial, OpenLens là open-source fork.

**ArgoCD**
GitOps CD tool — sync K8s cluster từ Git repo tự động. Khi merge vào main, ArgoCD detect thay đổi trong Helm chart / manifests và apply lên cluster. UI Web đẹp, thấy diff trước khi sync. Chuẩn GitOps.

---

### Observability — Metrics · Logs · Traces

**OpenTelemetry (OTel)**
Instrumentation layer chuẩn — một SDK, export ra nhiều backends. Java auto-instrumentation agent (zero-code), Go/Python manual SDK. Học concepts: spans, traces, context propagation.

**Prometheus + Alertmanager**
Metrics scraping từ mọi service (Spring Actuator /metrics, Go expvar, Python prometheus-client). Alertmanager gửi alert khi latency > threshold. Học PromQL queries.

**Grafana**
Dashboard cho tất cả: Prometheus metrics, Loki logs, Tempo traces — unified UI. Học cách build dashboard: request rate, error rate, latency (RED method), service dependency graph.

**Grafana Loki**
Log aggregation. Promtail agent collect logs từ containers và push lên Loki. Query bằng LogQL. Học cách correlate logs với traces (trace ID trong log lines).

**Grafana Tempo**
Distributed tracing backend. Nhận traces từ OTel Collector. Trace một request từ API Gateway → Java service → DB → RabbitMQ → Go consumer. Thấy latency breakdown end-to-end.

**OTel Collector**
Central pipeline: nhận metrics/logs/traces từ tất cả services → route/transform → export sang Prometheus, Loki, Tempo. Học cách config pipeline, batch export, sampling.

---

### DevSecOps — Security Pipeline

**Gitleaks**
Secret scanning — phát hiện API keys, passwords hardcoded trong code ngay tại commit. Chạy đầu tiên trong pipeline vì nhanh nhất và critical nhất.

**SonarCloud** (free for OSS)
SAST — static analysis tìm security bugs (SQLi, XSS, insecure deserialization) + code quality gate (coverage, duplication). Decoration trực tiếp lên PR.

**OWASP Dependency-Check + Dependabot**
SCA — scan CVE trong Maven dependencies (NVD database). Dependabot tự tạo PR update dependency khi có security fix. Block build nếu CVSS ≥ 9.0.

**Trivy (Aqua Security)**
All-in-one scanner: Docker image CVE (OS packages + language libs) + IaC misconfiguration (K8s YAML, Helm charts). Một tool, nhiều scan targets.

**OWASP ZAP**
DAST — scan running app trên staging environment. Tìm OWASP Top 10: SQLi, XSS, CSRF, insecure headers. Baseline Scan mode chạy được trong CI/CD.

**Cosign (Sigstore)**
Image signing — sign Docker image sau khi build, verify trước khi deploy. Ngăn supply chain attack: đảm bảo image chạy production đúng là image đã pass CI.

**Falco**
Runtime security — K8s DaemonSet monitor syscalls. Alert khi có anomalous behavior: shell trong container, unexpected file write, privilege escalation. Lớp bảo vệ cuối cùng.

---

### Infrastructure & Tooling

**Cloudinary (Media)**
Avatar upload + transformation (resize, crop, optimize). Free tier 25GB. Tránh cần setup S3 + CloudFront.

**Resend (Email)**
Transactional email. 3000 emails/month free. Simple REST API, gọi từ Go Notification Service.

**Vercel (Next.js Web)**
First-class Next.js support. Edge CDN cho SSR. Preview deployments per PR. Backend services chạy trên K8s, frontend chạy Vercel.

---

## Decision Log — Open questions đã được resolve

| Question | Decision | Lý do | Trade-off chấp nhận | Status |
|----------|----------|-------|---------------------|--------|
| Architecture pattern? | **Modular Monolith + DDD** Phase 1 → **Microservices** Phase 2 | Nắm domain boundaries + DDD patterns trước khi tách. Monolith cho phép refactor domain model tự do mà không cần lo distributed transactions. Khi boundaries rõ ràng → tách tự nhiên. | Cần discipline cao để giữ module boundaries không bị xâm phạm. Dùng ArchUnit để enforce dependency rules. | **Decided** |
| Cross-module communication? | **Outbox Pattern** — INSERT trong cùng transaction, Relay deliver async | At-least-once guarantee không cần 2-phase commit. Relay dùng FOR UPDATE SKIP LOCKED — safe với nhiều instances. Phase 2: đổi Relay deliver từ Spring Events sang RabbitMQ, không cần đổi transaction logic. | Relay polling delay ~1s cho async events. Consumers phải idempotent (check eventId). Cần monitor FAILED events. | **Decided** |
| Backend language strategy? | **Java 21** main, **Go** concurrent, **Python** AI | Học 3 paradigms trong 1 project. Java: OOP/enterprise patterns. Go: goroutine concurrency. Python: AI/ML ecosystem. Mỗi ngôn ngữ ở đúng chỗ mạnh nhất. | 3 ngôn ngữ = 3 build pipelines, 3 codebases. Phù hợp vì mục tiêu là học, không phải tối ưu team velocity. | **Decided** |
| Message Queue? | **RabbitMQ** với AMQP | Học proper message broker: exchange types (topic/fanout/direct), routing keys, DLQ, acknowledgment. Hiểu sâu hơn BullMQ/Redis pub-sub. | Phức tạp hơn BullMQ. Cần chạy thêm 1 service. Trade-off chấp nhận được vì mục tiêu là học. | **Decided** |
| Container orchestration? | **Docker Compose** local + **k3s/Kubernetes** staging | Docker Compose để dev nhanh (1 lệnh spin up toàn bộ stack). K8s để học production orchestration: deployments, services, ingress, HPA, rolling updates. | K8s overhead cao cho local. Dùng k3s (lightweight) hoặc minikube. Không cần managed EKS cho learning. | **Decided** |
| Observability stack? | OTel + Prometheus + Grafana + Loki + Tempo | Học full observability: metrics (Prometheus), logs (Loki), traces (Tempo) — tất cả trong Grafana. OTel Collector là single pipeline. Thấy distributed trace end-to-end qua 3 ngôn ngữ. | Nặng hơn ELK Stack nhưng modern và free. Grafana stack tích hợp tốt, không cần nhiều config glue. | **Decided** |
| JWT strategy? | Access (15m) + Refresh (7d) với rotation | Access token ngắn → giảm risk nếu bị leak. Refresh rotation → detect token reuse. httpOnly cookie → XSS safe. | Refresh rotation yêu cầu Redis. Silent refresh cần xử lý cẩn thận trên client (race condition). | **Decided** |
| ORM cho Java? | **Spring Data JPA** + **Flyway** | JPA cho CRUD standard. Native queries (@Query) cho complex analytics. Flyway cho migrations — SQL files versioned trong git, reproducible schema. | Hibernate N+1 query trap — cần discipline. Monitor với Hibernate stats + slow query log. | **Decided** |
| Mobile notification? | **Expo Push Notifications** | Expo abstraction layer xử lý APNs + FCM. Không cần setup riêng. Đủ cho learning scope. | Phụ thuộc Expo infrastructure. Migrate sang direct FCM/APNs nếu cần advanced control. | **Decided** |
| World visualization engine? | **D3.js** cho web, **Skia (via RN Skia)** cho mobile | D3.js flexible cho generative district layout. RN Skia cho GPU-accelerated canvas trên mobile. World là differentiating feature — cần custom rendering. | D3 learning curve. Skia thêm native dependency. Implement sau — MVP có thể dùng simple bar chart trước. | **Deferred to v2** |

---

## Environments

### Development

Docker Compose: PostgreSQL + Redis + ES + RabbitMQ local. Next.js dev server. Java (Spring Boot), Go (Fiber) và Python (FastAPI) chạy với `--watch`/hot reload.

### Staging

k3s/Kubernetes cluster (Rancher) per branch. Vercel preview deployment. Seed DB với fixture data. GitHub Actions build → ArgoCD sync.

### Production

Kubernetes production (API + services). Vercel production (Web). Expo EAS production build. Upstash Redis + Bonsai ES + RabbitMQ managed.

---

## Technical Risks

### Risk: Elasticsearch sync lag

**Quest mới publish không xuất hiện ngay trong search**

`quest.published` event async → ES index delay 1-5s. **Mitigation:** Hiển thị "Your quest will appear in search shortly" sau publish. Monitor queue lag.

### Risk: Denormalized counter drift

**fork_count, avg_rating, completion_count có thể bị stale**

Nếu outbox relay event fail và không retry. **Mitigation:** Idempotent consumer (eventId) + exponential backoff. Nightly reconcile job tính lại từ source tables.

### Risk: World visualization performance

**User có nhiều skill domains → nhiều districts → render chậm**

**Mitigation:** MVP dùng simple layout. D3 force simulation chỉ khi < 20 nodes. Virtualize nếu nhiều hơn. Defer heavy world rendering đến v2.
