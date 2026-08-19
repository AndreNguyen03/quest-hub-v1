# RUNBOOK — QuestHub

Hướng dẫn chạy toàn bộ project trên máy local. Môi trường: **Windows + PowerShell 5.1** (lệnh bash tương đương ghi chú trong ngoặc).

> Nguyên tắc: `.env` = copy từ `*.example`. **Chỉ commit file `*.example`, không commit `.env`.**

---

## 1. Yêu cầu trước khi chạy

| Công cụ | Version | Kiểm tra |
|---|---|---|
| Java | 21 | `java -version` |
| Maven Wrapper | (đi kèm `backend/mvnw.cmd`) | — |
| Docker | bất kỳ | `docker --version` |
| Node.js | 20+ | `node -v` |
| Go | 1.22+ | `go version` |
| Python | 3.12 | `python --version` |

---

## 2. Cài đặt lần đầu (one-time)

```powershell
# 1. Tạo .env từ example (dùng cho docker compose)
Copy-Item .env.dev.example .env

# 2. Cài dependencies frontend
cd web; npm install
cd ../admin-web; npm install
cd ../mobile; npm install

# 3. Python venv cho ai-service
cd ../ai-service
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
```

---

## 3. Khởi động hạ tầng (Docker)

Postgres (5432) + Redis (6379) + Elasticsearch (9200):

```powershell
docker compose up -d postgres redis elasticsearch
```

Kiểm tra container chạy:

```powershell
docker ps
```

Tắt hạ tầng:

```powershell
docker compose stop postgres redis elasticsearch   # tạm dừng
docker compose down                                # xóa container (GIỮ volume)
docker compose down -v                            # xóa luôn data (KHÔNG làm trừ khi muốn reset)
```

Logs hạ tầng:

```powershell
docker compose logs -f
```

> **Lưu ý ES:** image elasticsearch đang dùng `8.18.8` phải khớp version client `elasticsearch-java` trong Spring Boot. Lệch version → health backend báo `DOWN` với lỗi decode `cluster.health`. Không tự ý hạ image xuống.

---

## 4. Backend (Spring Boot, port 9090)

```powershell
cd backend
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

> Bash: `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`

**Kiểm chứng:**

```powershell
Invoke-WebRequest -Uri http://localhost:9090/actuator/health -UseBasicParsing
# Kỳ vọng: {"status":"UP"}
```

- `UP` = DB + Redis + ES đều kết nối được.
- `DOWN` = xem mục Troubleshooting.

**Restart khi thêm/sửa migration:** Flyway chỉ chạy migration **lúc startup**. Thêm file `V*.sql` xong phải restart backend:

```powershell
# Windows: Ctrl+C trong terminal đang chạy backend, rồi chạy lại lệnh trên
```

**Kiểm tra schema sau khi Flyway chạy:**

```powershell
docker exec -it questhub-postgres psql -U questhub -d questhub -c "\d users"
docker exec -it questhub-postgres psql -U questhub -d questhub -c "SELECT * FROM flyway_schema_history;"
```

---

## 5. AI service (FastAPI, port 8090)

```powershell
cd ai-service
.\.venv\Scripts\Activate.ps1
uvicorn app.main:app --reload --port 8090
```

Kiểm chứng: mở `http://localhost:8090/docs`

---

## 6. Social & Notification (Go, port 8081 / 8082)

```powershell
cd social
go run ./cmd/server
```

```powershell
cd notification
go run ./cmd/server
```

---

## 7. Web & Admin Web (Next.js, port 3000 / 3001)

```powershell
cd web
npm run dev
```

```powershell
cd admin-web
npm run dev
```

---

## 8. Chạy tất cả (tương đương Makefile)

Makefile chỉ chạy được trên bash (WSL/Git Bash). Trên PowerShell chạy từng lệnh ở mục 3–7.

| Việc | Makefile | PowerShell tương đương |
|---|---|---|
| Tạo .env | `make env-example` | `Copy-Item .env.dev.example .env` |
| Lên hạ tầng | `make db-up` | `docker compose up -d postgres redis elasticsearch` |
| Backend | `make dev-backend` | `cd backend; .\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev` |
| AI | `make dev-ai` | `cd ai-service; uvicorn app.main:app --reload --port 8090` |
| Social | `make dev-social` | `cd social; go run ./cmd/server` |
| Notification | `make dev-notification` | `cd notification; go run ./cmd/server` |
| Web | `make dev-web` | `cd web; npm run dev` |
| Tắt hạ tầng | `make down` | `docker compose down` |

---

## 9. Migration — quy trình chuẩn

1. Tạo file `backend/src/main/resources/db/migration/V<N>__<ten>.sql` — **chung cho mọi env** (dev/staging/prod cùng chain).
2. **Bất biến:** file migration đã chạy ở prod tuyệt đối không sửa — thay đổi = tạo file `V<N+1>` mới.
3. Seed data (dữ liệu giả cho dev) — KHÔNG nhét vào `db/migration`. Đặt thư mục riêng + profile riêng.
4. Restart backend → Flyway tự chạy → kiểm tra bằng `flyway_schema_history`.

**Test nhanh SQL trước khi giao cho Flyway** (tạo bảng tay để bắt syntax error):

```powershell
Get-Content backend\src\main\resources\db\migration\V1__create_users.sql |
  docker exec -i questhub-postgres psql -U questhub -d questhub
```

> Cảnh báo: đã tạo tay bằng psql thì Flyway sẽ báo `relation already exists`. Muốn để Flyway quản lý → `DROP TABLE` bảng đó trước khi restart backend.

---

## 10. Test

```powershell
# Backend
cd backend; .\mvnw.cmd test

# AI
cd ai-service; pytest

# Go (social + notification)
cd social; go test ./...
cd ../notification; go test ./...
```

---

## 11. Troubleshooting

| Triệu chứng | Nguyên nhân | Cách xử lý |
|---|---|---|
| Health backend `DOWN` | ES chưa up / lệch version ES vs client | `docker compose up -d elasticsearch` · xem mục 3 |
| `relation "users" already exists` | Tạo bảng tay bằng psql trước khi chạy Flyway | `DROP TABLE users;` rồi restart backend |
| `FlywayException: checksum mismatch` | Sửa file migration đã từng chạy | Không sửa file cũ — tạo `V<N+1>` mới |
| Port 9090 đã bị chiếm | Backend cũ còn chạy | Tìm & kill process Java cũ, restart |
| `psql: error: connection refused` | Postgres chưa up | `docker compose up -d postgres` |

---

## 12. Kiến trúc module

| Folder | Runtime | Port |
|---|---|---|
| `backend/` | Java 21 + Spring Boot 3 | 9090 |
| `social/` | Go + Fiber | 8081 |
| `notification/` | Go + Fiber | 8082 |
| `ai-service/` | Python 3.12 + FastAPI | 8090 |
| `web/` | Next.js | 3000 |
| `admin-web/` | Next.js | 3001 |
| `mobile/` | React Native | — |
