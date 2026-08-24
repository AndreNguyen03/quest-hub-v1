# QuestHub

Nền tảng học tập có gamification: chọn mục tiêu → đi theo Quest → tiến độ thật phản ánh vào World cá nhân. AI chấm bài, gợi ý và coaching theo thời gian thực.

## Monorepo — các service

| Folder | Runtime | Port | Trạng thái | Mô tả |
|--------|---------|------|-----------|-------|
| `backend/` | Java 21 + Spring Boot 3 | 9090 | ✅ Done | Modular Monolith — Identity, Quest, Marketplace, World, Admin |
| `notification/` | Go + Gin | 8082 | ✅ Done | Inbox API + SSE real-time + FCM push + Email + Admin broadcast |
| `social/` | Go + Gin | 8081 | ✅ Done | Feed, follows, comments/discussions (materialized path) |
| `ai-service/` | Python 3.12 + FastAPI | 8090 | ✅ Done | Grade, AI Coach (tool-use + SSE), Recommend (ES), Generate quest |
| `web/` | Next.js 16 | 3000 | 🚧 In progress | Web app — auth done, marketplace/learning UI tiếp theo |
| `admin-web/` | TBD | 3001 | ⏳ Planned | Admin panel |
| `mobile/` | React Native | — | ⏳ Planned | Mobile app |

## Bắt đầu nhanh (local)

```bash
# 1. Setup .env
cp .env.dev.example .env

# 2. Khởi động infra (Postgres + Redis + Elasticsearch)
make db-up

# 3. Chạy từng service (mỗi cái 1 terminal)
make dev-backend      # Spring Boot :8080
make dev-notification # Go :8082
make dev-social       # Go :8081
make dev-ai           # FastAPI :8090
make dev-web          # Next.js :3000

# Hoặc chạy toàn bộ bằng Docker
make build && make up
```

> Chi tiết setup từng bước xem [`RUNBOOK.md`](RUNBOOK.md)

## Môi trường

| Env | Chạy ở đâu | Config |
|-----|-----------|--------|
| **dev** | Local — docker compose + máy dev | `.env.dev.example` → `.env` · Spring profile `dev` |
| **staging** | k3s (`questhub-staging`) | `.env.staging.example` · `infra/k8s/overlays/staging` |
| **prod** | k3s (`questhub-prod`) — manual approval | `.env.prod.example` · `infra/k8s/overlays/prod` |

- Chỉ commit file `*.example` — bản thật đã gitignore.
- Dev dùng docker compose; staging/prod dùng Kustomize overlays, image tag = git SHA.


