# RUNBOOK — QuestHub

Hướng dẫn chạy toàn bộ project trên máy local.

> **Nguyên tắc:** `.env` = copy từ `*.example`. Chỉ commit file `*.example`, không commit `.env`.

---

## 1. Yêu cầu trước khi chạy

| Công cụ | Version | Kiểm tra |
|---|---|---|
| Java | 21 | `java -version` |
| Go | 1.25+ | `go version` |
| Python | 3.12 | `python --version` |
| Node.js | 24+ | `node -v` |
| pnpm | latest | `pnpm -v` |
| Docker Desktop | bất kỳ | `docker --version` |

> Maven Wrapper (`mvnw`) đi kèm trong `backend/` — không cần cài Maven riêng.

---

## 2. Cài đặt lần đầu

```bash
# 1. Tạo .env từ example (dùng cho docker compose)
cp .env.dev.example .env

# 2. Go deps
cd notification && go mod download && cd ..
cd social && go mod download && cd ..

# 3. Python deps cho ai-service
cd ai-service
python -m venv .venv
# Windows:  .venv\Scripts\activate
# Mac/Linux: source .venv/bin/activate
pip install -r requirements.txt
cd ..

# 4. Web deps
cd web && pnpm install && cd ..
```

---

## 3. Khởi động hạ tầng (Docker)

Postgres (5432) + Redis (6379) + Elasticsearch (9200):

```bash
make db-up
# hoặc:
docker compose up -d postgres redis elasticsearch
```

Kiểm tra:
```bash
docker ps
```

Tắt:
```bash
make down               # tắt container, giữ volume
docker compose down -v  # xóa cả volume/data (reset hoàn toàn)
```

> **Lưu ý ES:** image `8.18.8` phải khớp version client trong Spring Boot. Lệch version → backend health `DOWN`.

---

## 4. Backend — Spring Boot (port 9090)

```bash
make dev-backend
# hoặc:
cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
# Windows:
cd backend && .\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

Kiểm tra:
```bash
curl http://localhost:9090/actuator/health
# Kỳ vọng: {"status":"UP"}
```

Swagger UI: `http://localhost:9090/swagger-ui.html`

**Sau khi thêm migration:** Flyway chỉ chạy lúc startup → restart backend.

```bash
# Kiểm tra schema
docker exec -it questhub-postgres psql -U questhub -d questhub -c "\dt"
docker exec -it questhub-postgres psql -U questhub -d questhub -c "SELECT * FROM flyway_schema_history ORDER BY installed_rank;"
```

---

## 5. Notification — Go (port 8082)

Phải chạy **sau khi backend đã chạy Flyway ít nhất 1 lần** (cần bảng `outbox_events` + `notifications`).

```bash
cp notification/app.env.example notification/app.env

make dev-notification
# hoặc:
cd notification && go run .
```

`notification/app.env`:
```
DATABASE_URL=postgres://questhub:questhub@localhost:5432/questhub
NOTIFICATION_PORT=8082
OUTBOX_POLL_INTERVAL_SECS=5
```

Kiểm tra:
```bash
curl http://localhost:8082/health_check
# {"error":false,"message":"ok"}
```

Swagger UI: `http://localhost:8082/swagger/index.html`

**Features:** In-app inbox · SSE real-time (`/api/v1/notifications/stream`) · FCM push (cần `FCM_CREDENTIALS_PATH`) · Email (cần `SMTP_HOST`) · Admin broadcast

---

## 6. Social — Go (port 8081)

```bash
make dev-social
# hoặc:
cd social && go run .
```

Config qua env vars:
```
DATABASE_URL=postgres://questhub:questhub@localhost:5432/questhub
SOCIAL_PORT=8081
OUTBOX_POLL_INTERVAL_SECS=5
```

Kiểm tra:
```bash
curl http://localhost:8081/health_check
# {"error":false,"message":"ok"}
```

**Features:** Feed, follow/unfollow · Comments + discussions (materialized path, tối đa 2 cấp) · Outbox worker consume quest/achievement events

---

## 7. AI Service — FastAPI (port 8090)

```bash
# Activate venv trước
# Windows:  ai-service\.venv\Scripts\activate
# Mac/Linux: source ai-service/.venv/bin/activate

make dev-ai
# hoặc:
cd ai-service && uvicorn app.main:app --reload --port 8090
```

Cần set trong môi trường hoặc `ai-service/.env`:
```
OPENROUTER_API_KEY=sk-or-...
AI_MODEL=meta-llama/llama-3.1-8b-instruct:free
DATABASE_URL=postgresql+asyncpg://questhub:questhub@localhost:5432/questhub
ELASTICSEARCH_URL=http://localhost:9200
```

Kiểm tra: `http://localhost:8090/docs`

**Features:** `POST /api/v1/ai/grade` · `POST /api/v1/ai/coach/sessions` + `/messages` (SSE streaming) · `POST /api/v1/ai/recommend` · `POST /api/v1/ai/generate-quest`

---

## 8. Web — Next.js (port 3000)

```bash
make dev-web
# hoặc:
cd web && pnpm dev
```

`web/.env.development` đã có sẵn:
```
NEXT_PUBLIC_API_BASE_URL=http://localhost:9090/api
NEXT_PUBLIC_AI_BASE_URL=http://localhost:8090
```

Mở: `http://localhost:3000`

---

## 9. Chạy toàn bộ bằng Docker

```bash
# Build tất cả images
docker compose build

# Chạy toàn bộ stack
docker compose up -d

# Logs
docker compose logs -f backend
docker compose logs -f notification

# Restart 1 service
docker compose restart backend

# Tắt
docker compose down
```

---

## 10. Makefile — tất cả targets

| Target | Việc làm |
|---|---|
| `make db-up` | Khởi động postgres + redis + elasticsearch |
| `make db-down` | Tắt infra |
| `make dev-backend` | Spring Boot :9090 |
| `make dev-notification` | Go notification :8082 |
| `make dev-social` | Go social :8081 |
| `make dev-ai` | FastAPI :8090 |
| `make dev-web` | Next.js :3000 |
| `make build` | Build tất cả Docker images |
| `make up` | Docker compose up toàn bộ stack |
| `make down` | Docker compose down |
| `make test` | Chạy tất cả tests |
| `make backend-test` | Maven test |
| `make ai-test` | pytest |
| `make go-test` | Go test notification + social |
| `make web-build` | Next.js production build |

---

## 11. Migration

1. Tạo `backend/src/main/resources/db/migration/V<N>__<ten>.sql`
2. File đã chạy ở prod **tuyệt đối không sửa** — thay đổi = tạo `V<N+1>`
3. Restart backend → Flyway tự chạy

Test SQL trước:
```bash
docker exec -i questhub-postgres psql -U questhub -d questhub < backend/src/main/resources/db/migration/VN__ten.sql
```

---

## 12. Tests

```bash
make test           # tất cả (backend + ai + go)
make backend-test   # Maven
make ai-test        # pytest
make go-test        # Go notification + social
```

---

## 13. Troubleshooting

| Triệu chứng | Nguyên nhân | Cách xử lý |
|---|---|---|
| Backend health `DOWN` | ES chưa up / lệch version | `docker compose up -d elasticsearch` |
| `relation already exists` | Tạo bảng tay trước Flyway | `DROP TABLE <table>;` → restart backend |
| `FlywayException: checksum mismatch` | Sửa file migration đã chạy | Tạo `V<N+1>` mới, không sửa cũ |
| Port 9090 bị chiếm | Backend cũ còn chạy | Kill process Java cũ |
| `psql: connection refused` | Postgres chưa up | `docker compose up -d postgres` |
| Notification không poll | Backend chưa tạo bảng outbox | Chạy backend trước ít nhất 1 lần |
| `OPENROUTER_API_KEY` thiếu | AI service thiếu API key | Set env var hoặc tạo `ai-service/.env` |

---

## 14. Trạng thái service

| Service | Port | Status |
|---|---|---|
| `backend/` | 9090 | ✅ Done |
| `notification/` | 8082 | ✅ Done |
| `social/` | 8081 | ✅ Done |
| `ai-service/` | 8090 | ✅ Done |
| `web/` | 3000 | 🚧 Auth done, tiếp tục |
| `admin-web/` | 3001 | ⏳ Planned |
| `mobile/` | — | ⏳ Planned |
