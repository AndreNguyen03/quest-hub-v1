# QuestHub — Mô tả các Module & Service

> Tài liệu này giải thích **mỗi module/service làm gì**, sở hữu dữ liệu gì, và không làm gì (ranh giới trách nhiệm).

---

## Java Monolith — Spring Boot Modulith

Một ứng dụng Spring Boot duy nhất được tổ chức thành **5 Bounded Context** độc lập. Các module giao tiếp qua public API interfaces, không import trực tiếp vào nhau.

---

### 1. Identity (Xác thực & Người dùng)

**Trách nhiệm:** Quản lý tài khoản người dùng từ đầu đến cuối — đăng ký, đăng nhập, cấp token, và thông tin hồ sơ.

**Làm gì:**
- Đăng ký tài khoản mới: validate email/username chưa tồn tại, hash password (BCrypt), lưu user
- Đăng nhập: kiểm tra credentials, phát access token (JWT 15 phút) + refresh token (7 ngày, lưu Redis)
- Refresh token: xoay vòng cặp token mới, vô hiệu token cũ
- Đăng xuất: xoá refresh token khỏi Redis
- Cập nhật profile: avatar URL, bio, display name, chế độ public/private
- Nâng role: tự động chuyển USER → CREATOR khi publish quest đầu tiên (idempotent)

**Sở hữu:**
- Bảng `users` (email, username, display_name, password_hash, role, follower_count, following_count, notification_prefs)

**Domain objects:** `User`, `Email`, `Username`, `DisplayName` (value objects với validation riêng), `Role (USER | CREATOR)`

**Giới hạn — KHÔNG làm:**
- Không biết follow relationships (đó là Social service)
- Không gửi email hay notification (đó là Notification service)
- Không quản lý nội dung quest (đó là Quest module)

---

### 2. Quest (Nội dung học tập)

**Trách nhiệm:** Toàn bộ vòng đời của một quest — từ khi creator soạn đến khi learner hoàn thành.

**Làm gì — phía Creator:**
- Tạo quest với cấu trúc cây: Quest → Chapter[] → Task[] → Resource[]
- Quản lý các loại task: LEARN (đọc/xem), QUIZ (trắc nghiệm), SUBMISSION (nộp bài), PRACTICE (luyện tập), REFLECTION (viết suy ngẫm)
- Cài đặt CompletionRule: ALL_TASKS, QUIZ_SCORE (ngưỡng %), SUBMISSION (task type bắt buộc), ALL_OF/ANY_OF (kết hợp)
- Publish / Unpublish quest (DRAFT ↔ PUBLIC)
- Xem analytics: completion rate, task drop-off từng bước

**Làm gì — phía Learner:**
- Fork quest: tạo bản PersonalQuest (snapshot riêng, độc lập với quest gốc)
- Hoàn thành task: validate từng loại task (SUBMISSION cần evidence, REFLECTION cần đủ ký tự, QUIZ dùng endpoint riêng)
- Submit quiz: chấm điểm tự động, lưu QuizAttempt, dùng best attempt để evaluate
- Undo task: thu hồi completion, tính lại tiến độ
- Chỉnh sửa PersonalQuest: thêm/xoá chapter/task, reorder
- Abandon quest

**Sở hữu:**
- `quests`, `chapters`, `tasks`, `resources`
- `learning_paths`
- `personal_quests`, `personal_chapters`, `personal_tasks`
- `quiz_attempts`, `task_completions`
- `skill_domains`

**Domain objects:** `Quest`, `PersonalQuest`, `CompletionRule`, `QuizGrader`, `CompletionEvaluator`

**Publish events:** `quest.published`, `quest.completed`, `quest.reopened`, `quest.forked`, `task.completed`, `task.undone`, `submission.graded` (từ AI service)

**Giới hạn — KHÔNG làm:**
- Không gửi notification (đó là Notification service)
- Không grade submission bằng AI (đó là AI service)
- Không quản lý review/rating (đó là Marketplace module)

---

### 3. Marketplace (Khám phá & Đánh giá)

**Trách nhiệm:** Giúp learner tìm và đánh giá quest. Là lớp public-facing của nội dung học.

**Làm gì:**
- Tìm kiếm quest qua Elasticsearch: full-text, filter theo difficulty/domain/tag
- Gợi ý quest: trending (nhiều fork gần đây), popular (rating cao), mới nhất
- Tạo review: learner đã fork quest mới được review, tối đa 1 review/người/quest
- Quản lý favorites: lưu/bỏ lưu quest vào danh sách yêu thích
- Cập nhật search index: lắng nghe `quest.published` để index Elasticsearch, `quest.forked` để tăng fork_count

**Sở hữu:**
- `reviews` (questId, userId, score, content)
- `favorites` (userId, questId)
- Elasticsearch index `quests` (search-optimized snapshot)

**Domain objects:** `Review`, `Favorite`, `QuestDocument` (Elasticsearch document)

**Giới hạn — KHÔNG làm:**
- Không lưu trữ nội dung quest (chỉ đọc từ Quest module)
- Không biết tiến độ học của learner (đó là Quest module)
- Không xử lý payment hay access control (chưa có)

---

### 4. World (Gamification & Thành tích)

**Trách nhiệm:** Hệ thống gamification — biến hoạt động học thành thế giới ảo trực quan có thể phát triển.

**Làm gì:**
- Tạo World cá nhân khi user đăng ký (lắng nghe `user.registered`)
- Quản lý Districts: mỗi skill domain có một District trong World
- Theo dõi completion count: mỗi task hoàn thành → tăng count District tương ứng, undo → giảm
- Mở khoá Building tự động theo completion count (có ngưỡng cụ thể, idempotent)
- Đánh giá Achievement: sau mỗi `quest.completed`, kiểm tra toàn bộ achievement criteria
- Criteria hiện có: QUEST_COUNT, TASK_COUNT (tổng), DOMAIN_TASK_COUNT (1 domain đạt ngưỡng là đủ)
- Publish `achievement.unlocked` khi đủ điều kiện

**Sở hữu:**
- `worlds`, `districts`, `buildings`, `district_events`
- `achievements`, `user_achievements`

**Domain objects:** `World`, `District`, `Building`, `Achievement`, `BuildingUnlockService`, `AchievementUnlockService`

**Lắng nghe events:** `user.registered`, `task.completed`, `task.undone`, `quest.completed`, `quest.reopened`

**Giới hạn — KHÔNG làm:**
- Không biết nội dung quest hay cấu trúc chapter (chỉ nhận domain ID từ event)
- Không gửi notification (chỉ publish event, Notification service tiêu thụ)

---

### 5. Admin (Quản trị hệ thống)

**Trách nhiệm:** Công cụ cho admin kiểm soát nội dung và cấu hình hệ thống.

**Làm gì:**
- Quản lý Skill Domain: tạo, cập nhật, deactivate các danh mục kỹ năng
- Quản lý Feature Flag: bật/tắt tính năng runtime, không cần deploy lại
- Kiểm duyệt Quest: ẩn/khôi phục quest vi phạm (override visibility)
- Phân quyền: chỉ user có role ADMIN mới truy cập được các endpoint này

**Sở hữu:**
- `skill_domains` (cùng bảng với Quest module nhưng Admin là owner logic)
- `feature_flags`

**Giới hạn — KHÔNG làm:**
- Không can thiệp vào learning progress của user
- Không có dashboard analytics (chưa có)

---

## Go Notification Service (port 8082)

**Trách nhiệm:** Thông báo mọi sự kiện quan trọng đến đúng người dùng, qua đúng kênh.

**Làm gì:**

**Tiêu thụ events từ outbox:**
| Event | Hành động |
|---|---|
| `user.registered` | Cache email user để gửi email về sau |
| `task.completed` | Tạo TASK_COMPLETED notification; nếu `isQuestCompleted=true` thêm QUEST_COMPLETED |
| `quest.completed` | Tạo QUEST_COMPLETED notification |
| `achievement.unlocked` | Tạo ACHIEVEMENT notification |
| `comment.created` | Tạo COMMENT notification cho quest creator |
| `discussion.created` | Tạo COMMENT notification cho quest creator |
| `user.followed` | Tạo FOLLOWED notification cho người bị follow |
| `submission.graded` | Tạo REVIEW notification với kết quả AI chấm |

**Kênh gửi (sau khi tạo notification trong DB):**
- **In-app inbox:** lưu vào DB, trả về qua REST API
- **SSE real-time:** push ngay tới browser/app đang kết nối (`GET /api/v1/notifications/stream`)
- **FCM push:** gửi tới device tokens đã đăng ký (graceful no-op nếu không cấu hình Firebase)
- **Email:** gửi qua SMTP cho QUEST_COMPLETED, ACHIEVEMENT, REVIEW (graceful no-op nếu không cấu hình SMTP)

**REST API:**
- `GET /api/v1/notifications` — inbox của user (pagination)
- `PATCH /api/v1/notifications/:id/read` — đánh dấu đã đọc
- `PATCH /api/v1/notifications/read-all` — đánh dấu tất cả đã đọc
- `GET /api/v1/notifications/unread-count` — badge count
- `GET /api/v1/notifications/stream` — SSE real-time stream
- `POST /api/v1/notifications/broadcast` — admin gửi thông báo hàng loạt
- `POST /api/v1/device-tokens` — đăng ký FCM token
- `DELETE /api/v1/device-tokens` — huỷ đăng ký FCM token

**Sở hữu (trong shared DB):**
- `notifications` — inbox của từng user
- `device_tokens` — FCM tokens theo user/platform
- `notification_user_emails` — cache email từ event

**Giới hạn — KHÔNG làm:**
- Không biết nội dung quest, chi tiết task (chỉ dùng data có trong event payload)
- Không quyết định notification xuất hiện khi nào (đó là phía publisher)
- Không lưu trữ history email (chỉ gửi, không lưu)

---

## Go Social Service (port 8081)

**Trách nhiệm:** Xây dựng lớp social cho nền tảng — ai follow ai, ai làm gì, và trao đổi về quest.

**Làm gì:**

**Follows (Mạng lưới xã hội):**
- Follow/unfollow user (không thể tự follow mình)
- Kiểm tra is-following
- Danh sách following/followers của một user
- Publish `user.followed` vào outbox khi follow (để Notification service thông báo)

**Activity Feed (Dòng thời gian):**
- Tự động tạo activity khi nhận events từ Java monolith:
  - `quest.published` → QUEST_PUBLISHED activity
  - `quest.forked` → QUEST_FORKED activity
  - `quest.completed` → QUEST_COMPLETED activity
  - `task.completed` (non-final) → TASK_COMPLETED activity
  - `achievement.unlocked` → ACHIEVEMENT_UNLOCKED activity
- `GET /api/v1/feed` — activities của những người user đang follow (newest first)
- `GET /api/v1/users/:username/activities` — public activities của một user

**Comments (Bình luận quest):**
- Comment trực tiếp lên quest (không qua discussion thread)
- Hỗ trợ reply tối đa 1 cấp (tổng 2 cấp): root → reply
- Cấu trúc lưu trữ: **fixed-width materialized path** — mỗi node chiếm 10 ký tự số, `ORDER BY path` tự động cho ra đúng thứ tự depth-first, không cần `parent_id` để traverse, parent suy ra bằng `path[:-10]`
- Publish `comment.created` vào outbox khi tạo comment

**Discussions (Thảo luận quest):**
- Mở discussion thread có tiêu đề lên quest
- Reply vào discussion (tối đa 1 cấp), cùng cơ chế materialized path với comments
- Publish `discussion.created` vào outbox khi mở discussion mới

**REST API:** 11 endpoints (`/feed`, `/users/:u/activities`, `/users/:u/follow`, `/users/me/following`, `/users/:u/followers`, `/quests/:id/comments`, `/quests/:id/discussions`, `/discussions/:id/comments`)

**Sở hữu (trong shared DB):**
- `follows`
- `activities`
- `comments` (materialized path)
- `discussions` (materialized path)

**Giới hạn — KHÔNG làm:**
- Không gửi notification trực tiếp (chỉ publish event vào outbox)
- Không lưu thông tin user (chỉ đọc `users` table read-only)
- Không quản lý direct messaging hay group chat

---

## Python AI Service (port 8090)

**Trách nhiệm:** Lớp intelligence của nền tảng — AI chấm bài, gợi ý, sinh nội dung, và coaching.

**Làm gì:**

**Grade — Chấm bài submission:**
- Nhận `personalTaskId` + `evidence` (text hoặc URL)
- Load task từ DB, đọc rubric từ `tasks.config` JSONB (criteria, passThreshold, scoring)
- Snapshot rubric tại thời điểm chấm → lưu vào `submission_grades.rubric_snapshot` (tránh rubric thay đổi làm lịch sử không nhất quán)
- Gọi LLM với rubric + evidence → nhận `{status, score, feedback, criteria[]}`
- Validate output: status phải là PASS/FAIL/NEEDS_REVISION, score trong 0-100
- Nếu PASS → publish `submission.graded` vào outbox (Java monolith consume để complete task)
- Rate limit: 20 lần/ngày/user

**AI Coach — Tư vấn học tập:**
- Tạo session chat gắn với optional `personalQuestId`
- Tool-use loop: LLM có thể gọi 4 tools đọc DB trước khi trả lời:
  - `get_progress(personalQuestId)` — % hoàn thành, tasks done/total
  - `get_streak(userId)` — chuỗi ngày hoạt động liên tiếp
  - `get_achievements(userId)` — danh sách achievement đã mở khoá
  - `get_upcoming_tasks(personalQuestId)` — 3 tasks tiếp theo chưa làm
- Stream response về client qua SSE (word-chunk), hỗ trợ huỷ mid-stream khi client disconnect
- Lưu toàn bộ message history (role: user/assistant/tool) để context liên tục
- Rate limit: 5 sessions/ngày, 60 messages/ngày/user

**Recommend — Gợi ý quest:**
- Nhận `goal` (mô tả mục tiêu học) từ user
- Tìm kiếm Elasticsearch: multi-match trên title (boost x3), description, tags
- LLM rerank: chọn quest phù hợp nhất + giải thích lý do
- Trả về `canGenerate: true` khi không có kết quả phù hợp (gợi ý tạo quest mới)
- Rate limit: 10 lần/giờ/user

**Generate Quest — Sinh quest từ mục tiêu:**
- Nhận `goal` + `domainId`
- LLM sinh cấu trúc quest đầy đủ: title, description, difficulty, chapters[], tasks[] (đúng task type)
- Gọi Java monolith REST API để tạo quest DRAFT dưới tên user
- User review và publish thủ công
- Rate limit: 3 lần/ngày/user

**Infrastructure layer:**
- **Safety:** Phát hiện prompt injection (20+ pattern), redact PII (email, phone, SSN), validate output format
- **Observability:** Structured logging (structlog), tracking token usage + latency + cost per request
- **Versioned prompts:** File `.md` với frontmatter (version, temperature, response_format) — thay đổi prompt không cần sửa code
- **Rate limiting:** DB-based, đếm rows trong window time

**Sở hữu (trong shared DB):**
- `submission_grades` — lịch sử chấm bài với rubric snapshot
- `coach_sessions` — phiên tư vấn
- `coach_messages` — lịch sử chat (role + tool_calls)

**Chỉ đọc:**
- `tasks`, `personal_tasks`, `personal_quests` — để lấy rubric và context
- `task_completions`, `user_achievements`, `activities` — cho coach tools

**Giới hạn — KHÔNG làm:**
- Không trực tiếp complete task (chỉ publish event, Java monolith xử lý)
- Không có quyền ghi vào quest/task data của monolith
- Không lưu trữ conversation history ngoài coach sessions
