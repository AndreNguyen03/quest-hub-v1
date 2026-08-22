# QuestHub

Goal-achievement platform: Domain → LearningPath → Quest → Chapter → Task. Người dùng chọn mục tiêu, đi theo quest, tiến bộ thật được phản ánh vào World cá nhân.

## Monorepo — các service

| Folder | Runtime | Port | Trạng thái | Mô tả |
|--------|---------|------|-----------|-------|
| `backend/` | Java 21 + Spring Boot 3 | 9090 | ✅ chạy được | Modular Monolith — Identity, Quest, Marketplace, World, Admin |
| `notification/` | Go + Gin + GORM | 8082 | ✅ chạy được | In-app inbox API + consumer `outbox_events` từ backend (push/email ở Phase 2) |
| `ai-service/` | Python 3.12 + FastAPI | 8090 | ✅ scaffold | MOD-06 — recommend, generate, grade (AI Grader), coach (read-only agent) |
| `social/` | Go + Fiber | 8081 | ⏳ planned | MOD-05 — feed, follow, comment, discussion |
| `web/` | Next.js | 3000 | ⏳ planned | Web app (SSR marketplace/quest detail) |
| `admin-web/` | Next.js | 3001 | ⏳ planned | Admin panel |
| `mobile/` | React Native | — | ⏳ planned | App tracking-first |

> Chia monorepo, khi deploy thật từng service độc lập mới tách repo riêng. Service boundary được enforce bằng cấu trúc folder + ArchUnit/Spring Modulith (backend) + DB roles.

## Bắt đầu nhanh

```bash
make db-up            # postgres + redis + elasticsearch (docker compose)
make dev-backend      # Spring Boot :9090
make dev-notification # Go outbox consumer + inbox API :8082
make dev-ai           # FastAPI :8090
make test             # test tất cả service đã implement
make down             # tắt hạ tầng
```

> `dev-social` / `dev-web` có trong Makefile nhưng các service này **chưa implement** — bỏ qua ở giai đoạn hiện tại.

## Môi trường: dev · staging · prod

| Env | Chạy ở đâu | Deploy khi nào | Config |
|-----|-----------|----------------|--------|
| **dev** | Local — docker compose + máy dev | — | `.env.dev.example` → `.env` · Spring profile `dev` · `web/.env.development` |
| **staging** | k3s (`questhub-staging`) | Tự động khi merge main | `.env.staging.example` · Spring profile `staging` · `infra/k8s/overlays/staging` · `deploy-staging.yml` |
| **prod** | k3s (`questhub-prod`) | Manual approval → ArgoCD GitOps | `.env.prod.example` · Spring profile `prod` · `infra/k8s/overlays/prod` · `deploy-prod.yml` |

- Chỉ commit file **`*.example`** — bản thật (`.env`, `.env.dev`, ...) đã gitignore.
- Dev dùng docker compose; staging/prod dùng Kustomize overlays, image tag = git SHA (immutable).
- Secret thật đi qua CI secrets / Vault, không bao giờ trong repo.
- Chi tiết pipeline bảo mật: `docs/devsecops-pipeline.html`.

## Tài liệu thiết kế

Toàn bộ design nằm trong [`docs/`](docs/):
`high-level-design.md` · `database-schema.md` · `api-design.md` · `event-contracts.md` ·
`ddd-convention.md` · `modules-user-stories.md` · `sequence-diagrams.md` · `devsecops-pipeline.md` ...

Skill AI dùng khi implement: `GO_SKILL.md`, `JAVA_SKILL.md`, `SKILL_*_ENTERPRISE.md`.
Cheatsheet Go cho Java dev + quy trình implement US: [`notification/CHEATSHEET.md`](notification/CHEATSHEET.md).

## Nguyên tắc (tóm tắt từ docs)

- Outbox pattern cho mọi domain event; consumer idempotent theo `eventId`.
- AI Coach **chỉ đọc** progress (tool calling read-only) — không tool nào write quest.
- AI Grader không tự PASS — chỉ publish `submission.graded`, Quest Module quyết định.
- Không XP/Level — Reward intrinsic, Achievement gắn mốc thật.
