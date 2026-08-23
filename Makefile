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
.PHONY: dev-ai ai-test ai-install ai-install-dev ai-install-eval ai-lint ai-typecheck ai-eval
dev-ai:
	cd ai-service && uvicorn app.main:app --reload --port 8090
ai-test:
	cd ai-service && pytest
ai-install:
	cd ai-service && pip install -e .
ai-install-dev:
	cd ai-service && pip install -e ".[dev]"
ai-install-eval:
	cd ai-service && pip install -e ".[eval]"
ai-lint:
	cd ai-service && ruff check .
ai-typecheck:
	cd ai-service && mypy app
ai-eval:
	cd ai-service && python -m evals.run_evals

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
.PHONY: test test-all
test: backend-test ai-test go-test
test-all: backend-test ai-test ai-lint ai-typecheck go-test
