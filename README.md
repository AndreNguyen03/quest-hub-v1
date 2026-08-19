# QuestHub

Goal-achievement platform: Domain → LearningPath → Quest → Chapter → Task. Người dùng chọn mục tiêu, đi theo quest, tiến bộ thật được phản ánh vào World cá nhân.

## Monorepo — các service

| Folder | Runtime | Port | Mô tả |
|--------|---------|------|-------|
| `backend/` | Java 21 + Spring Boot 3 | 9090 | Modular Monolith — MOD-01→05, 07 (Identity, Quest, Marketplace, World, Admin) |
| `social/` | Go + Fiber | 8081 | MOD-05 — feed, follow, comment, discussion |
| `notification/` | Go + Fiber | 8082 | In-app + push + email |
| `ai-service/` | Python 3.12 + FastAPI | 8090 | MOD-06 — recommend, generate, grade (AI Grader), coach (read-only agent) |
| `web/` | Next.js | 3000 | Web app (SSR marketplace/quest detail) |
| `admin-web/` | Next.js | 3001 | Admin panel |
| `mobile/` | React Native | — | App tracking-first |

> Chia monorepo, khi deploy thật từng service độc lập mới tách repo riêng. Service boundary được enforce bằng cấu trúc folder + ArchUnit (backend) + DB roles.

## Bắt đầu nhanh

```bash
make db-up       # postgres + redis + elasticsearch (docker compose)
make dev-backend # Spring Boot
make dev-ai      # FastAPI
make test        # chạy test tất cả service
make down        # tắt hạ tầng
```

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

Toàn bộ design nằm trong [`docs/`](docs/) — mở bằng trình duyệt:
`modules-user-stories.html` · `us-analysis.html` · `api-design.html` · `database-schema.html` · `event-contracts.html` · `high-level-design.html` · `sequence-diagrams.html` ...

## Nguyên tắc (tóm tắt từ docs)

- Outbox pattern cho mọi domain event; consumer idempotent theo `eventId`.
- AI Coach **chỉ đọc** progress (tool calling read-only) — không tool nào write quest.
- AI Grader không tự PASS — chỉ publish `submission.graded`, Quest Module quyết định.
- Không XP/Level — Reward intrinsic, Achievement gắn mốc thật.
