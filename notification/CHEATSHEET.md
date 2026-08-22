# Notification Service — Cheatsheet (Go cho Java Dev)

Go side-app consumes domain events từ transactional outbox của Java monolith
(`outbox_events`) và ghi vào bảng `notifications`. Stack align shareway-be.

---

## Phần 1 — Go cho Java Developer

### 1.1 Bản đồ khái niệm Java → Go

| Java | Go (trong repo này) | Ghi chú |
|---|---|---|
| `pom.xml` / Maven | `go.mod` / `go get` | Module path `questhub/notification` như groupId |
| `class User {}` | `type User struct {...}` | Không có class, không kế thừa |
| `extends` / `implements` | embedding + **implicit interface** | Struct tự động thỏa interface nếu đủ method — không cần khai báo `implements` |
| `public` / `private` | Chữ **HOA** = export, chữ **thường** = private | `Create()` public, `poll()` private. Không có keyword |
| Constructor | Hàm factory `NewXxx(...)` | `NewNotificationRepository(db)` — convention toàn repo |
| `throw` / `try-catch` | `return error` + `if err != nil` | Error là giá trị trả về, không có exception |
| `finally` | `defer` | `defer rows.Close()` chạy khi hàm return |
| `null` / `Optional<T>` | Zero value / con trỏ `*T` | `Body *string` = nullable; `string` rỗng ≠ nil |
| `@JsonProperty("user_id")` | Struct tag `` `json:"userId"` `` | Tag cũng dùng cho `gorm:"..."`, `form:"..."`, `binding:"..."`, `uri:"..."` |
| JPA/Hibernate `@Entity` | GORM tags + `TableName()` | Xem `schemas.Notification` |
| `@Transactional` | `db.Transaction(func(tx *gorm.DB) error {...})` | Return nil = commit, return error = rollback |
| Spring `@Autowired` | **DI thủ công** qua constructor | Không có magic: `NewXxx(dep)` hết |
| Spring Boot | Gin + `main.go` bootstrap tự viết | Xem luồng khởi động trong main.go |
| `application.properties` | `app.env` (viper load) | Env var override được file |
| `Stream().map().filter()` | Vòng `for range` + hàm tự viết | Go không có stream API |
| `List<String>` / `Map<K,V>` | `[]string` / `map[K]V` | Slice có thể append, không fixed-size |
| `<T> T method(T x)` | `[T any] func(x T) T` | Generics Go 1.18+, ít dùng hơn Java |
| `Thread` / `ExecutorService` | `go f()` (goroutine) + `sync.WaitGroup` | Goroutine rẻ hơn thread hàng chục lần |
| `CompletableFuture` | goroutine + channel / `context.Context` | Cancel qua ctx.Done() |
| `synchronized` | `sync.Mutex` / channel | Worker hiện tại không cần |
| JUnit | `testing` + `go test ./...` | File `*_test.go` cùng package |

### 1.2 Pattern error handling (gặp mọi nơi trong repo)

```go
// Wrap error giữ nguyên cause — như exception chain của Java (%w = unwrap được bằng errors.Is/As)
if err := json.Unmarshal(raw, &p); err != nil {
    return fmt.Errorf("parse userId: %w", err)
}

// HTTP handler: fail sớm từng bước (không try-catch bọc ngoài)
if err := c.ShouldBindQuery(&req); err != nil {
    helper.BadRequest(c, "dev message EN (vào log)", "Thông báo tiếng Việt cho user")
    return
}
```

### 1.3 Syntax hay gặp

```go
// := khai báo + gán (kiểu tự suy), var khi cần khai trước
userID, err := uuid.Parse(req.UserID)

// Pointer receiver (sửa state / tránh copy) vs value receiver (read-only nhỏ)
func (r *NotificationRepository) Create(...)   // *receiver chuẩn cho struct có DB handle
func (Notification) TableName() string          // value receiver cho hàm thuần

// Multiple return values — chuẩn Go
func (r *INotificationRepository) CountUnread(...) (int, error)

// context.Context luôn là tham số ĐẦU TIÊN để trace/cancel xuyên suốt
func (s *NotificationService) MarkRead(ctx context.Context, id uuid.UUID) error
```

### 1.4 Tooling tương đương

| Việc | Command |
|---|---|
| Chạy app | `make run` (`go run .`) |
| Build jar/exe | `make build` |
| Lint (như SpotBugs) | `make vet` |
| Format tự động (như spotless) | `gofmt -w .` |
| Test | `make test` |
| Regenerate Swagger | `make swagger` (swag CLI trong `go/bin`) |

---

## Phần 2 — Kiến trúc & luồng request

```
Request → Router (+CORS) → Controller (bind → validate.Struct → gọi service)
        → Service (interface) → Repository (interface) → GORM → PostgreSQL

Song song: OutboxWorker poll outbox_events (FOR UPDATE SKIP LOCKED)
        → EventHandler → INotificationRepository.Create
```

Mỗi layer có **Container + Factory** (như ApplicationContext mini):

| Layer | File đăng ký | Interface |
|---|---|---|
| repository | `repository/repository.go` | `INotificationRepository` |
| service | `service/service.go` | `INotificationService` |
| controller | `controller/controller.go` | (struct, phụ thuộc interface service) |

Bootstrap `main.go`: `init()` UTC → viper config → zerolog → GORM →
`NewRepositoryFactory(psql).CreateRepositories()` → `NewServiceFactory(repos)...` →
`NewControllerFactory(services)...` → `NewAPIServer(ctrls)` → graceful shutdown.

---

## Phần 3 — Implement US mới: THỨ TỰ CODE

### 3.1 Nguyên tắc

- **Code từ đáy lên**: schemas → repository → service → controller → router. Vì Go compile kiểm tra dependency, layer dưới phải tồn tại trước layer trên.
- **Interface-first**: định nghĩa interface ngay trong file implementation.
- **Đăng ký kép**: module mới = thêm field vào Container + method trong Factory của cả 3 layer.

### 3.2 Checklist chuẩn (in ra dán màn hình)

| # | File | Việc phải làm |
|---|---|---|
| 1 | `schemas/<domain>.go` | DTO request/response (`binding` tags) + entity GORM nếu có bảng mới |
| 2 | `repository/<domain>_repo.go` | `I<Domain>Repository` + struct + `New<Domain>Repository(db *gorm.DB)`; **thêm field vào `RepositoryContainer` + method trong `CreateRepositories()`** |
| 3 | `service/<domain>_service.go` | `I<Domain>Service` + impl inject repo interface; **thêm vào `ServiceContainer` + `CreateServices()`** |
| 4 | `controller/<domain>_controller.go` | Handlers: `ShouldBindJSON/Query` → `validate.Struct` → service → `helper.*`; **thêm vào `ControllerContainer` + `CreateControllers()`** |
| 5 | `router/<domain>_router.go` | `Setup<Domain>Router(rg *gin.RouterGroup, server *APIServer)`; gọi nó trong `SetupRouter()` |
| 6 | Swagger annotations | Viết luôn ở bước 4 (`@Summary`, `@Param`, `@Success`, `@Router`) rồi `make swagger` |
| 7 | Verify | `go vet ./...` + `go build` (Makefile: `make vet`) |

### 3.3 Ví dụ xuyên suốt: US "Gửi broadcast notification"

`POST /api/v1/notifications/broadcast` — admin gửi thông báo trực tiếp.

**Bước 1 — `schemas/notification.go`** (thêm DTO, entity giữ nguyên):

```go
// CreateBroadcastRequest is the JSON body for POST /api/v1/notifications/broadcast.
type CreateBroadcastRequest struct {
	UserID string `json:"userId" binding:"required,uuid"`
	Type   string `json:"type" binding:"required,oneof=TASK_COMPLETED QUEST_COMPLETED ACHIEVEMENT FOLLOWED COMMENT REVIEW ADMIN"`
	Title  string `json:"title" binding:"required,max=255"`
	Body   string `json:"body" binding:"max=1000"`
}
```

**Bước 2 — repository: KHÔNG ĐỔI** (đã có `INotificationRepository.Create`). Chỉ thêm method khi cần query mới — và lúc đó sửa interface + impl trong **cùng 1 file**.

**Bước 3 — `service/notification_service.go`** (thêm method vào interface + impl):

```go
// Trong INotificationService thêm:
Broadcast(ctx context.Context, req *schemas.CreateBroadcastRequest) (*schemas.Notification, error)

// Impl — business rule nằm đây: ép type hợp lệ, default payload {}
func (s *NotificationService) Broadcast(ctx context.Context, req *schemas.CreateBroadcastRequest) (*schemas.Notification, error) {
	userID, _ := uuid.Parse(req.UserID)
	n := &schemas.Notification{
		ID:      uuid.New(),
		UserID:  userID,
		Type:    schemas.NotificationType(req.Type),
		Title:   req.Title,
		Payload: jsonb.JSONB{},
	}
	if req.Body != "" {
		n.Body = &req.Body
	}
	if err := s.repo.Create(ctx, n); err != nil {
		return nil, err
	}
	return n, nil
}
```

**Bước 4 — `controller/notification_controller.go`** (handler + swagger annotation):

```go
// Broadcast godoc
// @Summary      Send broadcast notification
// @Description  Admin gửi thông báo trực tiếp tới user
// @Tags         notifications
// @Accept       json
// @Produce      json
// @Param        request  body  schemas.CreateBroadcastRequest  true  "Nội dung broadcast"
// @Success      201  {object}  schemas.Notification
// @Failure      400  {object}  helper.ErrorResponse
// @Failure      500  {object}  helper.ErrorResponse
// @Router       /api/v1/notifications/broadcast [post]
func (ctrl *NotificationController) Broadcast(c *gin.Context) {
	var req schemas.CreateBroadcastRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		helper.BadRequest(c, "bind broadcast body failed", "Dữ liệu không hợp lệ")
		return
	}
	if err := ctrl.validate.Struct(req); err != nil {
		helper.BadRequest(c, "validate broadcast body failed", "Dữ liệu không hợp lệ")
		return
	}

	n, err := ctrl.service.Broadcast(c.Request.Context(), &req)
	if err != nil {
		helper.InternalError(c, err, "broadcast notification failed")
		return
	}
	helper.GinResponse(c, http.StatusCreated, n)
}
```

**Bước 5 — `router/notification_router.go`**:

```go
n.POST("/broadcast", server.Ctrls.Notification.Broadcast)
```

**Bước 6–7**: `make swagger` → `make vet` → `make build`.

### 3.4 Khi nào phải đụng Container/Factory?

| Loại thay đổi | schemas | repo | service | ctrl | router | Container/Factory |
|---|---|---|---|---|---|---|
| Thêm/sửa endpoint trong domain có sẵn | ✏️ | ✏️ nếu query mới | ✏️ | ✏️ | ✏️ | ❌ không đụng |
| **Thêm domain/module mới** (VD `preferences`) | ✅ file mới | ✅ file mới | ✅ file mới | ✅ file mới | ✅ file mới | ✅ **cả 3 tầng** |
| Đổi logic business | ❌ | ❌ | ✏️ | ❌ | ❌ | ❌ |

Module mới `preferences` sẽ tạo thêm: `schemas/preferences.go`, `repository/preferences_repo.go` +
field `Preferences IPreferencesRepository`, `service/preferences_service.go` + field,
`controller/preferences_controller.go` + field, `router/preferences_router.go`.

### 3.5 Quy ước bắt buộc (review checklist)

- Godoc comment trên **mọi** exported func/type
- Response chỉ qua `helper.GinResponse` / `helper.ErrorResponseWithMessage`
  (body chuẩn `{"error": bool, "message": string}`; dev msg EN vào log, user msg tiếng Việt)
- Validate: bind → `ctrl.validate.Struct(req)` → return sớm mỗi bước fail
- Error wrap bằng `%w`, log qua `logger.Log` (zerolog)
- Timezone UTC (đã set trong `init()`), `context.Context` tham số đầu tiên
- Không commit `app.env`, `logs/`, `bin/`

---

## Phần 4 — Ops nhanh

### Run

```bash
cd notification
cp app.env.example app.env    # viper load "." — hoặc dùng env vars
make swagger                  # regen docs/ khi đổi annotations
make run                      # http://localhost:8082/swagger/index.html
```

Health check: `GET /health_check`. Timezone toàn app UTC.

### Config keys (app.env / env var)

| Key | Env var | Default |
|---|---|---|
| `database.url` | `DATABASE_URL` | `postgres://questhub:questhub@localhost:5432/questhub` |
| `server.port` | `NOTIFICATION_PORT` | `8082` |
| `outbox.poll_interval_secs` | `OUTBOX_POLL_INTERVAL_SECS` | `5` |
| `log.file_path` | `LOG_FILE_PATH` | `logs/notification.log` |

### API

| Method | Path | Mô tả |
|---|---|---|
| GET | `/api/v1/notifications?userId=&page=&limit=` | Inbox (mới nhất trước, limit ≤ 100) |
| PATCH | `/api/v1/notifications/:id/read` | Đánh dấu 1 notification đã đọc |
| PATCH | `/api/v1/notifications/read-all?userId=` | Đánh dấu tất cả đã đọc |
| GET | `/api/v1/notifications/unread-count?userId=` | Badge count |

### Outbox polling (Phase 1)

- Poll: `SELECT ... WHERE status='PENDING' AND event_type = ANY(...) ORDER BY created_at LIMIT 50 FOR UPDATE SKIP LOCKED`
  (raw SQL trong GORM transaction).
- Event types consumed: `task.completed`, `quest.completed`, `achievement.unlocked`,
  `comment.created`, `discussion.created`, `user.followed`, `submission.graded`.
- `task.completed` có cờ `isQuestCompleted` → tạo thêm QUEST_COMPLETED (chống duplicate).

### Phase 2 TODO

- RabbitMQ fan-out exchange thay polling (khi đó thêm `infra/rabbitmq` như shareway).
- Java monolith enrich `recipientUserId` vào `comment.created`/`discussion.created`
  (hiện thiếu → handler skip + warn).
- Multi-consumer tracking để không race với Java relay.
