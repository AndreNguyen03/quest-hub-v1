# QuestHub — Screen Inventory

37 màn hình · Landing Page · Web App · Mobile App · Admin

---

## Landing Page — 3 màn hình

### #01 Home

Trang marketing chính. Hero với tagline + 2 CTA ("Explore Quests" / "Get Started"). Giới thiệu sản phẩm, cách hoạt động, ví dụ quests nổi bật.

Sections: Hero + CTA · Features · How It Works · Example Quests · Footer

### #02 Login

Form email + password. OAuth Google / GitHub. Link tới Register và Forgot Password.

Sections: Email form · OAuth buttons · Forgot password link

### #03 Register

Form email + password + username. OAuth. Terms checkbox. Sau khi xong → redirect vào Web App, World được tạo tự động.

Sections: Email form · OAuth buttons · Terms checkbox · Auto-create World

---

## Web App — 20 màn hình

### Marketplace

#### #04 Explore

Trang khám phá chính. Guest xem được không cần login. Hiển thị trending, popular, skill domains grid và featured quest banner.

Sections: Trending quests · Popular quests · Domains grid · Featured banner

#### #05 Search Results

Kết quả tìm kiếm theo keyword. Filter theo skill domain, sort theo trending / newest / rating. Paginated grid.

Sections: Search bar · Domain filter · Sort options · Quest grid · Pagination

#### #06 Domain Browse

Header skill domain + learning paths và quest grid lọc theo domain bên dưới.

Sections: Domain header · Learning paths · SkillDomain info · Quest grid

#### #07 Quest Detail (Public)

Xem quest trước khi dùng. Cover, mô tả, preview chapters & tasks (không tick được), creator info, ratings, fork count. Nút "Use Quest" yêu cầu login.

Sections: Cover + meta · Chapters preview · Creator card · Ratings · Use Quest CTA

### Quest

#### #08 My Dashboard

Tổng quan tiến độ cá nhân. Active PersonalQuests với progress bar, recently completed, quick stats tuần này.

Sections: Active quests · Progress bars · Recently completed · Weekly stats

#### #09 Create Quest

Wizard 3 bước: ① Thông tin (title, description, skill domain) → ② Chapters & Tasks (add/reorder) → ③ Settings (visibility, thumbnail, completion_rule) → Publish.

Sections: Step 1: Info · Step 2: Chapters & Tasks · Step 3: Settings · Publish

#### #10 Edit Quest

Giống Create nhưng pre-filled. Chỉ chỉnh được Quest chưa bị fork, hoặc một số fields nhất định sau khi đã fork.

Sections: Pre-filled form · Edit restrictions · Save / Unpublish

#### #11 Quest Tracking (PersonalQuest)

Màn hình chính để làm quest. Progress bar tổng, danh sách chapters với các task checkbox, ngày hoàn thành từng cái, nút "Add note". Hiển thị forked from Quest gốc.

Sections: Progress bar · Chapter/task checklist · Completion dates · Notes · Forked from

#### #12 My Favorites

Grid các quests đã save. Toggle remove favorite. Không cần fork để vào đây.

Sections: Saved quest grid · Remove toggle

### Creator

#### #13 Creator Analytics

Chọn quest → xem hiệu quả: fork count, completion rate, drop-off rate theo từng task (task nào người ta bỏ nhiều nhất), average rating, reviews.

Sections: Quest selector · Fork count · Completion rate · Drop-off per task · Ratings & reviews

### World

#### #14 My World

Grid các Districts. Mỗi District hiển thị SkillDomain + tổng TaskCompletion. Click vào District để xem chi tiết.

Sections: District grid · Completion count per domain · Overall stats

#### #15 District Detail

Xem một SkillDomain cụ thể. Stats domain, danh sách PersonalQuests đang active, danh sách đã complete trong domain này.

Sections: Domain stats · Active quests · Completed quests

### Profile & Social

#### #16 My Profile

Trang cá nhân của mình. Avatar, bio, social links, public quests đã tạo, world preview nhỏ, followers/following count.

Sections: Avatar + bio · Public quests · World preview · Followers / Following

#### #17 Public Profile

Xem profile người khác. Giống My Profile nhưng read-only + nút Follow/Unfollow. Chỉ hiển thị nếu profile đó public.

Sections: Read-only view · Follow / Unfollow · Public quests · World preview

#### #18 Activity Feed

Feed theo thời gian từ những người đang follow. Các loại activity: "A completed quest X", "B started quest Y", "C published quest Z".

Sections: Chronological feed · Activity types · Link to quest / profile

### AI

#### #19 AI Advisor

Text input mô tả mục tiêu → AI gợi ý quests có sẵn trong hệ thống. Nếu không có → offer "Generate a new quest". Preview quest được generate trước khi dùng.

Sections: Goal input · Quest recommendations · Generate quest · Quest preview

#### #37 AI Grader — Review Submission

Nộp bài task SUBMISSION/PRACTICE (text/URL) → bấm "Chấm bằng AI" → hiện kết quả PASS/FAIL/NEEDS_REVISION + score + feedback theo rubric. PASS → task tự hoàn thành. FAIL → hiện nút nộp lại.

Sections: Submission input · Grade result · Score + feedback · Resubmit

#### #38 AI Coach — Chat

Chat với AI coach biết trạng thái quest thật của bạn. Hỏi "tôi đang kẹt ở đâu?" → trả lời dựa trên progress + gợi ý bước tiếp. Streaming response, lịch sử theo session.

Sections: Chat bubbles · Progress-aware reply · Next-step suggestion · Session history

### Settings

#### #20 Settings

Cài đặt tài khoản. Profile info, đổi password, notification preferences, privacy (public/private), connected OAuth accounts.

Sections: Profile info · Password · Notifications · Privacy · OAuth connections

---

## Mobile App — 10 màn hình

### #21 Today / Home

Màn hình chính, focus vào hôm nay. Active PersonalQuests, tasks cần làm hôm nay, streak counter, quick-complete button ngay trên màn hình.

Sections: Active quests · Today's tasks · Streak counter · Quick complete

### #22 Explore (mobile)

Search bar trên cùng + skill domains horizontal scroll + trending quests dạng card dọc.

Sections: Search bar · Domains scroll · Trending list

### #23 Quest Detail (mobile)

Chapters & tasks list, progress bar, nút "Use Quest" hoặc "Continue" nếu đã fork.

Sections: Chapters & tasks list · Progress bar · Use / Continue CTA

### #24 Task Check

Fullscreen focused: tên task to, checkbox lớn, optional note, nút Confirm. Thiết kế để cảm giác satisfying khi tick xong.

Sections: Task name · Big checkbox · Optional note · Confirm button

### #25 My Quests

Danh sách active + completed PersonalQuests. Filter toggle giữa hai tab.

Sections: Active tab · Completed tab · Progress per quest

### #26 My World (mobile)

Simplified: district list dạng dọc với progress bar và tổng completion count theo từng domain.

Sections: District list · Progress per domain · Total stats

### #27 Profile (mobile)

Avatar, stats tóm tắt, public quests, world snapshot nhỏ.

Sections: Avatar + stats · Public quests · World snapshot

### #28 Notifications

Activity list: completions của người follow, new followers, quest updates từ creator.

Sections: Activity list · Unread indicator

### #29 Search (mobile)

Fullscreen search với recent searches và suggestions theo keyword.

Sections: Search input · Recent searches · Suggestions

### #30 Settings (mobile)

Notification preferences, privacy, account info. Đơn giản hơn web settings.

Sections: Notifications · Privacy · Account

---

## Admin — 6 màn hình

### #31 Dashboard

Platform overview: tổng users, quests, completions hôm nay và tuần này. Charts đơn giản.

Sections: User count · Quest count · Completion count · Charts

### #32 Quest Moderation Queue

Danh sách quests bị report hoặc pending review. Filter theo status: Pending / Reviewed / Hidden.

Sections: Report queue · Status filter · Quick actions

### #33 Quest Detail (Admin view)

Xem toàn bộ quest bao gồm cả private content. Actions: Hide, Approve, Flag, Add internal note.

Sections: Full quest view · Hide / Approve · Flag · Internal notes

### #34 User Management

Search user, xem profile, assign role (User / Creator / Admin), ban account.

Sections: User search · Role assignment · Ban / Restore

### #35 Skill Domain Management

CRUD skill domains, reorder, hide/show. Thay đổi ảnh hưởng toàn bộ Marketplace và World (district).

Sections: SkillDomain CRUD · Reorder · Hide / Show

### #36 Feature Flags

List tất cả features với toggle on/off, mô tả, scope ảnh hưởng. Thay đổi có hiệu lực ngay, không cần deploy lại.

Sections: Feature list · Toggle on / off · Affected scope · Instant effect

---

## Tổng kết

| Platform | Số màn hình |
|---|---:|
| Landing Page | 3 |
| Web App | 20 |
| Mobile App | 10 |
| Admin | 6 |
| **Tổng** | **39** |
