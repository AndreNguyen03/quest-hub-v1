# QuestHub — API Design

REST · Base URL: `/api` · JWT Bearer authentication · JSON request & response

---

**Pagination** — Tất cả list endpoints hỗ trợ `?page=1&limit=20`. Response bao gồm `{ data: [], meta: { total, page, limit, totalPages } }`.

**Errors** — `{ error: { code: "QUEST_NOT_FOUND", message: "..." } }`. HTTP status codes chuẩn: 400 validation, 401 unauthenticated, 403 forbidden, 404 not found, 409 conflict.

**Routing** — Phase 1: tất cả đi qua `/api`, gateway/router forward tới module tương ứng (Java modules trong 1 Spring Boot app, Social/Notification là Go apps, AI là Python). Phase 2: mỗi service có host riêng.

---

## Modules — 8 tổng

| Module | Base Path | Endpoints |
|---|---|---|
| Identity Module | `/auth`, `/users` | 7 endpoints |
| Quest Module | `/learning-paths`, `/quests`, `/personal-quests` | 28 endpoints |
| Marketplace Module | `/marketplace` | 11 endpoints |
| World Module | `/world` | 4 endpoints |
| Social Module | `/feed`, `/discussions`, `/comments` | 11 endpoints |
| AI Module | `/ai` | 6 endpoints |
| Notification Module | internal only | no public endpoints |
| Admin Module | `/admin` | 9 endpoints |

---

## Identity Module

Base: `/api/v1/auth` · `/api/v1/users`

### `POST /auth/register` — Public

Tạo tài khoản mới (email hoặc OAuth). World được tạo tự động — Identity publish `user.registered`.

```json
// Request body
{ "email": "user@example.com", "username": "john_doe", "displayName": "John Doe", "password": "..." }

// Response 201
"data": { "accessToken": "eyJ...", "refreshToken": "..." }
```

### `POST /auth/login` — Public

Đăng nhập bằng email/password. Trả về accessToken (15 phút) và refreshToken (7 ngày, httpOnly cookie).

### `POST /auth/refresh` — Public

Rotate refresh token. Gửi refreshToken trong httpOnly cookie, nhận accessToken mới.

### `POST /auth/logout` — Auth

Xóa refresh token khỏi Redis. Invalidate session hiện tại.

### `GET /users/me` — Auth

Profile của current user, bao gồm stats denorm (quest count, follower_count, following_count).

### `PUT /users/me` — Auth

Cập nhật avatar, bio, is_public. Không cho đổi email/username qua endpoint này.

### `GET /users/:username` — Public

Public profile của user. Nếu is_public=false, trả 404 với visitor chưa follow.

---

## Quest Module

Base: `/api/v1/learning-paths` · `/api/v1/quests` · `/api/v1/personal-quests`

### `POST /learning-paths` — Auth (creator)

Tạo learning path mới — mặc định private. Phải thuộc một `skill_domain`.

```json
// Request body
{ "title": "Java Backend Engineer", "description": "...", "domainId": "uuid", "difficulty": "INTERMEDIATE" }
```

### `GET /learning-paths/:id` — Public (if public)

Chi tiết path + danh sách quest. Path PUBLIC accessible by anyone; private chỉ author.

### `PUT /learning-paths/:id` — Auth (author only)

Sửa title, description, difficulty, is_public.

### `POST /learning-paths/:id/publish` — Auth (author only)

Path is_public = true → xuất hiện trong Marketplace. Unpublish qua PUT với is_public=false.

### `POST /quests` — Auth (creator)

Tạo quest mới ở trạng thái DRAFT. Chưa xuất hiện trong Marketplace.

```json
// Request body — completionRule là cấu hình được
{
  "title": "Spring Security Fundamentals",
  "description": "...",
  "learningPathId": "uuid", // nullable
  "difficulty": "INTERMEDIATE",
  "completionRule": { "type": "ALL_TASKS" },
  "reward": { "message": "...", "icon": "🎖️" }
}
```

### `GET /quests/:id` — Public (if PUBLIC)

Chi tiết quest + chapters + tasks + resources. PUBLIC quests accessible by anyone. DRAFT chỉ creator.

### `PUT /quests/:id` — Auth (creator only)

Cập nhật metadata (title, description, difficulty, completionRule, reward). PUBLIC quest chỉ sửa được metadata, không sửa chapters/tasks.

### `POST /quests/:id/publish` — Auth (creator only)

Publish quest → visibility = PUBLIC. Phải có ít nhất 1 chapter với 1 task. Triggers `quest.published`.

### `POST /quests/:id/unpublish` — Auth (creator only)

Unpublish → visibility = DRAFT. Quest biến mất khỏi Marketplace. PersonalQuests không bị ảnh hưởng.

### `POST /quests/:questId/chapters` — Auth (creator only)

Thêm chapter vào quest (title, description, position). Chỉ được khi quest chưa có fork nào.

### `PUT /quests/:questId/chapters/:chapterId` — Auth (creator only)

Sửa title, description, position của chapter.

### `DELETE /quests/:questId/chapters/:chapterId` — Auth (creator only)

Xóa chapter. 409 nếu quest đã có fork.

### `POST /quests/:questId/chapters/:chapterId/tasks` — Auth (creator only)

Thêm task vào chapter — có `type` (LEARN/QUIZ/PRACTICE/SUBMISSION/REFLECTION) và config type-specific (passThreshold, minLength...).

### `PUT /quests/:questId/tasks/:taskId` — Auth (creator only)

Sửa title, description, type, config, order của task.

### `DELETE /quests/:questId/tasks/:taskId` — Auth (creator only)

Xóa task. 409 nếu quest đã có fork.

### `POST /tasks/:taskId/resources` — Auth (creator only)

Gắn resource vào task LEARN (VIDEO/ARTICLE/BOOK/DOCUMENT/COURSE/PODCAST/FILE/LINK).

### `DELETE /resources/:resourceId` — Auth (creator only)

Xóa resource.

### `POST /quests/:id/fork` — Auth

Fork quest → tạo PersonalQuest + copy chapters/tasks + snapshot completion_rule. 409 nếu đã fork rồi. Triggers `quest.forked`.

```json
// Response 201
{ "personalQuestId": "uuid", "title": "Spring Security Fundamentals", "progress": 0 }
```

### `GET /users/me/quests` — Auth

Quests do current user tạo ra. Filter: `?visibility=DRAFT|PUBLIC|HIDDEN`.

### `GET /quests/:id/analytics` — Auth (creator only)

Fork count, completion rate, avg rating, task drop-off theo chapter/task. Chỉ creator xem được.

### `GET /personal-quests` — Auth

Danh sách personal quests của current user. Filter: `?status=ACTIVE` | `COMPLETED` | `ABANDONED`.

### `GET /personal-quests/:id` — Auth (owner only)

Chi tiết personal quest + chapters + tasks + completion status từng task.

### `POST /personal-quests/:id/chapters` — Auth (owner only)

Thêm custom chapter vào PersonalQuest (source_chapter_id = null).

### `POST /personal-quests/:id/chapters/:chapterId/tasks` — Auth (owner only)

Thêm custom task. Không ảnh hưởng quest gốc hay fork khác.

### `PATCH /personal-quests/:id/tasks/:taskId/complete` — Auth (owner only)

Hoàn thành task theo type: LEARN/PRACTICE/REFLECTION — tick là xong (REFLECTION bắt buộc `evidence.text` ≥ minLength); SUBMISSION bắt buộc `evidence.submissionUrl`. QUIZ → 409, dùng quiz-attempts. Recalc progress + evaluate completion_rule; thỏa → status COMPLETED + publish `task.completed` / `quest.completed`.

```json
// Request body
{ "evidence": { "submissionUrl": "https://..." } } // optional cho LEARN/PRACTICE

// Response 200
{ "progress": 75, "isCompleted": false, "completedAt": null }
```

### `DELETE /personal-quests/:id/tasks/:taskId/complete` — Auth (owner only)

Undo completion — xóa TaskCompletion. Recalculate progress. Không revert quest status nếu đã COMPLETED.

### `POST /personal-quests/:id/tasks/:taskId/quiz-attempts` — Auth (owner only)

Nộp kết quả quiz → tạo QuizAttempt (score, max_score). Nếu score ≥ passThreshold → task tự hoàn thành.

```json
// Request body
{ "score": 9, "maxScore": 10, "answers": { "q1": "a" } }

// Response 201
{ "attemptId", "passed": true, "taskCompleted": true }
```

### `GET /personal-quests/:id/tasks/:taskId/quiz-attempts` — Auth (owner only)

Lịch sử các lần làm quiz — hiển thị score từng attempt.

---

## Marketplace Module

Base: `/api/v1/marketplace` · `/api/v1/quests/:id/reviews`

### `GET /marketplace/quests` — Public

Search + filter quests. Query params: `?q=keyword&domain=programming&difficulty=INTERMEDIATE&sort=popular|trending|recent&page=1&limit=20`. Backed by Elasticsearch.

```json
// Response 200
{
  "data": [{ "id", "title", "creator", "domain", "difficulty", "forkCount", "avgRating", "taskCount" }],
  "meta": { "total": 142, "page": 1, "limit": 20, "totalPages": 8 }
}
```

### `GET /marketplace/learning-paths` — Public

Browse learning paths theo domain. Filter: `?domain=programming`. Path PUBLIC mới xuất hiện.

### `GET /marketplace/trending` — Public

Top 20 quests trending trong 7 ngày qua. Cached trong Redis (TTL 15 phút). Algo: fork_count + completion_count trong window.

### `GET /marketplace/popular` — Public

Top quests mọi thời đại theo avg_rating × log(fork_count + 1). Cached Redis (TTL 1 giờ).

### `GET /quests/:id/reviews` — Public

Danh sách reviews của quest. Paginated, sorted by created_at DESC.

### `POST /quests/:id/reviews` — Auth

Tạo review (score 1–5 + content tùy chọn). 403 nếu user chưa fork quest. 409 nếu đã review rồi. Triggers `quest.rated` (CREATED).

```json
// Request body
{ "score": 5, "content": "Rất chi tiết, hữu ích!" }
```

### `PUT /quests/:id/reviews/me` — Auth

Cập nhật review của current user. Triggers `quest.rated` (UPDATED) để recalc avg_rating.

### `DELETE /quests/:id/reviews/me` — Auth

Xóa review. Triggers `quest.rated` (DELETED).

### `POST /quests/:id/favorite` — Auth

Lưu quest vào favorites. Idempotent.

### `DELETE /quests/:id/favorite` — Auth

Xóa khỏi favorites.

### `GET /users/me/favorites` — Auth

Danh sách quests đã favorite. Paginated, sorted by created_at DESC.

---

## World Module

Base: `/api/v1/world`

### `GET /world` — Auth

World của current user — tất cả districts (skill_domain + completion_count) + buildings + achievements đã unlock.

### `GET /world/users/:username` — Public (if user is public)

World của user khác (resource `world`). 404 nếu user.is_public = false.

### `GET /world/districts/:skillDomainId` — Auth

Chi tiết một district — quest đã complete, quest đang active, tổng task hoàn thành, danh sách buildings.

### `GET /world/achievements` — Auth

Danh sách achievements của current user + unlocked_at. `?onlyLocked=true` để xem achievements chưa mở.

---

## Social Module

Base: `/api/v1/feed` · `/api/v1/discussions` · `/api/v1/comments` · `/api/v1/users/:username`

### `GET /feed` — Auth

Activity feed của những người current user đang follow. Paginated. Sort: created_at DESC. Types: QUEST_COMPLETED, QUEST_FORKED, TASK_COMPLETED, QUEST_PUBLISHED, ACHIEVEMENT_UNLOCKED.

### `GET /users/:username/activities` — Public (if user is public)

Public activities của một user. Không hiện TASK_COMPLETED (quá nhiều noise).

### `POST /users/:username/follow` — Auth

Follow user. Idempotent. Social INSERT follows → publish `user.followed` để Identity cập nhật follower_count.

### `DELETE /users/:username/follow` — Auth

Unfollow user. Update denormalized counts tương ứng.

### `GET /users/me/following` — Auth

Danh sách người current user đang follow. Paginated.

### `GET /users/:username/followers` — Public (if user is public)

Danh sách follower của user. Paginated.

### `GET /quests/:id/discussions` — Public

Danh sách discussions gắn với quest. Filter: `?status=OPEN|CLOSED`. Paginated.

### `POST /quests/:id/discussions` — Auth

Tạo discussion trên quest. Triggers `discussion.created` → Notification thông báo creator quest.

### `GET /discussions/:id/comments` — Public

Thread comment của discussion. Paginated.

### `POST /discussions/:id/comments` — Auth

Comment vào discussion. `parentId` để reply (tối đa 2 cấp). Triggers `comment.created`.

### `POST /quests/:id/comments` — Auth

Comment trực tiếp lên quest (target_type = QUEST). Triggers `comment.created` → thông báo creator.

---

## AI Module

Base: `/api/v1/ai`

### `POST /ai/recommend` — Auth

Gợi ý quests có sẵn phù hợp với mục tiêu. Input là text. Claude API xử lý + search Elasticsearch. Rate-limited: 10 req/hour per user.

```json
// Request
{ "goal": "I want to become a backend developer in 6 months" }

// Response 200
{ "recommendations": [{ "quest": {...}, "reason": "Covers PostgreSQL and REST APIs..." }],
  "canGenerate": true // true nếu không có quest phù hợp
}
```

### `POST /ai/generate-quest` — Auth

Sinh quest mới từ mục tiêu — chapters + tasks có type phù hợp. Quest được tạo dưới tên user, status DRAFT, user edit trước khi publish. Rate-limited: 3 req/day per user.

```json
// Request
{ "goal": "Learn Docker and container deployment", "domainId": "uuid" }

// Response 201 — quest is created as DRAFT
{ "questId": "uuid", "title": "Docker Fundamentals", "taskCount": 8 }
```

### `POST /ai/grade` — Auth

Chấm bài task SUBMISSION/PRACTICE theo rubric trong task.config. Kết quả PASS → tự tạo TaskCompletion qua event `submission.graded`. Rate-limited: 20 req/day per user.

```json
// Request
{ "personalTaskId": "uuid", "evidence": { "text": "...", "submissionUrl": "..." } }

// Response 200
{ "gradeId": "uuid", "status": "PASS", "score": 92,
  "feedback": "Đúng trọng tâm rubric...", "gradedAt": "..." }
```

### `POST /ai/coach/sessions` — Auth

Tạo phiên coach mới. Body tùy chọn `personalQuestId` để gắn context quest cụ thể. Rate-limited: 5 sessions/day.

```json
// Request
{ "personalQuestId": "uuid" }

// Response 201
{ "sessionId": "uuid", "status": "ACTIVE", "title": "Spring Boot Quest" }
```

### `GET /ai/coach/sessions` — Auth

Danh sách phiên coach của user (mới nhất trước). Hỗ trợ `?status=ACTIVE`.

### `POST /ai/coach/sessions/:id/messages` — Auth

Gửi tin nhắn. AI dùng tool calling đọc progress thật (get_progress, get_streak, get_achievements, get_upcoming_tasks) rồi trả lời streaming qua SSE. Rate-limited: 60 messages/day.

```json
// Request
{ "content": "Tôi đang kẹt ở đâu trong quest này?" }

// Response 200 — text/event-stream (SSE)
data: {"delta":"Bạn đang ở Chapter 2 · Task 3 (Docker volumes)..."}
```

### `GET /ai/coach/sessions/:id` — Auth

Lịch sử tin nhắn của session (role USER/ASSISTANT/TOOL). Dùng để hiển thị lại chat khi load lại trang.

---

## Admin Module

Base: `/api/v1/admin` — requires role: ADMIN

### `GET /admin/quests` — Admin

Tất cả quests với visibility filter. Hỗ trợ sort, search, filter theo visibility.

### `POST /admin/quests/:id/hide` — Admin

Ẩn quest vi phạm → visibility = HIDDEN. Creator nhận notification. Quest không còn xuất hiện trong Marketplace.

### `POST /admin/quests/:id/restore` — Admin

Khôi phục quest HIDDEN về PUBLIC.

### `GET /admin/skill-domains` — Admin

Tất cả skill domains (kể cả ẩn). Grouped listing cho admin panel.

### `POST /admin/skill-domains` — Admin

Tạo skill domain mới (name, slug, description, icon). Ảnh hưởng LearningPath lẫn District.

### `PUT /admin/skill-domains/:id` — Admin

Update name, description, icon, is_active. Deactivate không xóa — chỉ ẩn khỏi Marketplace filter.

### `GET /admin/feature-flags` — Admin

Danh sách tất cả feature flags và trạng thái hiện tại.

### `PUT /admin/feature-flags/:key` — Admin

Toggle flag. Hiệu lực ngay — services read từ Redis cache (TTL 30s), không cần restart.

### `GET /admin/users` — Admin

Danh sách users. Filter theo email, username. Paginated.
