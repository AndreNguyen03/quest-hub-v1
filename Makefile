SHELL := /bin/bash
ENV ?= dev

# ── Hạ tầng (docker compose) ──────────────────────────────────────────
# .env = copy từ .env.$(ENV).example. Mặc định ENV=dev (docker compose chỉ dùng cho dev).
.PHONY: env-example db-up db-down down logs
env-example:
	cp .env.$(ENV).example .env
db-up:
	docker compose up -d postgres redis elasticsearch
db-down:
	docker compose stop postgres redis elasticsearch
down:
	docker compose down
logs:
	docker compose logs -f

# ── Backend (Spring Boot) ─────────────────────────────────────────────
# Profile theo ENV: dev | staging | prod (application-{env}.yml)
.PHONY: dev-backend backend-test
dev-backend:
	cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=$(ENV)
backend-test:
	cd backend && ./mvnw test

# ── AI service (FastAPI) ──────────────────────────────────────────────
.PHONY: dev-ai ai-test
dev-ai:
	cd ai-service && uvicorn app.main:app --reload --port 8090
ai-test:
	cd ai-service && pytest

# ── Social / Notification (Go) ────────────────────────────────────────
# social/ chưa implement — target giữ nguyên cho lúc sau
.PHONY: dev-social dev-notification go-test
dev-social:
	cd social && go run ./cmd/server
dev-notification:
	cd notification && go run .
go-test:
	cd notification && go test ./...

# ── Web (Next.js) ─────────────────────────────────────────────────────
.PHONY: dev-web web-test
dev-web:
	cd web && npm run dev
web-test:
	cd web && npm run lint && npm run typecheck

# ── Toàn bộ ───────────────────────────────────────────────────────────
.PHONY: test
test: backend-test ai-test go-test
