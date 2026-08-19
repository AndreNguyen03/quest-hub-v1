# QuestHub — Modules & User Stories (v2)

## Tổng quan module

| Module | Mô tả | Core? |
|--------|-------|-------|
| **MOD-01 Quest** | Tạo learning path, quest, chapter, task, fork, track, hoàn thành | ✅ Core |
| **MOD-02 Marketplace** | Khám phá, tìm kiếm, đánh giá quest & learning path | ✅ Core |
| **MOD-03 World** | Thế giới cá nhân + district + building + achievement | ✅ Core |
| **MOD-04 Identity** | Đăng ký, đăng nhập, profile, role | Supporting |
| **MOD-05 Social** | Follow, feed, comment, discussion | Supporting |
| **MOD-06 AI** | Gợi ý, sinh quest, chấm bài (grader), coach cá nhân | Supporting |
| **MOD-07 Admin** | Kiểm duyệt, quản lý domain | Supporting |

---

## MOD-01 — Quest

### Personas
- **Creator** — người tạo learning path / quest để chia sẻ
- **Learner** — người dùng quest để đạt mục tiêu

---

**US-01** — Tạo learning path
> As a **Creator**,
> I want to create a learning path with a title, description and difficulty,
> so that I can bundle multiple quests toward one goal.

Acceptance Criteria:
- Path phải thuộc một Domain (`skill_domains`)
- Path mặc định private khi tạo
- Creator là owner của path

---

**US-02** — Tạo quest
> As a **Creator**,
> I want to create a quest with chapters and tasks,
> so that I can structure a roadmap for others to follow.

Acceptance Criteria:
- Quest có thể gắn vào LearningPath (hoặc độc lập)
- Quest phải có ít nhất 1 chapter, mỗi chapter ít nhất 1 task
- Quest mặc định là DRAFT khi tạo
- Mỗi task có `type` (LEARN/QUIZ/PRACTICE/SUBMISSION/REFLECTION)

---

**US-03** — Thêm chapter & task, gắn resource
> As a **Creator**,
> I want to add chapters, add tasks inside each chapter, and attach resources to LEARN tasks,
> so that the learning path is clear and logical.

Acceptance Criteria:
- Chapter có title, description, có thể sắp xếp lại thứ tự
- Task có title, description, type, order — có thể reorder
- Task LEARN có thể chứa nhiều Resource (VIDEO, ARTICLE, BOOK, DOCUMENT, COURSE, PODCAST, FILE, LINK)
- Chapter/task có thể xóa nếu quest chưa được fork

---

**US-04** — Cấu hình CompletionRule
> As a **Creator**,
> I want to set a completion rule for my quest,
> so that a quest is considered done only when the real criteria are met.

Acceptance Criteria:
- Rule mặc định: `ALL_TASKS`
- Hỗ trợ `QUIZ_SCORE` (quiz ≥ ngưỡng %), `SUBMISSION` (phải nộp), `ALL_OF` (AND), `ANY_OF` (OR)
- Rule được snapshot vào PersonalQuest khi fork — thay đổi sau không ảnh hưởng

---

**US-05** — Fork quest
> As a **Learner**,
> I want to fork a public quest into my own copy,
> so that I can track my personal progress without affecting the original.

Acceptance Criteria:
- Tạo ra PersonalQuest + PersonalChapter + PersonalTask (copy toàn bộ)
- Learner là owner của PersonalQuest
- Quest gốc không bị thay đổi

---

**US-06** — Hoàn thành task theo loại
> As a **Learner**,
> I want to complete tasks of different types,
> so that my progress reflects real work done.

Acceptance Criteria:
- LEARN/PRACTICE/REFLECTION: tick là xong (REFLECTION bắt buộc text ≥ minLength nếu cấu hình)
- SUBMISSION: phải nộp URL/text vào `evidence` mới tính hoàn thành
- QUIZ: phải đạt `passThreshold` trong task.config mới tính hoàn thành
- Tạo TaskCompletion record; progress tự tính
- Completion có thể bị undo

---

**US-07** — Làm quiz và xem kết quả
> As a **Learner**,
> I want to take a quiz and see my score immediately,
> so that I know if I've passed the task.

Acceptance Criteria:
- Mỗi lần làm quiz ghi 1 QuizAttempt (score, max_score, passed)
- Đạt ngưỡng → task tự hoàn thành; chưa đạt → được làm lại
- Lịch sử attempt hiển thị được

---

**US-08** — Hoàn thành quest theo rule
> As a **Learner**,
> I want my quest to be marked completed automatically when the rule is satisfied,
> so that I know I have achieved my goal.

Acceptance Criteria:
- Evaluate `completion_rule` khi task completed/undone
- Rule thỏa → status = COMPLETED, completed_at được lưu
- Activity + Notification được tạo ra
- Achievement được kiểm tra mở khóa

---

**US-09** — Chỉnh sửa PersonalQuest
> As a **Learner**,
> I want to add or modify chapters/tasks in my forked quest,
> so that I can customize the path to fit my needs.

Acceptance Criteria:
- Chỉ chỉnh sửa được PersonalQuest của mình
- Không ảnh hưởng Quest gốc
- Chapter/task thêm mới không ảnh hưởng fork khác

---

**US-10** — Publish quest
> As a **Creator**,
> I want to publish my quest so others can discover it,
> so that my work can help the community.

Acceptance Criteria:
- Quest chuyển từ DRAFT sang PUBLIC
- Quest xuất hiện trong Marketplace
- Creator có thể unpublish bất cứ lúc nào

---

## MOD-02 — Marketplace

### Personas
- **Guest** — chưa đăng nhập
- **Learner** — đã đăng nhập

---

**US-11** — Khám phá learning path & quest
> As a **Guest**,
> I want to browse learning paths and popular/trending quests,
> so that I can find interesting goals to pursue.

Acceptance Criteria:
- Hiển thị Learning Paths theo Domain (Programming, Language, Fitness...)
- Hiển thị Popular quests (theo usage count + rating)
- Hiển thị Trending quests (gần đây được dùng nhiều)
- Không cần đăng nhập để xem

---

**US-12** — Tìm kiếm quest
> As a **Guest**,
> I want to search quests by keyword,
> so that I can find quests relevant to my goal.

Acceptance Criteria:
- Tìm theo title, description, chapter title, task title
- Kết quả xếp theo relevance
- Lọc được theo domain, difficulty

---

**US-13** — Review quest
> As a **Learner**,
> I want to rate a quest I have used with stars and optional text,
> so that I can help others choose quality quests.

Acceptance Criteria:
- Chỉ review được quest đã fork
- Score từ 1–5 sao + content text (tùy chọn)
- Chỉ được review 1 lần, có thể cập nhật
- Average rating hiển thị trên quest card

---

**US-14** — Lưu quest yêu thích
> As a **Learner**,
> I want to save a quest to my favorites,
> so that I can find it easily later.

Acceptance Criteria:
- Toggle favorite (add/remove)
- Danh sách favorite trong profile
- Không cần fork để favorite

---

**US-15** — Xem analytics quest (Creator)
> As a **Creator**,
> I want to see how many people are using and completing my quest,
> so that I can understand its impact.

Acceptance Criteria:
- Hiển thị: fork count, completion rate, average rating
- Hiển thị task/chapter nào có drop-off nhiều nhất

---

## MOD-03 — World

### Personas
- **Learner** — người xem world của mình
- **Visitor** — người xem world của người khác

---

**US-16** — Xem thế giới cá nhân
> As a **Learner**,
> I want to see my Knowledge World,
> so that I can visualize my progress across different domains.

Acceptance Criteria:
- Mỗi Domain có một District riêng
- District reflect số TaskCompletion thuộc domain đó
- World cập nhật ngay khi có completion mới

---

**US-17** — Xem district & building
> As a **Learner**,
> I want to see district details with buildings,
> so that I can feel my progress visually.

Acceptance Criteria:
- District hiển thị quest đã complete + quest đang active + tổng task hoàn thành
- Building mở khóa theo completion_count của district — **thuần visualize, không ảnh hưởng logic**
- Building type tự do (house, school, library...) — không gắn game rule nào

---

**US-18** — Xem world người khác
> As a **Visitor**,
> I want to view another user's Knowledge World,
> so that I can be inspired by their progress.

Acceptance Criteria:
- Chỉ xem được nếu user đó có profile public
- Không thể chỉnh sửa world người khác

---

**US-19** — Mở khóa achievement
> As a **Learner**,
> I want to unlock achievements at real milestones,
> so that I get intrinsic recognition without fake gamification.

Acceptance Criteria:
- Achievement gắn mốc thật: quest đầu tiên, X quests, X tasks, Y tasks trong 1 domain
- Không có XP/Level/điểm thưởng
- Achievement unlock hiển thị trên feed và profile

---

## MOD-04 — Identity

---

**US-20** — Đăng ký
> As a **Guest**,
> I want to create an account,
> so that I can start tracking my progress.

Acceptance Criteria:
- Đăng ký bằng email hoặc OAuth
- World được tạo tự động sau khi đăng ký
- Profile mặc định là public

---

**US-21** — Cập nhật profile
> As a **Learner**,
> I want to update my profile information,
> so that others can know who I am.

Acceptance Criteria:
- Cập nhật avatar, bio, social links
- Có thể set profile public hoặc private
- Role mặc định LEARNER; nâng CREATOR khi có quest published

---

## MOD-05 — Social

---

**US-22** — Follow user
> As a **Learner**,
> I want to follow other users,
> so that I can see their progress and be motivated.

Acceptance Criteria:
- Toggle follow/unfollow
- Số follower hiển thị trên profile

---

**US-23** — Xem feed
> As a **Learner**,
> I want to see a feed of activities from people I follow,
> so that I can stay updated on their progress.

Acceptance Criteria:
- Hiển thị: quest completed, quest forked, task completed, quest published, achievement unlocked
- Sắp xếp theo thời gian mới nhất

---

**US-24** — Comment trên quest & discussion
> As a **Learner**,
> I want to comment on quests and reply inside discussions,
> so that the community can help each other.

Acceptance Criteria:
- Comment trên Quest (target_type = QUEST)
- Tạo Discussion gắn với Quest (hỏi đáp, chia sẻ)
- Reply lồng nhau tối đa 2 cấp qua parent_id

---

## MOD-06 — AI

---

**US-25** — Gợi ý quest theo mục tiêu
> As a **Learner**,
> I want to describe my goal and get quest recommendations,
> so that I can quickly find a relevant learning path.

Acceptance Criteria:
- Input là text mô tả mục tiêu
- Output là danh sách quest gợi ý có sẵn
- Nếu không có quest phù hợp, gợi ý AI sinh quest mới

---

**US-26** — Sinh quest mới bằng AI
> As a **Learner**,
> I want AI to generate a quest for a goal that has no existing quest,
> so that I can start immediately without waiting for a creator.

Acceptance Criteria:
- AI tạo quest với chapters + tasks có type phù hợp
- Quest được tạo dưới tên user (không phải AI), DRAFT
- User có thể chỉnh sửa trước khi dùng

---

**US-30** — Chấm bài tự động bằng AI (AI Grader)
> As a **Learner**,
> I want AI to grade my SUBMISSION/PRACTICE submission against the task rubric,
> so that I know whether my work really meets the requirements.

Acceptance Criteria:
- Chỉ áp dụng cho task SUBMISSION/PRACTICE có rubric trong task.config
- AI chấm theo rubric → `PASS` / `FAIL` / `NEEDS_REVISION` + score (0–100) + feedback cụ thể
- `PASS` → tạo TaskCompletion tự động + evaluate completion_rule
- `FAIL` / `NEEDS_REVISION` → user nộp lại, feedback hiển thị kèm mỗi lần nộp
- Rate-limited: 20 req/day per user

---

**US-31** — AI Coach cá nhân
> As a **Learner**,
> I want to chat with an AI coach that knows my real progress,
> so that I can stay on track and know what to do next.

Acceptance Criteria:
- Chat hỏi đáp về trạng thái PersonalQuest thật của user (task đang dở, streak, achievement)
- AI dùng tool calling để đọc: `get_progress`, `get_streak`, `get_achievements`, `get_upcoming_tasks`
- Gợi ý bước tiếp theo dựa trên dữ liệu thật
- Streaming response; lịch sử chat lưu theo session
- AI **chỉ đọc** progress — không tự ý sửa/chấm quest của user

---

## MOD-07 — Admin

---

**US-27** — Ẩn quest vi phạm
> As an **Admin**,
> I want to hide a quest that violates community guidelines,
> so that the marketplace remains safe and high quality.

Acceptance Criteria:
- Quest bị hidden không xuất hiện trong Marketplace
- Creator được thông báo lý do

---

**US-28** — Quản lý domain
> As an **Admin**,
> I want to create and manage skill domains,
> so that quests and paths are organized properly.

Acceptance Criteria:
- Tạo / sửa / ẩn skill_domains
- Domain ảnh hưởng cả LearningPath lẫn District trong World

---

**US-29** — Bật/tắt feature flag
> As an **Admin**,
> I want to enable or disable specific features,
> so that I can control the rollout of new functionality.

Acceptance Criteria:
- Feature flag được quản lý qua admin panel
- Thay đổi có hiệu lực ngay, không cần deploy lại
