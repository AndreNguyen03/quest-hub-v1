# QuestHub — Wireframes

5 key screens · Lo-fi · Web App (1024px viewport)

**Table of Contents:** 1 · Explore | 2 · Quest Detail | 3 · Personal Quest Tracker | 4 · World View | 5 · Create Quest | 6 · AI Grader | 7 · AI Coach

---

## WF-01 — Explore (Marketplace Home)

Screen #04 — Web App · Guest-accessible · US-08, US-09, US-10

### Wireframe Layout

**Navbar:** QuestHub | Search quests... | Explore (active) · My Quests · World | [Log in]

**Filter chips:** All (active) · Programming · Fitness · Language · Finance · Design · Science | Sort: Popular

**Featured banner:**
- [Featured] — Quest title · description lines · ★★★★☆ 4.2 · 312 learners | [Use Quest]

**① Trending this week** — 3-col grid:

| Card 1 | Card 2 | Card 3 |
|--------|--------|--------|
| thumbnail | thumbnail | thumbnail |
| Title | Title | Title |
| ★ 4.5 · 128 learners [Use] | ★ 4.1 · 87 learners [Use] | ★ 4.8 · 210 learners [Use] |

**② Popular all time** — 4-col compact grid (img + title + subtitle each)

### Annotations

- **① Trending** — 3-col grid. Sorted by recent activity (7 ngày). Scroll → more.
- **② Popular** — 4-col grid, compact cards. Sort by avg_rating × log(forks).
- **③ Filter chips** — Multi-select. All = no filter. Kết quả update instant, không reload page.
- **④ Guest CTA** — "Use Quest" redirect tới Login nếu chưa đăng nhập. Session saved, redirect back sau login.

---

## WF-02 — Quest Detail

Screen #08 — Web App · US-03, US-11, US-12

### Wireframe Layout

**Navbar:** QuestHub | Explore · My Quests · World | [avatar]

**Breadcrumb:** Explore / Programming / Backend Development

**Title:** Learn PostgreSQL from Zero

**Meta:** by johndoe · ★★★★☆ 4.3 (48 ratings) · 215 learners · 8 chapters · ~3 months · [PostgreSQL]

**Tabs:** Overview (active) | Chapters & Tasks (8) | Reviews (48)

**Left panel — Overview:**

Description text lines...

Chapters preview:
1. Install & configure PostgreSQL locally
2. Learn basic SQL: SELECT, INSERT, UPDATE, DELETE
3. Understand indexes and query plans
4. + 5 more chapters

**Right panel — Action card:**

Start this Quest

| 215 Learners | 4.3★ Rating | 68% Complete |

[① Use Quest (Fork)]
[♡ Save to favorites]

Created by **johndoe** · Last updated 2 weeks ago

② Already forked? [Go to your quest →]

### Annotations

- **① Use Quest** — Fork action. 409 nếu đã fork → button đổi thành "Go to your quest".
- **② Already forked** — Hiển thị link tới PersonalQuest nếu user đã fork quest này.
- **③ Chapters preview** — Hiển thị 3 chapters đầu, collapse phần còn lại. Không reveal hết để tạo curiosity.

---

## WF-03 — Personal Quest Tracker

Screen #14 — Web App · US-04, US-05, US-06

### Wireframe Layout

**Navbar:** QuestHub | Explore · My Quests (active) · World | [avatar]

**Left sidebar:**

My Active Quests:
- Learn PostgreSQL (active)
- React Native Basics
- System Design 101
- IELTS 7.0 Prep

Completed:
- HTML & CSS Fundamentals

**Main panel:**

**Learn PostgreSQL**
Forked from johndoe's quest · Started Jun 3 | [Edit chapters/tasks]

**① Progress**
37.5% — 3 of 8 tasks completed
[████████░░░░░░░░░░░░░░] 37.5%

**② Chapters & Tasks:**

| Status | Task | Date |
|--------|------|------|
| ✓ | ~~Install & configure PostgreSQL locally~~ | Jun 3 |
| ✓ | ~~Learn basic SQL: SELECT, INSERT, UPDATE, DELETE~~ | Jun 7 |
| ✓ | ~~Understand indexes and query plans~~ | Jun 12 |
| ☐ | JOINs and relationships in depth | [Done] |
| ☐ | Transactions and ACID properties | [Done] |
| ☐ | Build a real project with Prisma ORM | [Done] |
| ③ + Add custom chapter/task | | |

### Annotations

- **① Progress bar** — Real-time — update ngay khi tick/untick task. Animated fill.
- **② Tick to complete** — Click checkbox → PATCH API call → optimistic UI update. Undo = click lại.
- **③ Custom chapter/task** — Learner tự thêm chapter/task custom vào PersonalQuest — không có trong template gốc (US-06).

---

## WF-04 — World View

Screen #18 — Web App · US-14, US-15, US-16

### Wireframe Layout

**Navbar:** QuestHub | Explore · My Quests · World (active) | [avatar]

**Header:**
johndoe's Knowledge World
① 4 districts unlocked · 28 tasks completed | [Share World]

**Left — World visualization (bubble map):**

- 💻 Programming — 18 completions (large bubble)
- 💪 Fitness — 5 (medium bubble)
- 📚 Language — 3 (small bubble)
- 💰 Finance — 2 (smallest bubble)

② Click district để xem chi tiết

District size = số tasks đã hoàn thành (TaskCompletion) trong domain đó

**Right — ③ Programming District detail:**

| 18 Completed | 3 Active | 2 Quests done |

Completed quests:
- ✓ HTML & CSS Fundamentals
- ✓ JavaScript Basics

In progress:
- Learn PostgreSQL — 37% progress
- React Native Basics — 20% progress

### Annotations

- **① World summary** — Districts unlocked = skill domains với ít nhất 1 completion. Tổng tasks là source of truth.
- **② District blobs** — Size proportional to completion_count. Generative layout bằng D3 force simulation. Click để drill down.
- **③ District detail** — Right panel hiện khi click district. Completed quests + in-progress quests trong domain đó.

---

## WF-05 — Create Quest

Screen #20 — Web App · US-01, US-02, US-07

### Wireframe Layout

**Navbar:** QuestHub | Explore · My Quests (active) · World | [avatar]

**Header:** Create Quest
Draft — không hiển thị trong Marketplace cho đến khi bạn Publish | [Save draft] [① Publish]

**Left panel — Quest Details form:**

- **Title \*** — Learn PostgreSQL from Zero
- **Description** — A practical guide to mastering PostgreSQL for backend development...
- **Domain \*** — Backend Development ▾

**② Chapters & Tasks** — 6 added (drag-to-reorder):

- ⠿ Install & configure PostgreSQL locally [✕]
- ⠿ Learn basic SQL: SELECT, INSERT, UPDATE, DELETE [✕]
- ⠿ Understand indexes and query plans [✕]
- ⠿ JOINs and relationships in depth [✕]

[③ + Add chapter/task]

**Right panel:**

**④ Preview card:**
[Quest cover (auto-generated)]
Learn PostgreSQL from Zero
by you · 6 chapters · 18 tasks · Backend Development

**Checklist trước khi Publish:**
- ✓ Title & description đã điền
- ✓ Ít nhất 1 chapter với 1 task
- ✓ Domain đã chọn
- ✗ Chưa có cover image

### Annotations

- **① Publish** — Disabled nếu chưa có chapter (với ít nhất 1 task). Confirm dialog trước khi publish. Không thể undo chapters/tasks sau khi quest có learner.
- **② Drag-to-reorder** — ⠿ handle cho drag-and-drop ordering. Keyboard-accessible (arrow keys).
- **③ Add chapter/task** — Thêm chapter/task mới inline, focus vào input ngay. Enter để confirm, Escape để cancel.
- **④ Live preview** — Preview card cập nhật real-time theo form input. Creator thấy quest sẽ trông như thế nào trên Marketplace.

---

## WF-06 — AI Grader — Review Submission

Screen #37 — Web App · US-30

### Wireframe Layout

**Navbar:** QuestHub | Explore · My Quests (active) · World | [avatar]

**Breadcrumb:** Chapter 2 · Task 4 — SUBMISSION

**Title:** Deploy a Docker container to a cloud VM

**Left panel — Submission + Result:**

Submission (lần 2):
github.com/johndoe/learn-docker · mô tả quá trình deploy + kết quả curl
[① Chấm bằng AI]

Result card:
**② PASS** Score **92/100** · graded by AI · rubric snapshot v2

**③ Feedback theo rubric:**
- ✓ Container chạy đúng — health check pass (criterion: running container)
- ✓ Port mapping + firewall config đầy đủ (criterion: accessibility)
- △ Chưa có log rotation — cải thiện ở bài sau (criterion: best practices)

---

Lần 1 — FAIL (55/100) · feedback: thiếu health check + port không mở ngoài

**Right panel — ④ Rubric:**

- Running container (30%)
- Accessibility từ ngoài (25%)
- Documentation & demo (25%)
- Best practices (20%)

---

*WF-07 — AI Coach listed in table of contents (content not yet documented)*
