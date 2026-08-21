# QuestHub — Database Schema

PostgreSQL · 29 tables · UUID primary keys · TIMESTAMPTZ timestamps · Domain → LearningPath → Quest → Chapter → Task

**Hierarchy cốt lõi:** `skill_domains` → `learning_paths` → `quests` → `chapters` → `tasks`. Bản fork phía user: `personal_quests` → `personal_chapters` → `personal_tasks`. XP/Level không tồn tại. Purchase/Subscription = chỉ là visibility `DRAFT/PUBLIC/HIDDEN`.

---

## Tables — 29 total

| Table | Module |
|---|---|
| `users` | Identity |
| `skill_domains` | Domain |
| `learning_paths` | LearningPath |
| `quests` | Quest |
| `chapters` | Chapter |
| `tasks` | Task |
| `resources` | Resource |
| `worlds` | World |
| `districts` | World |
| `buildings` | World (visual) |
| `personal_quests` | Progress |
| `personal_chapters` | Progress |
| `personal_tasks` | Progress |
| `task_completions` | Progress |
| `quiz_attempts` | Progress |
| `reviews` | Community |
| `comments` | Community |
| `discussions` | Community |
| `achievements` | Gamification |
| `user_achievements` | Gamification |
| `notifications` | Notification |
| `favorites` | Marketplace |
| `follows` | Social |
| `activities` | Feed |
| `feature_flags` | Admin |
| `submission_grades` | AI Grader |
| `coach_sessions` | AI Coach |
| `coach_messages` | AI Coach |
| `outbox_events` | Infra |

---

## Table Definitions

### users

Tài khoản người dùng — cả Creator lẫn Learner

| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | UUID | PK DEFAULT gen_random_uuid() | — |
| email | TEXT | NOT NULL UNIQUE | Case-insensitive — store lowercase |
| username | TEXT | NOT NULL UNIQUE | Lowercase alphanumeric + underscore |
| password_hash | TEXT | NULLABLE | NULL khi đăng nhập qua OAuth |
| role | TEXT | NOT NULL DEFAULT 'LEARNER' | CHECK IN ('LEARNER','CREATOR','ADMIN') |
| avatar_url | TEXT | NULLABLE | Cloudinary URL |
| bio | TEXT | NULLABLE | Max 300 ký tự (validate ở app layer) |
| is_public | BOOLEAN | NOT NULL DEFAULT true | Ảnh hưởng visibility của World / profile |
| follower_count | INT | NOT NULL DEFAULT 0 | Denormalized — update via UserFollowed event |
| following_count | INT | NOT NULL DEFAULT 0 | Denormalized |
| notification_prefs | JSONB | NOT NULL DEFAULT '{}' | Channels: {email, push, in_app} — toggle từng loại |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |
| updated_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | Trigger auto-update |

---

### skill_domains

Đỉnh hierarchy — lĩnh vực kỹ năng lớn (Programming, Language, Fitness...)

| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | UUID | PK | — |
| name | TEXT | NOT NULL UNIQUE | e.g. "Programming" |
| slug | TEXT | NOT NULL UNIQUE | e.g. "programming" |
| description | TEXT | NULLABLE | Mô tả ngắn cho landing/marketplace |
| icon | TEXT | NULLABLE | Emoji icon |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |

---

### learning_paths

Lộ trình hoàn chỉnh đạt một mục tiêu — chứa nhiều Quest

| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | UUID | PK | — |
| domain_id | UUID | NOT NULL FK → skill_domains | Path thuộc một Domain |
| author_id | UUID | NOT NULL FK → users | SET NULL khi user bị xóa |
| title | TEXT | NOT NULL | e.g. "Java Backend Engineer" |
| description | TEXT | NULLABLE | Mục tiêu của path + lộ trình tổng quan |
| difficulty | TEXT | NOT NULL DEFAULT 'BEGINNER' | CHECK IN ('BEGINNER','INTERMEDIATE','ADVANCED') |
| estimated_duration | INT | NOT NULL DEFAULT 0 | Tổng số phút ước tính — tổng của các Quest |
| is_public | BOOLEAN | NOT NULL DEFAULT false | Public = xuất hiện trên Marketplace |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |
| updated_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |

---

### quests

Template bất biến — đơn vị tiến bộ chính của hệ thống

| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | UUID | PK | — |
| learning_path_id | UUID | FK → learning_paths NULLABLE | SET NULL khi path bị xóa. NULL = quest độc lập |
| creator_id | UUID | NOT NULL FK → users | SET NULL khi user bị xóa |
| title | TEXT | NOT NULL | Max 120 ký tự |
| description | TEXT | NULLABLE | Markdown supported |
| difficulty | TEXT | NOT NULL DEFAULT 'BEGINNER' | CHECK IN ('BEGINNER','INTERMEDIATE','ADVANCED') |
| estimated_duration | INT | NOT NULL DEFAULT 0 | Số phút ước tính — tổng của các task |
| completion_rule | JSONB | NOT NULL DEFAULT '{"type":"ALL_TASKS"}' | Rule cấu hình được — xem bên dưới |
| reward | JSONB | NOT NULL DEFAULT '{}' | Intrinsic — {message, icon}. KHÔNG chứa XP/Level |
| visibility | TEXT | NOT NULL DEFAULT 'DRAFT' | CHECK IN ('DRAFT','PUBLIC','HIDDEN') — purchase/subscription = public/private |
| fork_count | INT | NOT NULL DEFAULT 0 | Denormalized — increment via QuestForked event |
| avg_rating | NUMERIC(3,2) | NULLABLE | Denormalized — recalc via QuestRated event |
| rating_count | INT | NOT NULL DEFAULT 0 | Denormalized |
| published_at | TIMESTAMPTZ | NULLABLE | Set khi visibility → PUBLIC |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |
| updated_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |

---

### CompletionRule (JSONB — value object)

Cấu trúc chuẩn của `quests.completion_rule`

```json
{ "type": "ALL_TASKS" }
```
— hoàn thành tất cả task (mặc định)

```json
{ "type": "QUIZ_SCORE", "minScore": 80 }
```
— mọi quiz đạt ≥ 80%

```json
{ "type": "SUBMISSION", "requiredTaskTypes": ["SUBMISSION"] }
```
— có submission cho mọi task loại đó

```json
{ "type": "ALL_OF", "rules": [ {"type":"QUIZ_SCORE","minScore":80}, {"type":"ALL_TASKS"} ] }
```
— kết hợp (AND)

```json
{ "type": "ANY_OF", "rules": [...] }
```
— chỉ cần 1 rule thỏa

Quest Module evaluate rule khi task completed/undone; đạt → status COMPLETED + publish `quest.completed`.

---

### chapters

Chia Quest lớn thành phần nhỏ — nhóm Task

| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | UUID | PK | — |
| quest_id | UUID | NOT NULL FK → quests | ON DELETE CASCADE |
| title | TEXT | NOT NULL | e.g. "Authentication" |
| description | TEXT | NULLABLE | — |
| position | INT | NOT NULL DEFAULT 0 | Thứ tự trong quest |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |
| updated_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |

---

### tasks

Đơn vị nhỏ nhất user thực hiện — có TaskType chuẩn hóa

| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | UUID | PK | — |
| chapter_id | UUID | NOT NULL FK → chapters | ON DELETE CASCADE |
| type | TEXT | NOT NULL | CHECK IN ('LEARN','QUIZ','PRACTICE','SUBMISSION','REFLECTION') |
| title | TEXT | NOT NULL | e.g. "Watch Spring Security Video" |
| description | TEXT | NULLABLE | — |
| order | INT | NOT NULL DEFAULT 0 | Thứ tự trong chapter |
| config | JSONB | NOT NULL DEFAULT '{}' | Type-specific: {passThreshold} cho QUIZ, {minLength} cho REFLECTION |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |
| updated_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |

**TaskType** — LEARN (đọc/xem/nghe) · QUIZ (trắc nghiệm) · PRACTICE (luyện tập) · SUBMISSION (nộp kết quả) · REFLECTION (chia sẻ bài học)

---

### resources

Tài liệu đính kèm Task LEARN

| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | UUID | PK | — |
| task_id | UUID | NOT NULL FK → tasks | ON DELETE CASCADE |
| type | TEXT | NOT NULL | CHECK IN ('VIDEO','ARTICLE','BOOK','DOCUMENT','COURSE','PODCAST','FILE','LINK') |
| title | TEXT | NOT NULL | — |
| url | TEXT | NOT NULL | Link tài liệu |
| estimated_minutes | INT | NULLABLE | Dùng tính estimated_duration của quest |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |

---

### worlds

1:1 với users — tạo tự động khi đăng ký

| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | UUID | PK | — |
| user_id | UUID | NOT NULL FK → users UNIQUE | ON DELETE CASCADE |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |

---

### districts

Khu vực trong World — tạo lazy khi có completion đầu tiên trong domain đó

| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | UUID | PK | — |
| world_id | UUID | NOT NULL FK → worlds | ON DELETE CASCADE |
| skill_domain_id | UUID | NOT NULL FK → skill_domains | District = một Domain |
| completion_count | INT | NOT NULL DEFAULT 0 | Denormalized — tăng khi nhận TaskCompleted event |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |
| updated_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |

**UNIQUE** `(world_id, skill_domain_id)`

---

### buildings

Công trình visualize trong District — THUẦN TRANG TRÍ, loại tùy ý

| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | UUID | PK | — |
| district_id | UUID | NOT NULL FK → districts | ON DELETE CASCADE |
| type | TEXT | NOT NULL | Tự do: HOUSE, SCHOOL, LIBRARY, GYM, LAB... không enforce enum |
| name | TEXT | NULLABLE | Hiển thị trên map |
| position | INT | NOT NULL DEFAULT 0 | Vị trí layout |
| unlocked_at | TIMESTAMPTZ | NULLABLE | Mốc khi mở khóa building (thường theo completion_count của district) |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |

---

### personal_quests

Bản fork của Quest thuộc về Learner — độc lập hoàn toàn

| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | UUID | PK | — |
| user_id | UUID | NOT NULL FK → users | ON DELETE CASCADE |
| quest_id | UUID | FK → quests NULLABLE | SET NULL nếu quest gốc bị xóa |
| learning_path_id | UUID | FK → learning_paths NULLABLE | Path mà quest này thuộc về (khi fork từ trong path) |
| title | TEXT | NOT NULL | Copy từ quest lúc fork |
| completion_rule | JSONB | NOT NULL | Snapshot của rule lúc fork — bản gốc đổi không ảnh hưởng |
| status | TEXT | NOT NULL DEFAULT 'ACTIVE' | CHECK IN ('ACTIVE','COMPLETED','ABANDONED') |
| progress | INT | NOT NULL DEFAULT 0 | 0–100, denormalized. Recalc khi task completed/undone |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |
| updated_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |
| completed_at | TIMESTAMPTZ | NULLABLE | Set khi completion_rule thỏa |

**UNIQUE** `(user_id, quest_id)` WHERE quest_id IS NOT NULL — mỗi user chỉ fork một quest một lần

---

### personal_chapters

Chapters của PersonalQuest — copy từ template hoặc user tự thêm

| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | UUID | PK | — |
| personal_quest_id | UUID | NOT NULL FK → personal_quests | ON DELETE CASCADE |
| source_chapter_id | UUID | FK → chapters NULLABLE | NULL = user tự thêm. SET NULL khi chapter gốc xóa |
| title | TEXT | NOT NULL | — |
| description | TEXT | NULLABLE | — |
| position | INT | NOT NULL DEFAULT 0 | — |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |
| updated_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |

---

### personal_tasks

Tasks của PersonalChapter — nơi ghi nhận trạng thái hoàn thành

| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | UUID | PK | — |
| personal_chapter_id | UUID | NOT NULL FK → personal_chapters | ON DELETE CASCADE |
| source_task_id | UUID | FK → tasks NULLABLE | NULL = user tự thêm. SET NULL khi task gốc xóa |
| type | TEXT | NOT NULL | Copy từ task gốc |
| title | TEXT | NOT NULL | — |
| description | TEXT | NULLABLE | — |
| order | INT | NOT NULL DEFAULT 0 | — |
| config | JSONB | NOT NULL DEFAULT '{}' | Snapshot type-config lúc fork |
| is_completed | BOOLEAN | NOT NULL DEFAULT false | Denormalized — tránh JOIN khi tính progress |
| completed_at | TIMESTAMPTZ | NULLABLE | — |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |
| updated_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |

---

### task_completions

Bản ghi khi Learner hoàn thành một Task — source of truth cho World

| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | UUID | PK | — |
| personal_task_id | UUID | NOT NULL FK → personal_tasks UNIQUE | UNIQUE enforces "mỗi task complete 1 lần" |
| evidence | JSONB | NOT NULL DEFAULT '{}' | {submissionUrl, text, ...} — bắt buộc cho SUBMISSION/REFLECTION |
| completed_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | Thời điểm thật sự hoàn thành |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |

Xóa row này = undo completion. ON DELETE CASCADE từ personal_tasks.

---

### quiz_attempts

Kết quả từng lần làm quiz — dùng để evaluate CompletionRule

| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | UUID | PK | — |
| personal_task_id | UUID | NOT NULL FK → personal_tasks | ON DELETE CASCADE |
| score | NUMERIC(5,2) | NOT NULL | — |
| max_score | NUMERIC(5,2) | NOT NULL | — |
| passed | BOOLEAN | NOT NULL DEFAULT false | score/max_score ≥ passThreshold trong task.config |
| answers | JSONB | NOT NULL DEFAULT '{}' | Lưu câu trả lời để review |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |

---

### reviews

Đánh giá 1–5 sao + nội dung text — chỉ sau khi fork quest

| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | UUID | PK | — |
| user_id | UUID | NOT NULL FK → users | ON DELETE CASCADE |
| quest_id | UUID | NOT NULL FK → quests | ON DELETE CASCADE |
| score | INT | NOT NULL CHECK(1–5) | — |
| content | TEXT | NULLABLE | Nội dung review — tùy chọn |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |
| updated_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |

**UNIQUE** `(user_id, quest_id)`

---

### discussions

Chủ đề thảo luận gắn với Quest — hỏi đáp, chia sẻ kinh nghiệm

| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | UUID | PK | — |
| quest_id | UUID | FK → quests NULLABLE | NULL = discussion toàn cộng đồng (không gắn quest) |
| author_id | UUID | NOT NULL FK → users | ON DELETE CASCADE |
| title | TEXT | NOT NULL | — |
| body | TEXT | NOT NULL | Markdown supported |
| status | TEXT | NOT NULL DEFAULT 'OPEN' | CHECK IN ('OPEN','CLOSED') |
| is_pinned | BOOLEAN | NOT NULL DEFAULT false | Admin pin topic nổi bật |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |
| updated_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |

---

### comments

Bình luận — cho phép reply lồng nhau qua parent_id

| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | UUID | PK | — |
| user_id | UUID | NOT NULL FK → users | ON DELETE CASCADE |
| target_type | TEXT | NOT NULL | CHECK IN ('DISCUSSION','QUEST') |
| target_id | UUID | NOT NULL | ID của discussion hoặc quest |
| parent_id | UUID | FK → comments NULLABLE | NULL = comment gốc. Reply lồng nhau tối đa 2 cấp |
| body | TEXT | NOT NULL | Max 2000 ký tự |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |

---

### achievements

Thành tựu intrinsic — không gắn XP/Level, chỉ gắn mốc tiến bộ thật

| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | UUID | PK | — |
| code | TEXT | NOT NULL UNIQUE | e.g. 'FIRST_QUEST_COMPLETED' |
| title | TEXT | NOT NULL | e.g. "First Blood" |
| description | TEXT | NOT NULL | — |
| icon | TEXT | NULLABLE | Emoji/icon |
| criteria_type | TEXT | NOT NULL | CHECK IN ('FIRST_QUEST','QUEST_COUNT','TASK_COUNT','DOMAIN_TASK_COUNT','STREAK_DAYS') |
| criteria_value | INT | NOT NULL DEFAULT 1 | Ngưỡng — e.g. 10 task completed |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |

---

### user_achievements

Achievement đã mở khóa bởi user

| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | UUID | PK | — |
| user_id | UUID | NOT NULL FK → users | ON DELETE CASCADE |
| achievement_id | UUID | NOT NULL FK → achievements | ON DELETE CASCADE |
| unlocked_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |

**UNIQUE** `(user_id, achievement_id)`

---

### notifications

Thông báo trong app — push/email đi qua Notification Service

| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | UUID | PK | — |
| user_id | UUID | NOT NULL FK → users | ON DELETE CASCADE |
| type | TEXT | NOT NULL | CHECK IN ('TASK_COMPLETED','QUEST_COMPLETED','ACHIEVEMENT','FOLLOWED','COMMENT','REVIEW','ADMIN') |
| title | TEXT | NOT NULL | — |
| body | TEXT | NULLABLE | — |
| payload | JSONB | NOT NULL DEFAULT '{}' | {questId, activityId...} — deep link |
| is_read | BOOLEAN | NOT NULL DEFAULT false | — |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |

---

### favorites

Quest được user lưu lại — không cần fork

| Column | Type | Constraints | Notes |
|---|---|---|---|
| user_id | UUID | PK FK → users | ON DELETE CASCADE |
| quest_id | UUID | PK FK → quests | ON DELETE CASCADE |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |

---

### follows

Quan hệ follow giữa users

| Column | Type | Constraints | Notes |
|---|---|---|---|
| follower_id | UUID | PK FK → users | ON DELETE CASCADE |
| following_id | UUID | PK FK → users | ON DELETE CASCADE |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |

CHECK `follower_id != following_id` — không tự follow bản thân

---

### activities

Sự kiện của user — nguồn dữ liệu cho Feed

| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | UUID | PK | — |
| user_id | UUID | NOT NULL FK → users | ON DELETE CASCADE |
| type | TEXT | NOT NULL | CHECK IN ('QUEST_COMPLETED','QUEST_FORKED','TASK_COMPLETED','QUEST_PUBLISHED','ACHIEVEMENT_UNLOCKED') |
| payload | JSONB | NOT NULL DEFAULT '{}' | Chứa questId, title, chapterTitle... để render feed không cần JOIN |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |

---

### feature_flags

Feature toggles — cập nhật runtime không cần redeploy

| Column | Type | Constraints | Notes |
|---|---|---|---|
| key | TEXT | PK | e.g. 'ai_quest_generation' |
| is_enabled | BOOLEAN | NOT NULL DEFAULT false | — |
| description | TEXT | NULLABLE | — |
| updated_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |

---

### outbox_events

Transactional Outbox — đảm bảo domain events được deliver ít nhất 1 lần (at-least-once)

| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | UUID | PK DEFAULT gen_random_uuid() | Idempotency key cho consumers |
| aggregate_type | TEXT | NOT NULL | 'Quest', 'PersonalQuest', 'User', 'Task', v.v. |
| aggregate_id | UUID | NOT NULL | ID của aggregate đã raise event này |
| event_type | TEXT | NOT NULL | 'task.completed', 'quest.forked', v.v. — khớp với routing key |
| payload | JSONB | NOT NULL | Full event payload — schema định nghĩa trong Event Contracts |
| status | TEXT | NOT NULL DEFAULT 'PENDING' | CHECK IN ('PENDING','PROCESSING','PROCESSED','FAILED') |
| retry_count | INT | NOT NULL DEFAULT 0 | Relay tăng lên mỗi lần retry. Max 5 → chuyển FAILED |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |
| processed_at | TIMESTAMPTZ | NULLABLE | Set khi status → PROCESSED |

- Relay dùng `SELECT ... FOR UPDATE SKIP LOCKED` để tránh nhiều relay thread xử lý cùng 1 row.
- Row INSERT trong **cùng transaction** với DB write chính (task_completions, v.v.) — đây là core của Outbox Pattern.
- Nếu transaction rollback → outbox row cũng rollback → không bao giờ deliver ghost event.

---

### submission_grades

Kết quả AI chấm bài task SUBMISSION/PRACTICE — status + score + feedback (AI Grader)

| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | UUID | PK | — |
| user_id | UUID | NOT NULL FK → users | ON DELETE CASCADE |
| personal_task_id | UUID | NOT NULL FK → personal_tasks | ON DELETE CASCADE |
| quest_id | UUID | NOT NULL FK → quests | Denorm — biết rubric mà không JOIN |
| attempt_no | INT | NOT NULL DEFAULT 1 | Lần nộp thứ mấy của task này |
| status | TEXT | NOT NULL | CHECK IN ('PASS','FAIL','NEEDS_REVISION') |
| score | NUMERIC(5,2) | NOT NULL CHECK(0–100) | Điểm theo rubric |
| feedback | TEXT | NOT NULL | Feedback cụ thể theo từng tiêu chí rubric |
| rubric_snapshot | JSONB | NOT NULL DEFAULT '{}' | Snapshot rubric lúc chấm — chống thay đổi sau |
| model | TEXT | NOT NULL | Claude model dùng để chấm |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |
| updated_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |

- Mỗi lần nộp = 1 row. Kết quả mới nhất = row có `attempt_no` cao nhất (hoặc created_at mới nhất).
- `task_completions.personal_task_id` UNIQUE vẫn chặn duplicate completion — PASS chỉ tạo completion 1 lần.

---

### coach_sessions

Phiên trò chuyện với AI Coach — gắn context 1 PersonalQuest (tùy chọn)

| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | UUID | PK | — |
| user_id | UUID | NOT NULL FK → users | ON DELETE CASCADE |
| personal_quest_id | UUID | FK → personal_quests NULLABLE | NULL = chat tổng quát |
| title | TEXT | NULLABLE | AI tự đặt từ chủ đề chat |
| status | TEXT | NOT NULL DEFAULT 'ACTIVE' | CHECK IN ('ACTIVE','CLOSED') |
| context | JSONB | NOT NULL DEFAULT '{}' | Snapshot progress context cho agent (không query lại mỗi turn) |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |
| updated_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |

---

### coach_messages

Tin nhắn trong session coach — role USER/ASSISTANT/TOOL, lưu cả tool_calls

| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | UUID | PK | — |
| session_id | UUID | NOT NULL FK → coach_sessions | ON DELETE CASCADE |
| role | TEXT | NOT NULL | CHECK IN ('USER','ASSISTANT','TOOL') |
| content | TEXT | NULLABLE | Null khi chỉ có tool_calls |
| tool_calls | JSONB | NULLABLE | Danh sách tool + args + result — để trace agent |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | — |

---

## Indexes

| Table | Columns | Purpose |
|---|---|---|
| users | email | Login lookup |
| users | username | Profile URL lookup |
| learning_paths | domain_id, is_public | Browse paths by domain |
| learning_paths | author_id | My paths |
| quests | learning_path_id | Quest list trong path |
| quests | creator_id | My quests list |
| quests | visibility, published_at DESC | Browse PUBLIC + trending |
| quests | avg_rating DESC, rating_count DESC | Popular quests sort |
| chapters | quest_id, position | Ordered chapter list |
| tasks | chapter_id, order | Ordered task list |
| resources | task_id | Resource list |
| personal_quests | user_id, status | My active/completed quests |
| personal_quests | learning_path_id | Progress per learning path |
| personal_chapters | personal_quest_id, position | Ordered chapters |
| personal_tasks | personal_chapter_id, order | Ordered tasks |
| quiz_attempts | personal_task_id, created_at DESC | Latest quiz attempt |
| reviews | quest_id | Recalculate avg rating |
| discussions | quest_id, created_at DESC | Discussion list per quest |
| comments | target_type, target_id, created_at | Comment thread load |
| notifications | user_id, is_read, created_at DESC | Inbox + unread count |
| favorites | user_id, created_at DESC | My favorites list |
| follows | following_id | Ai đang follow user này |
| activities | user_id, created_at DESC | User activity timeline |
| districts | world_id, completion_count DESC | World render — biggest districts first |
| buildings | district_id, position | Building layout render |
| user_achievements | user_id, unlocked_at DESC | Achievement timeline |
| submission_grades | personal_task_id, attempt_no DESC | Lịch sử chấm của 1 task |
| submission_grades | user_id, created_at DESC | Grading history của user |
| coach_sessions | user_id, updated_at DESC | Danh sách session của user |
| coach_messages | session_id, created_at | Lịch sử chat của session |
| outbox_events | status, created_at WHERE status='PENDING' (partial) | Relay polling — chỉ scan PENDING rows, không scan PROCESSED |

---

## Denormalized Fields — Tại sao & Khi nào cập nhật

**quests.fork_count**
Increment khi nhận `QuestForked` event. Dùng cho sort "Popular" mà không cần COUNT(personal_quests).

**quests.avg_rating + rating_count**
Recalculate khi nhận `QuestRated`. Công thức incremental: new_avg = (old_avg × old_count + new_score) / (old_count + 1).

**personal_quests.progress**
Recalculate ngay trong Quest Module khi task completed/undone: `completed / total × 100`. Tránh JOIN nhiều bảng mỗi lần render.

**personal_tasks.is_completed**
Mirror của existence trong `task_completions`. Set true khi insert completion, false khi delete. Giúp tính progress nhanh.

**districts.completion_count**
Increment khi World Module nhận `TaskCompleted` event. Source of truth để render District size + mở khóa building.

**users.follower_count / following_count**
Update async khi nhận `UserFollowed`. Tránh COUNT(follows) mỗi lần render profile.

**activities.payload (JSONB)**
Snapshot của quest title, username, chapter title tại thời điểm activity xảy ra. Feed render không cần JOIN nhiều bảng, tránh ghost data nếu quest bị xóa.
