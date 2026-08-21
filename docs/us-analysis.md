# QuestHub — US Analysis theo Sprint (Spec · Plan · Implement · Verify)

31 user stories sắp xếp theo thứ tự implement · đọc từ Sprint 1 → Sprint 16 · tham chiếu roadmap.html

---

## Lộ trình implement (đọc theo thứ tự này)

| Sprint | Tháng | Module / Chủ đề | US theo thứ tự làm |
|--------|-------|-----------------|-------------------|
| Sprint 1 | M1 | Identity | US-20 → US-21 |
| Sprint 2 | M1 | Quest CRUD | US-01 → US-02 → US-03 → US-04 → US-10 |
| Sprint 3 | M2 | Fork + Task Completion + Quiz | US-05 → US-06 → US-07 |
| Sprint 4 | M2 | Completion engine + PersonalQuest edit | US-08 → US-09 |
| Sprint 5 | M3 | World + Marketplace browse | US-16 → US-17 → US-18 → US-19 → US-11 |
| Sprint 7 | M4 | Search + Review + Favorite | US-12 → US-13 → US-14 |
| Sprint 8 | M4 | Quest Analytics | US-15 |
| Sprint 9 | M5 | Social (Go service) | US-22 → US-23 → US-24 |
| Sprint 11 | M6 | Web Next.js — Auth + Marketplace + Quest Detail | FE-01 → FE-02 → FE-03 |
| Sprint 12 | M6 | Web — Dashboard + Create Quest + Tracker | FE-04 → FE-05 → FE-06 |
| Sprint 13 | M7 | Web — World + Profile + Feed | FE-07 → FE-08 → FE-09 |
| Sprint 14 | M7 | Web — Analytics + AI + Deploy staging | FE-10 → FE-11 → FE-12 |
| Sprint 15 | M8 | AI (Python service) | US-25 → US-26 → US-30 → US-31 |
| Sprint 16 | M8 | Admin | US-27 → US-28 → US-29 |
| Sprint 21 | M11 | React Native — Home + Task Check + My Quests | FE-13 → FE-14 |
| Sprint 22 | M11 | RN — World + Profile + Notifications + Search | FE-15 → FE-16 |
| Sprint 23 | M12 | Admin page | FE-17 → FE-18 |
| Sprint 24 | M12 | Landing page + polish + deploy prod | FE-19 → FE-20 |

---

## Cách đọc file này

- **SPEC** — Yêu cầu & acceptance criteria (từ modules-user-stories.html)
- **PLAN** — Thiết kế: bảng DB, endpoint, domain entity/VO, event (từ database-schema.html + api-design.html + high-level-design.html)
- **IMPLEMENT** — Thứ tự code: file/package cần viết theo lớp DDD (domain → application → infrastructure → presentation)
- **VERIFY** — Cách kiểm chứng: unit test, integration test, curl/manual e2e

---

## Sprint 1 · M1 · Identity — bắt đầu ở đây

### US-20 — Đăng ký

Identity · Sprint 1 / M1 · **DONE**

*As a Guest, I want to create an account, so that I can start tracking my progress.*

**SPEC**
- Đăng ký bằng email hoặc OAuth
- World được tạo tự động sau khi đăng ký
- Profile mặc định là public

**PLAN**
- DB: `users` (id, email UNIQUE, username UNIQUE, password_hash, role LEARNER, is_public=true, avatar, bio, timestamps)
- API: `POST /api/v1/auth/register` → access + refresh token; `POST /api/v1/auth/login`; `POST /api/v1/auth/refresh`; `POST /api/v1/auth/logout`
- World auto-tạo: trong transaction register → tạo users + worlds
- Security: bcrypt, JWT access 15m + refresh 7d (rotation, Redis)
- OAuth: defer (roadmap note) — email/password trước

**IMPLEMENT**
- `V1__users.sql` (email/username UNIQUE, index)
- domain: `User` (entity + VO Role), repository interface
- application: `RegisterUseCase` (unique check → bcrypt → save user → save world), `LoginUseCase`, `RefreshUseCase`, `LogoutUseCase`
- infrastructure: JPA repo, JwtService (sign/verify), RedisTokenStore (refresh rotation, blacklist)
- presentation: `AuthController` — set refreshToken trong httpOnly cookie

**VERIFY**
- Unit test: email trùng → 409; username trùng → 409; password không hash (plaintext) → fail test security
- Integration test: register → 201 + world được tạo; login sai password → 401
- curl e2e: register → login → gọi `GET /users/me` với access token → 200

---

### US-21 — Cập nhật profile

Identity · Sprint 1 / M1 · **DONE**

*As a Learner, I want to update my profile information, so that others can know who I am.*

**SPEC**
- Cập nhật avatar, bio, social links
- Có thể set profile public hoặc private
- Role mặc định LEARNER; nâng CREATOR khi có quest published

**PLAN**
- API: `GET /api/v1/users/me` (kèm denorm stats), `PUT /api/v1/users/me`, `GET /api/v1/users/:username`
- Avatar: upload qua Cloudinary → lưu URL; không đổi email/username (AC từ api-design)
- Role promotion: trigger trong PublishQuestUseCase (US-10)
- Denorm stats: quest_count, follower_count, following_count — cập nhật qua event (M5)

**IMPLEMENT**
- application: `GetMyProfileQuery`, `UpdateProfileUseCase`, `GetPublicProfileQuery`
- infrastructure: Cloudinary client (upload avatar), denorm fields trên users
- guard public/private: GET /users/:username private → 404 nếu chưa follow

**VERIFY**
- Integration test: PUT avatar/bio → GET thấy mới; đổi is_public=false → GET public profile trả 404
- Publish quest đầu tiên → role = CREATOR

---

## Sprint 2 · M1 · Quest CRUD — chạy từ Sprint 1 xong

### US-01 — Tạo learning path

Quest · Sprint 2 / M1 · **DONE**

*As a Creator, I want to create a learning path with a title, description and difficulty, so that I can bundle multiple quests toward one goal.*

**SPEC**
- Path phải thuộc một `skill_domains`
- Path mặc định **private** khi tạo
- Creator là owner của path

**PLAN**
- DB: `learning_paths` (id, creator_id FK→users, domain_id FK→skill_domains, title, description, difficulty, is_public, timestamps)
- API: `POST /api/v1/learning-paths` · `GET /api/v1/learning-paths/:id` · `PUT /api/v1/learning-paths/:id`
- Domain: entity `LearningPath`, VO `Difficulty` (BEGINNER/INTERMEDIATE/ADVANCED)
- Kiểm tra domain tồn tại trong application layer trước khi persist

**IMPLEMENT**
- `db/migration/V2__learning_paths.sql` (FK + index creator_id, domain_id)
- domain: `LearningPath`, `Difficulty`; repository interface `LearningPathRepository`
- application: `CreateLearningPathUseCase` (validate domain → tạo → private mặc định), `UpdateLearningPathUseCase`
- infrastructure: `JpaLearningPathRepository`, map exception domain→404
- presentation: `LearningPathController` (POST/PUT), DTO + validation

**VERIFY**
- Unit test: tạo path với domain không tồn tại → throw; mặc định `is_public=false`
- Integration test: POST → 201; GET theo id; PUT không phải owner → 403
- curl: `curl -X POST localhost:9090/api/v1/learning-paths -H "Authorization: Bearer ..." -d @path.json`

---

### US-02 — Tạo quest với chapters & tasks

Quest · Sprint 2 / M1 · **DONE**

*As a Creator, I want to create a quest with chapters and tasks, so that I can structure a roadmap for others to follow.*

**SPEC**
- Quest có thể gắn vào LearningPath (hoặc độc lập)
- Quest phải có ≥1 chapter, mỗi chapter ≥1 task
- Quest mặc định **DRAFT** khi tạo
- Mỗi task có `type`: LEARN/QUIZ/PRACTICE/SUBMISSION/REFLECTION

**PLAN**
- DB: `quests` (id, creator_id, path_id nullable, domain_id, title, description, difficulty, status DRAFT, completion_rule JSONB, timestamps), `chapters` (id, quest_id, title, description, position), `tasks` (id, chapter_id, title, description, type, position, config JSONB — threshold/evidence)
- API: `POST /api/v1/quests` (body chứa chapters+tasks lồng nhau), `GET /api/v1/quests/:id`, `PUT /api/v1/quests/:id`
- Domain: Aggregate `Quest` → chứa `Chapter` → chứa `Task`; enum `TaskType`; VO `QuestStatus`
- Transaction: tạo cả cây quest+chapters+tasks trong 1 @Transactional

**IMPLEMENT**
- `V3__quests_chapters_tasks.sql` (quests, chapters, tasks + FK cascade + indexes)
- domain: `Quest` (aggregate root với `addChapter()`/`addTask()`), `Chapter`, `Task`, `TaskType`, `QuestStatus`
- application: `CreateQuestUseCase` — validate cây ≥1 chapter ≥1 task, gán position, set DRAFT; `QuestQueryService`
- infrastructure: `JpaQuestRepository` (save cả cascade), DTO mapper quest↔cây chapters/tasks
- presentation: `QuestController` POST/GET/PUT + request DTO có nested list + bean validation

**VERIFY**
- Unit test: quest không chapter → reject; chapter không task → reject; mặc định DRAFT
- Integration test: POST với cây 2 chapters × 2 tasks → 201 + toàn bộ tồn tại; position tự tăng
- Kiểm tra cascade delete: xóa quest → chapters/tasks bị xóa

---

### US-03 — Thêm chapter & task, gắn resource

Quest · Sprint 2 / M1 · **DONE**

*As a Creator, I want to add chapters, add tasks inside each chapter, and attach resources to LEARN tasks, so that the learning path is clear and logical.*

**SPEC**
- Chapter: title, description, có thể reorder
- Task: title, description, type, order — có thể reorder
- Task LEARN chứa nhiều Resource (VIDEO, ARTICLE, BOOK, DOCUMENT, COURSE, PODCAST, FILE, LINK)
- Chapter/task có thể xóa nếu quest chưa được fork

**PLAN**
- DB: thêm `resources` (id, task_id, type, title, url, position, timestamps)
- API: `POST /api/v1/quests/:id/chapters`, `POST /api/v1/quests/:id/chapters/:chapterId/tasks`, `POST /api/v1/tasks/:id/resources`, `PUT /api/v1/quests/:id/order` (reorder), `DELETE ...`
- Domain: `Resource`, enum `ResourceType`; method `quest.reorderChapters()` chỉ hoạt động khi status DRAFT
- Guard: `canModify(quest)` → đã có personal_quest thì 409 (US-03 AC)

**IMPLEMENT**
- `V4__resources.sql`
- domain: `Resource`, `ResourceType`, mở rộng `Task.addResource()` (chỉ khi type LEARN — invariant trong domain)
- application: `AddChapterUseCase`, `AddTaskUseCase`, `AddResourceUseCase`, `ReorderQuestUseCase`, `DeleteQuestPartUseCase`
- Guard chống fork: query `personal_quests` theo quest_id → tồn tại thì reject
- presentation: mở rộng controller + DTO reorder (danh sách {id, position})

**VERIFY**
- Unit test: addResource vào task PRACTICE → reject (chỉ LEARN); reorder khi quest đã PUBLIC + đã fork → 409
- Integration test: thêm chapter/task/resource → GET quest thấy thứ tự mới
- curl: POST resource → 201; DELETE chapter → GET lại không còn

---

### US-04 — Cấu hình CompletionRule

Quest · Sprint 2 / M1 · **DONE**

*As a Creator, I want to set a completion rule for my quest, so that a quest is considered done only when the real criteria are met.*

**SPEC**
- Rule mặc định: `ALL_TASKS`
- Hỗ trợ `QUIZ_SCORE` (quiz ≥ ngưỡng %), `SUBMISSION` (phải nộp), `ALL_OF` (AND), `ANY_OF` (OR)
- Rule được **snapshot** vào PersonalQuest khi fork — thay đổi sau không ảnh hưởng

**PLAN**
- DB: cột `quests.completion_rule` JSONB (đã có từ V3)
- JSON schema: `{ "type": "ALL_TASKS" }` | `{ "type": "QUIZ_SCORE", "threshold": 80 }` | `{ "type": "SUBMISSION" }` | `{ "type": "ALL_OF", "rules": [...] }` | `{ "type": "ANY_OF", "rules": [...] }`
- Domain: VO `CompletionRule` (type + params), factory với default; validation schema
- Engine `CompletionEvaluator` đọc PersonalQuest tasks → trả boolean (được dùng thật ở M2, nhưng validation + lưu trữ làm ngay)

**IMPLEMENT**
- domain: `CompletionRule` + `CompletionRuleFactory.defaultAllTasks()` + parser từ JSON (Jackson)
- application: `SetCompletionRuleUseCase` (PUT /api/v1/quests/:id/completion-rule) — validate schema, chỉ creator, chỉ khi DRAFT
- infrastructure: AttributeConverter lưu VO ↔ JSONB
- presentation: endpoint riêng + DTO; trả về rule đã lưu để confirm

**VERIFY**
- Unit test: parser rule hợp lệ/không hợp lệ (unknown type → reject); default khi tạo quest = ALL_TASKS
- Integration test: PUT rule → GET thấy rule; non-creator → 403; quest đã PUBLIC → 409
- Snapshot check chuyển sang M2 khi có fork

---

### US-10 — Publish quest

Quest · Sprint 2 / M1 · **DONE**

*As a Creator, I want to publish my quest so others can discover it, so that my work can help the community.*

**SPEC**
- Quest chuyển từ DRAFT sang PUBLIC
- Quest xuất hiện trong Marketplace
- Creator có thể unpublish bất cứ lúc nào

**PLAN**
- API: `POST /api/v1/quests/:id/publish`, `POST /api/v1/quests/:id/unpublish`
- Guard: phải có ≥1 chapter ≥1 task (US-02 invariant) trước khi publish
- Outbox: `quest.published` → Marketplace index (ES ở M4; trước đó list query filter status=PUBLIC)
- Nâng role: creator được nâng CREATOR (US-21 AC) khi có quest published đầu tiên

**IMPLEMENT**
- application: `PublishQuestUseCase`, `UnpublishQuestUseCase` (validate tree, đổi status, role promotion, write outbox)
- presentation: 2 endpoint; DTO trả quest + status mới

**VERIFY**
- Unit test: publish quest rỗng → 400; non-creator → 403
- Integration test: publish → GET danh sách PUBLIC thấy quest; unpublish → biến mất; role creator được cập nhật

---

## Sprint 3 · M2 · Fork + Task Completion + Quiz — track tiến độ thật

### US-05 — Fork quest

Quest · Sprint 3 / M2 · **DONE**

*As a Learner, I want to fork a public quest into my own copy, so that I can track my personal progress without affecting the original.*

**SPEC**
- Tạo ra PersonalQuest + PersonalChapter + PersonalTask (copy toàn bộ)
- Learner là owner của PersonalQuest
- Quest gốc không bị thay đổi

**PLAN**
- DB: `personal_quests` (id, user_id, quest_id FK, completion_rule snapshot, status ACTIVE, timestamps), `personal_chapters`, `personal_tasks` (id, ref: task_id, type, config snapshot)
- API: `POST /api/v1/quests/:id/fork`, `GET /api/v1/personal-quests`, `GET /api/v1/personal-quests/:id`
- Domain: `PersonalQuest` aggregate; `Quest.forkTo(user)` — copy rule + cây chapter/task
- Idempotent: fork 2 lần cùng quest → 409 (đã fork)

**IMPLEMENT**
- `V5__personal_quests.sql` (3 bảng, FK cascade)
- domain: `PersonalQuest`/`PersonalChapter`/`PersonalTask`, method `forkTo()`
- application: `ForkQuestUseCase` (transaction copy cây, snapshot rule), `GetPersonalQuestsQuery`
- outbox: publish `quest.forked` trong cùng transaction
- presentation: controller + DTO

**VERIFY**
- Unit test: fork 2 lần → 409; snapshot rule không đổi sau khi quest gốc sửa rule
- Integration test: fork → 3 bảng personal có đủ bản sao; quest gốc không đổi
- Verify outbox: bảng `outbox_events` có `quest.forked` sau fork

---

### US-06 — Hoàn thành task theo loại

Quest · Sprint 3 / M2 · **DONE**

*As a Learner, I want to complete tasks of different types, so that my progress reflects real work done.*

**SPEC**
- LEARN/PRACTICE/REFLECTION: tick là xong (REFLECTION bắt buộc text ≥ minLength nếu cấu hình)
- SUBMISSION: phải nộp URL/text vào `evidence` mới tính hoàn thành
- QUIZ: phải đạt `passThreshold` trong task.config mới tính hoàn thành
- Tạo TaskCompletion record; progress tự tính; completion có thể undo

**PLAN**
- DB: `task_completions` (id, personal_task_id, user_id, completed_at, evidence nullable, UNIQUE(personal_task_id))
- API: `PUT /api/v1/personal-quests/:pqId/tasks/:ptId/complete` (body: evidence/reflection optional), `DELETE .../complete` (undo)
- Domain: `PersonalTask.complete(evidence)` — validate theo type (REFLECTION length, SUBMISSION bắt buộc evidence, QUIZ không qua endpoint này)
- Event: `task.completed` / `task.undone` → update progress + trigger rule evaluation (US-08)

**IMPLEMENT**
- `V6__task_completions.sql` (UNIQUE constraint để chống double-complete)
- domain: `TaskCompletion`, logic validate theo type trong domain layer
- application: `CompleteTaskUseCase`, `UndoTaskUseCase` (idempotent khi undo chưa có record)
- sau complete → gọi `CompletionEvaluator` (US-08) trong cùng transaction hoặc qua event
- presentation: endpoint + DTO evidence

**VERIFY**
- Unit test: SUBMISSION không evidence → reject; REFLECTION ngắn hơn minLength → reject; complete 2 lần → 409
- Integration test: complete → task_completions có record; undo → record biến mất; progress % giảm đúng
- curl: PUT complete + DELETE undo

---

### US-07 — Làm quiz và xem kết quả

Quest · Sprint 3 / M2 · **DONE**

*As a Learner, I want to take a quiz and see my score immediately, so that I know if I've passed the task.*

**SPEC**
- Mỗi lần làm quiz ghi 1 QuizAttempt (score, max_score, passed)
- Đạt ngưỡng → task tự hoàn thành; chưa đạt → được làm lại
- Lịch sử attempt hiển thị được

**PLAN**
- DB: `quiz_attempts` (id, personal_task_id, user_id, score, max_score, passed, created_at)
- API: `POST /api/v1/personal-tasks/:ptId/quiz-attempts` (body: answers) → trả score; `GET /api/v1/personal-tasks/:ptId/quiz-attempts`
- Task QUIZ config: `{ "passThreshold": 80, "questions": [...] }` (questions có thể trả về ẩn đáp án)
- Pass → tự gọi CompleteTaskUseCase (US-06)

**IMPLEMENT**
- `V7__quiz_attempts.sql`
- domain: `QuizAttempt`, `QuizService.grade(task.config, answers)` — đếm câu đúng, tính %, passed
- application: `SubmitQuizUseCase` (grade → save attempt → nếu passed gọi complete), `GetQuizHistoryQuery`
- presentation: 2 endpoint + DTO (không trả đáp án trong response)

**VERIFY**
- Unit test: đúng 8/10 → 80% → passed; 7/10 → failed; failed lần 2 vẫn được nộp
- Integration test: POST attempt pass → task tự complete + task_completions có record
- curl: nộp 2 lần → GET history thấy 2 records

---

## Sprint 4 · M2 · Completion engine + PersonalQuest edit

### US-08 — Hoàn thành quest theo rule

Quest · Sprint 4 / M2 · **DONE**

*As a Learner, I want my quest to be marked completed automatically when the rule is satisfied, so that I know I have achieved my goal.*

**SPEC**
- Evaluate `completion_rule` khi task completed/undone
- Rule thỏa → status = COMPLETED, completed_at được lưu
- Activity + Notification được tạo ra; Achievement được kiểm tra mở khóa

**PLAN**
- Domain: `CompletionEvaluator` — strategy per rule type; `PersonalQuest.advance()` đổi status
- Trigger: trong CompleteTask/UndoTask use case → gọi evaluator; undo có thể reset COMPLETED → ACTIVE
- Outbox: publish `quest.completed` + `task.completed`
- Consumers (M3): World cập nhật completion_count, Social tạo activity, Notification gửi (M5)

**IMPLEMENT**
- domain: `CompletionEvaluator` + các rule strategy (ALL_TASKS, QUIZ_SCORE, SUBMISSION, ALL_OF, ANY_OF)
- application: hook vào CompleteTaskUseCase/UndoTaskUseCase; `EvaluateCompletionUseCase`
- thêm cột `completed_at` vào `personal_quests` (V6 alter)
- outbox writer cho 2 event
- achievement check: stub interface cho tới M3

**VERIFY**
- Unit test: ALL_TASKS complete hết → COMPLETED; undo 1 → ACTIVE; ANY_OF chỉ cần 1
- Integration test: hoàn thành quest → completed_at có giá trị + outbox có quest.completed
- Test idempotent: complete lại task đã complete → không ghi event trùng

---

### US-09 — Chỉnh sửa PersonalQuest

Quest · Sprint 4 / M2 · **DONE**

*As a Learner, I want to add or modify chapters/tasks in my forked quest, so that I can customize the path to fit my needs.*

**SPEC**
- Chỉ chỉnh sửa được PersonalQuest của mình
- Không ảnh hưởng Quest gốc
- Chapter/task thêm mới không ảnh hưởng fork khác

**PLAN**
- API: `POST /api/v1/personal-quests/:id/chapters`, `POST /api/v1/personal-quests/:id/chapters/:cid/tasks`, reorder, delete (chỉ ACTIVE)
- Domain: giống US-03 nhưng trên cây Personal*; guard owner + status ACTIVE
- Task mới mặc định chưa completed (không tạo TaskCompletion)

**IMPLEMENT**
- application: `EditPersonalQuestUseCase` (đủ 4 thao tác thêm/xóa/reorder chapter + task)
- reuse guard: owner check + `isActive()`
- không chạm quest gốc — chỉ thao tác trên personal_* tables

**VERIFY**
- Unit test: sửa quest của user khác → 403; quest COMPLETED → 409
- Integration test: thêm task vào PersonalQuest → quest gốc không đổi; fork khác không đổi

---

## Sprint 5 · M3 · World + Marketplace browse

### US-16 — Xem thế giới cá nhân

World · Sprint 5 / M3 · **DONE**

*As a Learner, I want to see my Knowledge World, so that I can visualize my progress across different domains.*

**SPEC**
- Mỗi Domain có một District riêng
- District reflect số TaskCompletion thuộc domain đó
- World cập nhật ngay khi có completion mới

**PLAN**
- DB: `worlds` (user_id 1-1, created_at), `districts` (id, world_id, domain_id, completion_count)
- API: `GET /api/v1/world/me` → world + districts + completion_count
- Consumer `task.completed`/`task.undone`: cập nhật completion_count district theo domain của quest gốc
- World + districts auto-tạo khi đăng ký (US-20 AC)

**IMPLEMENT**
- `V9__worlds_districts.sql`
- domain: `World`, `District` (aggregate World chứa districts)
- application: `GetWorldQuery`; `TaskCompletedEventHandler` cập nhật completion_count (idempotent, +1/-1 đúng sự kiện)
- chỉ tạo District khi domain có completion đầu tiên (lazy) hoặc eager tạo tất cả — chọn lazy để world gọn

**VERIFY**
- Integration test: complete task → district completion_count tăng; undo → giảm
- Idempotent test: xử lý trùng event → không tăng 2 lần

---

### US-17 — Xem district & building

World · Sprint 5 / M3 · **DONE**

*As a Learner, I want to see district details with buildings, so that I can feel my progress visually.*

**SPEC**
- District hiển thị quest đã complete + quest đang active + tổng task hoàn thành
- Building mở khóa theo completion_count — thuần visualize, không ảnh hưởng logic
- Building type tự do (house, school, library...) — không gắn game rule nào

**PLAN**
- DB: `buildings` (id, district_id, type, unlocked_at, position)
- API: `GET /api/v1/world/districts/:id` → district + buildings + quests completed/active + total tasks
- Unlock rule: threshold completion_count → unlock building (predefined: 1/5/10/20...)
- Thuần UI mapping — không dùng cho logic chấm điểm

**IMPLEMENT**
- `V9__buildings.sql`
- application: `GetDistrictDetailQuery` (join personal_quests completed/active theo domain); `BuildingUnlockService` chạy khi completion_count đổi ngưỡng
- presentation: endpoint + DTO

**VERIFY**
- Integration test: completion_count đạt ngưỡng → building mới xuất hiện; quest completed/active liệt kê đúng
- UI test (web M6): building render từ DTO

---

### US-18 — Xem world người khác

World · Sprint 5 / M3 · **LATER**

*As a Visitor, I want to view another user's Knowledge World, so that I can be inspired by their progress.*

**SPEC**
- Chỉ xem được nếu user đó có profile public
- Không thể chỉnh sửa world người khác

**PLAN**
- API: `GET /api/v1/world/users/:username` — world là resource chính (`/world`), lookup by username
- Guard: kiểm tra `users.is_public`; private + chưa follow → 404 (ẩn danh tính như US profile)
- Read-only — không có mutation endpoint

**IMPLEMENT**
- application: `GetUserWorldQuery` (permission check qua profile service)
- reuse DTO thế giới từ US-16

**VERIFY**
- Integration test: profile private → 404 với visitor; public → 200; không có endpoint chỉnh sửa

---

### US-19 — Mở khóa achievement

World · Sprint 5 / M3 · **LATER**

*As a Learner, I want to unlock achievements at real milestones, so that I get intrinsic recognition without fake gamification.*

**SPEC**
- Achievement gắn mốc thật: quest đầu tiên, X quests, X tasks, Y tasks trong 1 domain
- Không có XP/Level/điểm thưởng
- Achievement unlock hiển thị trên feed và profile

**PLAN**
- DB: `achievements` (id, code, title, description, criteria JSONB), `user_achievements` (user_id, achievement_id, unlocked_at, PK composite)
- Engine: `AchievementEvaluator` chạy khi quest completed / task completed — đọc counter (total_quests, total_tasks, per-domain tasks) → check criteria → unlock
- Event: `achievement.unlocked` → feed (M5) + notification (M5)

**IMPLEMENT**
- `V10__achievements.sql` + seed 6-8 achievement codes
- domain: `Achievement`, `AchievementEvaluator` (strategy per criteria)
- application: hook sau quest completed (US-08); `GetMyAchievementsQuery` + `?onlyLocked=true`

**VERIFY**
- Unit test: đạt mốc → unlock đúng; không double-unlock; criteria không khớp → không unlock
- Integration test: complete quest đầu tiên → achievement "First Quest" có trong user_achievements

---

### US-11 — Khám phá learning path & quest

Marketplace · Sprint 5 / M3 · **LATER**

*As a Guest, I want to browse learning paths and popular/trending quests, so that I can find interesting goals to pursue.*

**SPEC**
- Hiển thị Learning Paths theo Domain
- Hiển thị Popular quests (usage count + rating)
- Hiển thị Trending quests (gần đây dùng nhiều)
- Không cần đăng nhập

**PLAN**
- API: `GET /api/v1/marketplace/home` (paths by domain, popular, trending)
- DB: denorm `quests.usage_count`, `quests.avg_rating` (M4); trending = usage trong 7 ngày qua (bảng aggregate tạm hoặc Redis ZSET)
- Redis (Upstash): cache kết quả ~60s, ZSET cho trending
- Public endpoint, không cần auth

**IMPLEMENT**
- application: `MarketplaceHomeQuery` — 3 dataset: paths theo domain, popular (ORDER BY usage_count DESC, avg_rating DESC), trending (Redis ZINCRBY)
- infrastructure: Redis template + cache config; repository query
- presentation: `MarketplaceController` GET /marketplace/home

**VERIFY**
- Integration test: không token vẫn 200; quest DRAFT không xuất hiện; thứ tự popular đúng
- Redis: sau fork (US-05) → usage_count tăng → quest lên trending

---

## Sprint 7 · M4 · Search + Review + Favorite

### US-12 — Tìm kiếm quest

Marketplace · Sprint 7 / M4 · **LATER**

*As a Guest, I want to search quests by keyword, so that I can find quests relevant to my goal.*

**SPEC**
- Tìm theo title, description, chapter title, task title
- Kết quả xếp theo relevance
- Lọc được theo domain, difficulty

**PLAN**
- Elasticsearch (Bonsai) index `quests`: fields title/desc (analyzed), chapter/task title (nested), domain_id, difficulty
- Sync: consumer `quest.published` → index; `quest.unpublished` → delete doc; `quest.updated` → reindex
- API: `GET /api/v1/marketplace/search?q=&domain=&difficulty=&page=`
- Trước khi có ES: fallback PostgreSQL full-text (tsvector) — plan chuyển sang ES khi M4

**IMPLEMENT**
- index mapping + `QuestIndexService` (index/delete/reindex)
- outbox consumer: `QuestPublishedEventHandler` → ES
- application: `SearchQuestsQuery` — build ES query (multi_match + filter) → map results
- presentation: `MarketplaceController.search()`

**VERIFY**
- Integration test: index quest → search keyword tìm thấy; filter domain đúng; unpublished không hiện
- Relevance: tên trùng từ khóa xếp trước mô tả chứa từ khóa

---

### US-13 — Review quest

Marketplace · Sprint 7 / M4 · **LATER**

*As a Learner, I want to rate a quest I have used with stars and optional text, so that I can help others choose quality quests.*

**SPEC**
- Chỉ review được quest đã fork
- Score 1–5 sao + content text (tùy chọn)
- Chỉ review 1 lần, có thể cập nhật
- Average rating hiển thị trên quest card

**PLAN**
- DB: `reviews` (id, quest_id, user_id, score 1-5, content, created_at, updated_at, UNIQUE(quest_id,user_id))
- API: `POST /api/v1/quests/:id/review`, `PUT /api/v1/reviews/:id`, `GET /api/v1/quests/:id/reviews`
- Denorm: cập nhật `quests.avg_rating` + `review_count` sau mỗi review/update (trong cùng transaction)
- Guard: phải có PersonalQuest của quest này

**IMPLEMENT**
- `V8__reviews.sql` (UNIQUE + index quest_id)
- application: `CreateReviewUseCase` (guard đã fork, upsert 1 lần), `UpdateReviewUseCase`, `GetReviewsQuery`
- infrastructure: recompute avg_rating (hoặc incremental update)
- presentation: controller + DTO

**VERIFY**
- Unit test: review quest chưa fork → 403; score ngoài 1–5 → 400; review 2 lần → update (không trùng)
- Integration test: sau review, `avg_rating` cập nhật đúng

---

### US-14 — Lưu quest yêu thích

Marketplace · Sprint 7 / M4 · **LATER**

*As a Learner, I want to save a quest to my favorites, so that I can find it easily later.*

**SPEC**
- Toggle favorite (add/remove)
- Danh sách favorite trong profile
- Không cần fork để favorite

**PLAN**
- DB: `favorites` (user_id, quest_id, created_at, PK(user_id, quest_id))
- API: `PUT /api/v1/quests/:id/favorite` (toggle hoặc body action), `GET /api/v1/users/me/favorites`
- Auth required; đơn giản, không cần event

**IMPLEMENT**
- `V8__favorites.sql`
- application: `ToggleFavoriteUseCase` (idempotent), `GetFavoritesQuery` (paginated)
- presentation: controller

**VERIFY**
- Integration test: toggle 2 lần → hết favorite; list trong profile đúng; chưa login → 401

---

## Sprint 8 · M4 · Quest Analytics

### US-15 — Xem analytics quest (Creator)

Marketplace · Sprint 8 / M4 · **LATER**

*As a Creator, I want to see how many people are using and completing my quest, so that I can understand its impact.*

**SPEC**
- Hiển thị: fork count, completion rate, average rating
- Hiển thị task/chapter nào drop-off nhiều nhất

**PLAN**
- API: `GET /api/v1/quests/:id/analytics` (owner only)
- Data: fork_count từ personal_quests, completion_rate = completed/total, drop-off = count task_completions per personal_task_id
- Native SQL query qua repository (join 3 bảng) — tránh N+1

**IMPLEMENT**
- infrastructure: `QuestAnalyticsRepository` — native query group by task, count completions
- application: `GetQuestAnalyticsQuery` — build DTO fork/completion/rating/dropOff[]
- presentation: endpoint owner-only

**VERIFY**
- Integration test: tạo 2 forks, 1 completed → completion rate 50%; drop-off đúng task
- Permission: creator khác → 403

---

## Sprint 9 · M5 · Social — Go service

### US-22 — Follow user

Social · Sprint 9 / M5 · **LATER**

*As a Learner, I want to follow other users, so that I can see their progress and be motivated.*

**SPEC**
- Toggle follow/unfollow
- Số follower hiển thị trên profile

**PLAN**
- DB: `follows` (follower_id, followee_id, created_at, PK composite)
- API: `PUT /api/v1/users/:username/follow` (toggle), `GET /api/v1/users/:username/followers`, `.../following`
- Go service (Fiber) sở hữu bảng này — không cho phép Java module viết trực tiếp (service-ownership)
- Denorm follower_count trên users (qua event hoặc update trực tiếp qua API internal)

**IMPLEMENT**
- repo Go: `internal/follow/` — domain + repository + handler
- migration V11__follows.sql (DB dùng chung phase 1)
- cập nhật follower_count: publish event `user.followed/unfollowed` → Identity consumer

**VERIFY**
- Go test: toggle follow; self-follow → 400; double follow → idempotent
- Integration: follow → follower_count tăng trên profile

---

### US-23 — Xem feed

Social · Sprint 9 / M5 · **LATER**

*As a Learner, I want to see a feed of activities from people I follow, so that I can stay updated on their progress.*

**SPEC**
- Hiển thị: quest completed, quest forked, task completed, quest published, achievement unlocked
- Sắp xếp theo thời gian mới nhất

**PLAN**
- DB: `activities` (id, actor_id, type, payload JSONB, created_at, index actor+time)
- API: `GET /api/v1/feed` (activities của người mình follow)
- Consumers: `quest.completed`, `quest.forked`, `task.completed`, `quest.published`, `achievement.unlocked` → insert activities
- Feed query: join follows + activities (fan-out-on-read, đủ cho learning scale)

**IMPLEMENT**
- migration V11__activities.sql
- Go: `internal/feed/` — event consumers (idempotent theo eventId) + `GetFeedQuery`
- mapper payload → display DTO

**VERIFY**
- Integration: user B follow A → A fork quest → feed B thấy activity; sắp xếp desc
- Idempotent: event trùng không insert 2 activity

---

### US-24 — Comment trên quest & discussion

Social · Sprint 9 / M5 · **LATER**

*As a Learner, I want to comment on quests and reply inside discussions, so that the community can help each other.*

**SPEC**
- Comment trên Quest (target_type = QUEST)
- Tạo Discussion gắn với Quest (hỏi đáp, chia sẻ)
- Reply lồng nhau tối đa 2 cấp qua parent_id

**PLAN**
- DB: `comments` (id, target_type, target_id, user_id, content, parent_id, created_at), `discussions` (id, quest_id, user_id, title, status OPEN/CLOSED, created_at)
- API: `GET/POST /api/v1/quests/:id/comments`, `GET/POST /api/v1/quests/:id/discussions`, reply qua parent_id (max 2 cấp)

**IMPLEMENT**
- migration V11__comments_discussions.sql
- Go: `internal/comment/`, `internal/discussion/` — handlers + depth guard

**VERIFY**
- Go test: reply cấp 3 → 400; discussion trên quest không tồn tại → 404

---

## Sprint 11 · M6 · Web (Next.js) — Auth + Marketplace + Quest Detail — bắt đầu làm web

### FE-01 — Web skeleton + Auth (Login · Register)

Web · Sprint 11 / M6 · **LATER**

Screens #02 Login, #03 Register — guest đăng nhập / tạo tài khoản, vào Web App được, World tự tạo.

**SPEC**
- Login: email + password, link Register / Forgot Password, OAuth buttons (placeholder nếu chưa làm)
- Register: email + password + username, terms checkbox, sau khi xong → redirect vào app
- Access token 15m + silent refresh 7d qua httpOnly cookie
- Bảo vệ route: chưa login → redirect Login

**PLAN**
- Next.js 14 App Router + Tailwind + TanStack Query (design tokens từ design-system.html)
- API: `POST /auth/register`, `POST /auth/login`, `POST /auth/refresh` (đã có từ US-20)
- Auth flow: `AuthProvider` (React context) — kiểm tra session khi app mount, silent refresh interceptor
- Middleware Next.js bảo vệ route: `middleware.ts` kiểm tra access token

**IMPLEMENT**
- `create-next-app` + setup Tailwind + folder `src/`
- `src/lib/api.ts` — fetch wrapper (base URL, credentials, error normalization)
- `src/providers/auth.tsx` — session state + login/register/logout actions
- `src/app/(auth)/login/page.tsx` + `register/page.tsx` — form + validation + error display
- `src/middleware.ts` — redirect chưa login
- UI primitives: Button, Input, Card từ design tokens

**VERIFY**
- Manual: register → vào app; login → vào app; access token hết hạn → tự refresh không mất phiên
- Không login → truy cập route private bị redirect
- Test component (jest/RTL): form validation hiển thị đúng lỗi

---

### FE-02 — Marketplace browse (Explore · Search · Domain)

Web · Sprint 11 / M6 · **LATER**

Screens #04 Explore, #05 Search Results, #06 Domain Browse — guest duyệt quest không cần login.

**SPEC**
- Explore: trending, popular, domains grid, featured banner
- Search: keyword, filter domain, sort (trending/newest/rating), paginated grid
- Domain Browse: header + learning paths + quest grid theo domain

**PLAN**
- API: `GET /api/v1/marketplace/home` (US-11), `GET /api/v1/marketplace/search` (US-12), `GET /api/v1/domains`
- State: TanStack Query — cache home/search, invalidate khi filter đổi
- Component: `QuestCard`, `DomainGrid`, `SearchBar`, `Pagination` (reuse giữa 3 màn hình)
- SEO: metadata cho Explore (SSR) — lý do chọn Next.js

**IMPLEMENT**
- `src/components/quest/QuestCard.tsx` — hiển thị rating, fork count, domain badge
- `src/app/page.tsx` (Explore) — fetch home, render sections
- `src/app/search/page.tsx` — URL state cho query/filter/sort (SSR-friendly)
- `src/app/domains/[slug]/page.tsx` — dynamic route + list
- `src/lib/queries.ts` — hooks useHomeData, useSearch

**VERIFY**
- Manual: guest xem Explore đủ sections; search "java" → kết quả + filter domain đúng
- Kết quả search khớp: quest PUBLIC/HIDDEN đúng từ API
- Pagination: trang 2 hoạt động, URL phản ánh page

---

### FE-03 — Quest Detail (Public)

Web · Sprint 11 / M6 · **LATER**

Screen #07 — xem quest trước khi dùng, không tick được, nút "Use Quest" yêu cầu login.

**SPEC**
- Cover + meta (domain, difficulty, rating, fork count)
- Chapters preview (3 chapters đầu, collapse phần còn lại)
- Creator card + Ratings
- "Use Quest" → chưa login thì redirect login; đã fork thì hiện "Go to your quest"

**PLAN**
- API: `GET /api/v1/quests/:id` (US-02), `POST /api/v1/quests/:id/fork` (US-05), reviews list (US-13)
- Route: `/quests/[id]` (SSR cho SEO + metadata)
- Use Quest: chưa login → redirect; đã login → fork → redirect PersonalQuest
- Kiểm tra đã fork: API trả flag `forked` khi đã login

**IMPLEMENT**
- `src/app/quests/[id]/page.tsx` — server component fetch + metadata
- `src/components/quest/ChapterPreview.tsx` — expand/collapse (client component)
- `src/components/quest/UseQuestButton.tsx` — auth check + fork mutation (TanStack)
- `src/components/quest/RatingSummary.tsx` + `ReviewList.tsx`
- `src/components/quest/CreatorCard.tsx`

**VERIFY**
- Manual: guest xem detail; click Use Quest → redirect login; sau login → fork → vào tracker
- Đã fork: nút đổi thành "Go to your quest" (không fork lần 2)
- Metadata/OG tag hiển thị đúng title + description

---

## Sprint 12 · M6 · Web — Dashboard + Create Quest + Tracker

### FE-04 — My Dashboard

Web · Sprint 12 / M6 · **LATER**

Screen #08 — tổng quan tiến độ cá nhân sau login.

**SPEC**
- Active PersonalQuests với progress bar
- Recently completed + quick stats tuần này
- Link tới từng PersonalQuest tracker

**PLAN**
- API: `GET /api/v1/personal-quests?status=ACTIVE` (US-05), recent completed
- Route: `/dashboard` (private)
- Component: `QuestProgressCard` (progress %, chapter list mini)

**IMPLEMENT**
- `src/app/dashboard/page.tsx` — layout 2 cột (active | recent)
- `src/components/quest/QuestProgressCard.tsx` — animated progress bar
- query hooks cho active/recent

**VERIFY**
- Manual: login → dashboard thấy quest đang active, progress đúng %
- Chưa có quest → empty state + CTA Explore

---

### FE-05 — Create Quest + Edit Quest

Web · Sprint 12 / M6 · **LATER**

Screens #09, #10 — wizard 3 bước tạo quest; edit pre-filled.

**SPEC**
- Step 1: title, description, skill domain, difficulty
- Step 2: Chapters & Tasks — add/reorder, task type, resource cho LEARN
- Step 3: completion_rule, thumbnail, visibility
- Publish (US-10); Edit chỉ cho quest chưa fork

**PLAN**
- API: `POST /api/v1/quests` (US-02), `POST /api/v1/quests/:id/chapters` (US-03), reorder, publish (US-10)
- Route: `/quests/new`, `/quests/[id]/edit` (private)
- State: local form state (nest chapters→tasks), validations trước khi submit
- Reorder: drag-and-drop (hoặc arrow buttons MVP)

**IMPLEMENT**
- `src/components/create/QuestWizard.tsx` — stepper + 3 steps
- `src/components/create/ChapterTaskEditor.tsx` — add/reorder task, type select
- `src/components/create/CompletionRulePicker.tsx` — 5 loại rule
- `src/app/quests/new/page.tsx` + edit page (reuse wizard với initialData)
- mutation + invalidate query sau khi save/publish

**VERIFY**
- Manual: tạo quest 2 chapters × 2 tasks → publish → xuất hiện Explore
- Validation: chưa đủ chapter/task → nút Publish disabled
- Edit quest đã fork → khóa phần chỉnh sửa (hiển thị restriction)

---

### FE-06 — Quest Tracking (PersonalQuest) + Favorites

Web · Sprint 12 / M6 · **LATER**

Screens #11, #12 — màn hình làm quest chính + danh sách favorite.

**SPEC**
- Progress bar tổng + chapters với task checkbox + completion dates + "Add note"
- Tick task: LEARN/PRACTICE/REFLECTION tick là xong; SUBMISSION cần evidence modal; QUIZ mở quiz
- Undo tick được (US-06); hiển thị "Forked from"
- Favorites: grid + toggle remove (US-14)

**PLAN**
- API: `GET /api/v1/personal-quests/:id`, `PUT/DELETE .../tasks/:ptId/complete` (US-06), quiz attempts (US-07), `PUT /api/v1/quests/:id/favorite` (US-14)
- Route: `/p/[id]` (private), `/favorites`
- Optimistic update khi tick — progress bar cập nhật real-time
- Component: `TaskRow` (theo type), `EvidenceModal`, `QuizRunner`

**IMPLEMENT**
- `src/app/p/[id]/page.tsx` — tree chapters + tasks, progress header
- `src/components/track/TaskRow.tsx` — checkbox per type, disabled states
- `src/components/track/EvidenceModal.tsx` + `ReflectionTextarea`
- `src/components/quiz/QuizRunner.tsx` — nộp đáp án, hiện score/passed
- `src/app/favorites/page.tsx` — grid + toggle
- optimistic mutation: rollback nếu API lỗi

**VERIFY**
- Manual: tick LEARN → progress tăng ngay; undo → giảm; SUBMISSION không evidence → modal yêu cầu
- QUIZ: submit → score → pass thì task tự complete
- Refresh page → trạng thái tick giữ nguyên

---

## Sprint 13 · M7 · Web — World + Profile + Feed

### FE-07 — My World + District Detail

Web · Sprint 13 / M7 · **LATER**

Screens #14, #15 — Knowledge World cá nhân.

**SPEC**
- Grid districts theo domain, mỗi district hiển thị completion_count
- Click district → detail: stats, active quests, completed quests, buildings
- Building mở khóa theo completion_count (thuần visualize — D3.js, có thể MVP = bar chart)

**PLAN**
- API: `GET /api/v1/world/me` (US-16), `GET /api/v1/world/districts/:id` (US-17)
- Route: `/world`, `/world/districts/[id]` (private)
- Render: MVP simple bar grid → upgrade D3 sau (decision log: Deferred)

**IMPLEMENT**
- `src/app/world/page.tsx` — district grid + overall stats
- `src/app/world/districts/[id]/page.tsx` — detail với quest lists
- `src/components/world/DistrictCard.tsx`, `BuildingList.tsx`

**VERIFY**
- Manual: hoàn thành task → world cập nhật count ngay khi refetch
- District detail liệt kê đúng active/completed quests

---

### FE-08 — My Profile + Public Profile

Web · Sprint 13 / M7 · **LATER**

Screens #16, #17 — profile của mình (edit) và của người khác (read-only + follow).

**SPEC**
- My Profile: avatar, bio, social links, quests đã tạo, world preview nhỏ, followers/following
- Public Profile: read-only + Follow/Unfollow (US-22); private profile → 404 với visitor

**PLAN**
- API: `GET/PUT /api/v1/users/me` (US-21), `GET /api/v1/users/:username`, follow endpoints (US-22)
- Route: `/profile` (private), `/u/[username]`
- Avatar upload: Cloudinary widget → URL → PUT profile

**IMPLEMENT**
- `src/app/profile/page.tsx` — edit form + avatar upload + stats
- `src/app/u/[username]/page.tsx` — read-only + FollowButton (optimistic)
- `src/components/profile/ProfileCard.tsx`, `StatsBar.tsx`

**VERIFY**
- Manual: sửa avatar/bio → lưu → hiển thị mới; follow/unfollow đổi trạng thái nút
- Profile private của người khác → trang 404

---

### FE-09 — Activity Feed + Settings

Web · Sprint 13 / M7 · **LATER**

Screens #18, #20 — feed hoạt động + cài đặt tài khoản.

**SPEC**
- Feed: activity từ người follow theo thời gian (completed/forked/published/achievement)
- Settings: profile info, đổi password, notification prefs, privacy (public/private), OAuth placeholder

**PLAN**
- API: `GET /api/v1/feed` (US-23), `PUT /api/v1/users/me` (privacy) (US-21)
- Route: `/feed`, `/settings` (private)

**IMPLEMENT**
- `src/app/feed/page.tsx` — timeline + infinite scroll
- `src/components/feed/ActivityItem.tsx` — render theo type
- `src/app/settings/page.tsx` — form sections

**VERIFY**
- Manual: follow người khác → feed thấy activity của họ
- Settings đổi privacy → public profile 404 đúng

---

## Sprint 14 · M7 · Web — Analytics + AI + Deploy staging

### FE-10 — Creator Analytics

Web · Sprint 14 / M7 · **LATER**

Screen #13 — creator xem hiệu quả quest của mình.

**SPEC**
- Quest selector → fork count, completion rate, drop-off per task, average rating, reviews
- Chart drop-off: task nào người bỏ nhiều nhất

**PLAN**
- API: `GET /api/v1/quests/:id/analytics` (US-15)
- Route: `/creator/analytics` (private, role CREATOR)
- Chart: simple bars (recharts hoặc tự vẽ div) — MVP không cần thư viện nặng

**IMPLEMENT**
- `src/app/creator/analytics/page.tsx` — selector + metric cards
- `src/components/analytics/DropOffChart.tsx`, `StatCard.tsx`

**VERIFY**
- Manual: chọn quest → số liệu đúng; task drop-off cao nhất hiển thị trước

---

### FE-11 — AI Advisor

Web · Sprint 14 / M7 · **LATER**

Screen #19 — mô tả mục tiêu → AI gợi ý quest / sinh quest mới.

**SPEC**
- Text input mục tiêu → danh sách quest gợi ý (US-25)
- Không có quest phù hợp → "Generate a new quest" (US-26) → preview trước khi dùng

**PLAN**
- API: `POST /api/v1/ai/recommend`, `POST /api/v1/ai/generate-quest` (M8 — khi tới Sprint 15 mới có backend)
- Route: `/ai` (private) — nếu flag ai.enabled tắt thì ẩn entry

**IMPLEMENT**
- `src/app/ai/page.tsx` — input + loading state + kết quả
- `src/components/ai/RecommendationList.tsx`, `GeneratePreview.tsx`

**VERIFY**
- Manual: gõ mục tiêu → loading → gợi ý quest; generate → preview quest DRAFT

---

### FE-12 — Deploy staging

Web · Sprint 14 / M7 · **LATER**

Đưa web lên Vercel, kết nối backend, env config.

**SPEC**
- Vercel preview per PR + staging domain
- Env: NEXT_PUBLIC_API_URL, cookie domain, CORS cho backend
- Backend: dùng được từ Vercel (có host staging hoặc local tunnel khi test)

**PLAN**
- Kết nối GitHub repo → Vercel import, configure env
- Backend staging: render/VM nhỏ (hoặc K8s từ M10) — tối thiểu chạy Docker trên VPS
- Cookie: httpOnly cookie cần CORS config + sameSite

**IMPLEMENT**
- `.env.production.example` trong repo
- Vercel project + domains
- CI workflow: build web + test → Vercel preview
- Kiểm tra silent refresh hoạt động cross-origin

**VERIFY**
- Manual: truy cập staging URL, login, tạo quest, fork, tick task — toàn bộ flow hoạt động
- Cookie/session hoạt động đúng trên https staging

---

## Sprint 15 · M8 · AI — Python service

### US-25 — Gợi ý quest theo mục tiêu

AI · Sprint 15 / M8 · **LATER**

*As a Learner, I want to describe my goal and get quest recommendations, so that I can quickly find a relevant learning path.*

**SPEC**
- Input là text mô tả mục tiêu
- Output là danh sách quest gợi ý có sẵn
- Nếu không có quest phù hợp → gợi ý sinh quest mới

**PLAN**
- API: `POST /api/v1/ai/recommend` → { suggestions: [...questIds], fallback: bool }
- Python service: FastAPI + Anthropic SDK
- Flow: prompt Claude "map goal text → keywords" → tìm quest qua ES (matching title/domain) → nếu rỗng, flag fallback=true

**IMPLEMENT**
- `app/routers/recommend.py` — endpoint + call Claude
- `app/services/recommender.py` — parse intent, query ES
- timeout + fallback: nếu LLM lỗi → trả trending quests

**VERIFY**
- pytest: mock Claude → intent đúng → quest match; no match → fallback=true
- Manual: POST mô tả "học Java backend" → gợi ý quest Java

---

### US-26 — Sinh quest mới bằng AI

AI · Sprint 15 / M8 · **LATER**

*As a Learner, I want AI to generate a quest for a goal that has no existing quest, so that I can start immediately without waiting for a creator.*

**SPEC**
- AI tạo quest với chapters + tasks có type phù hợp
- Quest tạo dưới tên user (không phải AI), DRAFT
- User có thể chỉnh sửa trước khi dùng

**PLAN**
- API: `POST /api/v1/ai/generate-quest` → trả quest DRAFT đã tạo (gọi internal Quest API)
- Prompt: yêu cầu JSON đúng schema quest (chapters[], tasks[{type}]) → validate → gọi tạo quest
- Json validation layer để chống LLM trả schema sai

**IMPLEMENT**
- `app/routers/generate.py`, `app/services/quest_generator.py` — prompt + JSON schema validate (pydantic)
- gọi `POST /api/v1/quests` với creator_id = user (service-to-service auth)

**VERIFY**
- pytest: JSON invalid → retry 1 lần → vẫn invalid → 422; quest tạo đúng DRAFT dưới tên user
- Manual: generate → mở UI chỉnh sửa → publish

---

### US-30 — Chấm bài tự động bằng AI (AI Grader)

AI · Sprint 15 / M8 · **LATER**

*As a Learner, I want AI to grade my SUBMISSION/PRACTICE submission against the task rubric, so that I know whether my work really meets the requirements.*

**SPEC**
- Chỉ áp dụng cho task SUBMISSION/PRACTICE có rubric trong task.config
- AI chấm theo rubric → PASS / FAIL / NEEDS_REVISION + score (0–100) + feedback cụ thể
- PASS → tạo TaskCompletion tự động + evaluate completion_rule
- FAIL / NEEDS_REVISION → user nộp lại, feedback hiển thị kèm mỗi lần nộp
- Rate-limited: 20 req/day per user

**PLAN**
- DB: `submission_grades` (personal_task_id FK, user_id FK, quest_id denorm, attempt_no, status, score, feedback, rubric_snapshot JSONB, model)
- API: `POST /api/v1/ai/grade` → { gradeId, status, score, feedback, gradedAt }
- Flow: gọi internal Quest API lấy rubric từ task.config → prompt Claude chấm theo rubric → validate JSON → ghi grade → nếu PASS publish `submission.graded`
- Quest Module consumer: status=PASS → tạo TaskCompletion + evaluate completion_rule

**IMPLEMENT**
- `app/routers/grade.py`, `app/services/grader.py` — prompt + rubric parse + pydantic validate (score 0–100, feedback required)
- Async: grade task nặng → queue nếu cần; timeout + retry (LLM lỗi → 503, không tự PASS)
- Call internal `POST /api/v1/quests/:id/tasks/:taskId/complete` khi PASS (service-to-service auth)

**VERIFY**
- pytest: rubric → PASS/FAIL/NEEDS_REVISION đúng; JSON invalid → 422; PASS → TaskCompletion được tạo + event publish
- Manual: nộp bài kém → FAIL + feedback; nộp bài tốt → PASS tự hoàn thành task

---

### US-31 — AI Coach cá nhân

AI · Sprint 15 / M8 · **LATER**

*As a Learner, I want to chat with an AI coach that knows my real progress, so that I can stay on track and know what to do next.*

**SPEC**
- Chat hỏi đáp về trạng thái PersonalQuest thật của user (task đang dở, streak, achievement)
- AI dùng tool calling để đọc: get_progress, get_streak, get_achievements, get_upcoming_tasks
- Gợi ý bước tiếp theo dựa trên dữ liệu thật
- Streaming response; lịch sử chat lưu theo session
- AI chỉ đọc progress — không tự ý sửa/chấm quest của user

**PLAN**
- DB: `coach_sessions` + `coach_messages` (role USER/ASSISTANT/TOOL, tool_calls JSONB)
- API: `POST /api/v1/ai/coach/sessions`, `GET /api/v1/ai/coach/sessions`, `POST /api/v1/ai/coach/sessions/:id/messages` (SSE streaming), `GET /api/v1/ai/coach/sessions/:id`
- Tool calling: define tools (get_progress, get_streak, get_achievements, get_upcoming_tasks) — read-only, query qua internal Quest/World API
- Rate-limited: 60 messages/day per user

**IMPLEMENT**
- `app/routers/coach.py`, `app/services/coach.py` — conversation loop + tool dispatch
- `app/services/coach_tools.py` — các tool read-only gọi internal API (personal_quests, task_completions, quiz_attempts, achievements)
- Streaming: SSE qua FastAPI StreamingResponse; message ASSISTANT lưu sau khi stream xong
- Tool calls chỉ READ — không có tool nào write quest

**VERIFY**
- pytest: mock Claude → tool được gọi đúng với progress thật; message lưu đúng session; không tool write nào được expose
- Manual: chat "tôi đang kẹt ở đâu?" → trả về task đang dở + gợi ý bước tiếp

---

## Sprint 16 · M8 · Admin

### US-27 — Ẩn quest vi phạm

Admin · Sprint 16 / M8 · **LATER**

*As an Admin, I want to hide a quest that violates community guidelines, so that the marketplace remains safe and high quality.*

**SPEC**
- Quest bị hidden không xuất hiện trong Marketplace
- Creator được thông báo lý do

**PLAN**
- API: `POST /api/v1/admin/quests/:id/hide` (body: reason), `POST .../unhide`
- Status thêm: HIDDEN — bị loại khỏi mọi list marketplace (filter status)
- Notification cho creator: gửi qua Notification service (M5)

**IMPLEMENT**
- application: `HideQuestUseCase` (role ADMIN via @PreAuthorize), `UnhideQuestUseCase`
- outbox: `quest.hidden` → ES remove (M4) + notification

**VERIFY**
- Integration test: non-admin → 403; hide → marketplace/search không thấy; unhide → hiện lại

---

### US-28 — Quản lý domain

Admin · Sprint 16 / M8 · **LATER**

*As an Admin, I want to create and manage skill domains, so that quests and paths are organized properly.*

**SPEC**
- Tạo / sửa / ẩn skill_domains
- Domain ảnh hưởng cả LearningPath lẫn District trong World

**PLAN**
- API: `GET/POST /api/v1/admin/domains`, `PUT/DELETE /api/v1/admin/domains/:id`
- DB: `skill_domains` (id, name, slug UNIQUE, is_active) — seed dữ liệu ban đầu (Programming, Language, Fitness...)
- Domain inactive → quest mới không chọn được

**IMPLEMENT**
- `V1__skill_domains.sql` + seed
- application: `AdminDomainUseCase` + `DomainQueryService`

**VERIFY**
- Integration test: tạo domain → path tạo với domain đó; ẩn → path mới không dùng được

---

### US-29 — Bật/tắt feature flag

Admin · Sprint 16 / M8 · **LATER**

*As an Admin, I want to enable or disable specific features, so that I can control the rollout of new functionality.*

**SPEC**
- Feature flag được quản lý qua admin panel
- Thay đổi có hiệu lực ngay, không cần deploy lại

**PLAN**
- DB: `feature_flags` (id, key UNIQUE, enabled, description, updated_at)
- API: `GET/PUT /api/v1/admin/feature-flags`; `GET /api/v1/feature-flags` (public aggregate cho client cache)
- Cache Redis: flag được cache ~30s → thay đổi hiệu lực nhanh; invalidate khi update

**IMPLEMENT**
- `V10__feature_flags.sql` + seed (ai.enabled, social.enabled...)
- application: `FeatureFlagService.isEnabled(key)` — check cache → DB; `AdminFlagUseCase`
- guard các endpoint mới theo flag (vd: AI endpoints nếu flag tắt → 404)

**VERIFY**
- Integration test: tắt flag ai.enabled → POST /api/v1/ai/* trả 404; bật lại → 200 (không restart)
- Cache test: sau PUT, flag mới phản ánh trong ≤ 30s

---

## Sprint 21 · M11 · React Native — Home + Task Check + My Quests

### FE-13 — RN skeleton + Today / Home + Task Check

Mobile · Sprint 21 / M11 · **LATER**

Screens #21 Today/Home, #24 Task Check — mobile-first daily tracking (tracking-first UX).

**SPEC**
- Home: active quests, tasks cần làm hôm nay, streak counter, quick-complete
- Task Check: fullscreen — tên task to, checkbox lớn, optional note, nút Confirm (satisfying UX)
- Auth: RN có login riêng (token lưu secure-store)

**PLAN**
- Expo SDK 51 + expo-router + RN Skia (cho world sau; MVP dùng RN core)
- API: personal-quests list (US-05), complete task (US-06)
- Auth: login screen trong app, token storage (expo-secure-store)
- Streak: tính từ task_completions theo ngày (backend thêm endpoint nếu cần)

**IMPLEMENT**
- `expo init` + expo-router setup + nativewind
- `app/(tabs)/index.tsx` — Today screen, quick-complete list
- `app/task/[id].tsx` — Task Check fullscreen
- `app/(auth)/login.tsx` — login/register, secure token storage
- `lib/api.ts` — fetch wrapper + token header

**VERIFY**
- Manual (Expo Go): login → Today hiển thị quests; tick task → complete + progress tăng
- Offline: app không crash khi mất mạng (graceful error)

---

### FE-14 — My Quests + Quest Detail + Explore (mobile)

Mobile · Sprint 21 / M11 · **LATER**

Screens #25 My Quests, #23 Quest Detail, #22 Explore — tab điều hướng chính trên mobile.

**SPEC**
- My Quests: active + completed tabs, progress per quest
- Quest Detail: chapters & tasks list, progress bar, "Use Quest"/"Continue"
- Explore: search bar + domains horizontal scroll + trending list

**PLAN**
- API: personal-quests (US-05), quest detail (US-02), marketplace home/search (US-11/12)
- Tabs: Today · My Quests · Explore · Profile

**IMPLEMENT**
- `app/(tabs)/quests.tsx` — tabs active/completed
- `app/quest/[id].tsx` — detail + Use Quest (fork) + Continue
- `app/(tabs)/explore.tsx` — search + domains + trending

**VERIFY**
- Manual: fork quest từ explore → xuất hiện My Quests; detail hiển thị chapters

---

## Sprint 22 · M11 · RN — World + Profile + Notifications + Search

### FE-15 — My World (mobile) + Profile (mobile)

Mobile · Sprint 22 / M11 · **LATER**

Screens #26, #27 — world + profile rút gọn cho mobile.

**SPEC**
- World: district list dọc + progress bar + completion count per domain
- Profile: avatar, stats tóm tắt, public quests, world snapshot

**PLAN**
- API: `GET /api/v1/world/me` (US-16), `GET /api/v1/users/me` (US-21)
- RN Skia nếu muốn world sinh động; MVP list đơn giản

**IMPLEMENT**
- `app/(tabs)/world.tsx` — district list + progress
- `app/(tabs)/profile.tsx` — avatar/stats/quests

**VERIFY**
- Manual: world cập nhật sau task complete; profile đúng dữ liệu

---

### FE-16 — Notifications + Search + Settings (mobile)

Mobile · Sprint 22 / M11 · **LATER**

Screens #28, #29, #30 — thông báo, search fullscreen, settings.

**SPEC**
- Notifications: completions của người follow, new followers, quest updates; unread indicator
- Search: fullscreen + recent searches + suggestions
- Settings: notification prefs, privacy, account (đơn giản hơn web)

**PLAN**
- API: notifications (backend M5 — Notification service), search (US-12)
- Push: Expo Push Notifications (decision log)

**IMPLEMENT**
- `app/(tabs)/notifications.tsx` — list + unread badges
- `app/search.tsx` — fullscreen search + recent (AsyncStorage)
- `app/settings.tsx` — prefs toggles

**VERIFY**
- Manual: nhận notification khi được follow; search có suggestions

---

## Sprint 23 · M12 · Admin page

### FE-17 — Admin Dashboard + Quest Moderation

Admin · Sprint 23 / M12 · **LATER**

Screens #31, #32, #33 — admin overview + kiểm duyệt quest.

**SPEC**
- Dashboard: tổng users, quests, completions hôm nay/tuần, charts đơn giản
- Moderation queue: quests pending/reviewed/hidden + quick actions
- Quest Detail admin view: xem cả private content, actions Hide/Approve/Flag + internal note

**PLAN**
- API: `/api/v1/admin/quests/:id/hide` (US-27), danh sách moderation, dashboard stats
- Route: `/admin/...` — role ADMIN gate (middleware + API guard)

**IMPLEMENT**
- `src/app/admin/page.tsx` — stats + charts
- `src/app/admin/moderation/page.tsx` — queue + filters + actions
- `src/app/admin/moderation/[id]/page.tsx` — full quest view + action bar
- middleware check role ADMIN

**VERIFY**
- Manual: non-admin truy cập /admin → redirect; hide quest → biến mất khỏi marketplace

---

### FE-18 — User Management + Skill Domain + Feature Flags

Admin · Sprint 23 / M12 · **LATER**

Screens #34, #35, #36 — quản lý user, domain, feature flags.

**SPEC**
- User Management: search, assign role, ban/restore
- Skill Domain: CRUD, reorder, hide/show (US-28)
- Feature Flags: list + toggle on/off, hiệu lực ngay (US-29)

**PLAN**
- API: `/api/v1/admin/domains` (US-28), `/api/v1/admin/feature-flags` (US-29), user management

**IMPLEMENT**
- `src/app/admin/users/page.tsx` — table + role select + ban
- `src/app/admin/domains/page.tsx` — CRUD + reorder
- `src/app/admin/flags/page.tsx` — toggle switch + scope display

**VERIFY**
- Manual: toggle feature flag → app phản ánh ngay không cần reload

---

*Sprint 24 · M12 · Landing page + polish + deploy prod (FE-19 → FE-20) — not yet documented*
