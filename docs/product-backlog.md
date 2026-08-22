# Product Backlog — QuestHub

> Cập nhật: 2026-08-22

## Tổng quan tiến độ

| Epic | Done | Partial | Not started | Total |
|------|------|---------|-------------|-------|
| 1 — Identity | 2 | 0 | 0 | 2 |
| 2 — Content | 5 | 0 | 0 | 5 |
| 3 — Learning Progress | 6 | 0 | 0 | 6 |
| 4 — Marketplace | 5 | 0 | 0 | 5 |
| 5 — World | 5 | 0 | 0 | 5 |
| 6 — Social | 0 | 0 | 3 | 3 |
| 7 — AI | 0 | 0 | 4 | 4 |
| 8 — Notification | 0 | 0 | 3 | 3 |
| 9 — Admin | 3 | 0 | 0 | 3 |
| **Total** | **26** | **0** | **10** | **36** |

**Progress: 26 / 36 = 72%** (Java monolith core xong, side services chưa bắt đầu)

---

## Legend

| Symbol | Nghĩa |
|--------|-------|
| ✅ | Done — có use case + endpoint |
| 🔧 | Partial — có một phần, chưa đủ |
| ⬜ | Not started |

---

## Epic 1 — Identity · `com.questhub.modules.identity`

| # | User Story | Status | Implementation |
|---|-----------|--------|----------------|
| 1.1 | As a Guest, I want to create an account | ✅ | `RegisterUserUseCase`, `POST /api/v1/auth/register` |
| 1.2 | As a User, I want to update my profile | ✅ | `UpdateProfileUseCase`, `PUT /api/v1/users/me` |

---

## Epic 2 — Content · `com.questhub.modules.quest`

| # | User Story | Status | Implementation |
|---|-----------|--------|----------------|
| 2.1 | As a Creator, I want to create a learning path | ✅ | `CreateLearningPathUseCase`, `POST /api/v1/learning-paths` |
| 2.2 | As a Creator, I want to create a quest with chapters and tasks | ✅ | `CreateQuestUseCase`, `POST /api/v1/quests` |
| 2.3 | As a Creator, I want to add chapters, tasks, and resources | ✅ | `AddChapterUseCase`, `AddTaskUseCase`, `AddResourceUseCase` |
| 2.4 | As a Creator, I want to set a completion rule | ✅ | `SetCompletionRuleUseCase`, `PUT /api/v1/quests/{id}/completion-rule` |
| 2.5 | As a Creator, I want to publish my quest | ✅ | `PublishQuestUseCase`, `POST /api/v1/quests/{id}/publish` |

---

## Epic 3 — Learning Progress · `com.questhub.modules.quest`

| # | User Story | Status | Implementation |
|---|-----------|--------|----------------|
| 3.1 | As a Learner, I want to fork a quest | ✅ | `ForkQuestUseCase`, `POST /api/v1/quests/{id}/fork` |
| 3.2 | As a Learner, I want to complete tasks of different types | ✅ | `CompleteTaskUseCase`, `PUT /api/v1/personal-quests/{pqId}/tasks/{ptId}/complete` |
| 3.3 | As a Learner, I want to take a quiz and see my score | ✅ | `SubmitQuizUseCase`, `POST /api/v1/personal-quests/{pqId}/tasks/{ptId}/quiz-attempts` |
| 3.4 | As a Learner, I want my quest marked completed automatically | ✅ | `EvaluateCompletionUseCase` — triggered after each task completion |
| 3.5 | As a Learner, I want to customize my forked quest | ✅ | `EditPersonalQuestUseCase` — add/remove/reorder chapters & tasks |
| 3.6 | As a Learner, I want to abandon a quest | ✅ | `AbandonQuestUseCase`, `DELETE /api/v1/personal-quests/{id}` |

---

## Epic 4 — Marketplace · `com.questhub.modules.marketplace`

| # | User Story | Status | Implementation |
|---|-----------|--------|----------------|
| 4.1 | As a Guest, I want to browse learning paths and quests | ✅ | `ListPublicLearningPathsQuery`, `GetPopularQuestsQuery`, `GetTrendingQuestsQuery` |
| 4.2 | As a Guest, I want to search quests by keyword | ✅ | `SearchQuestsUseCase`, `GET /api/v1/marketplace/search` |
| 4.3 | As a Learner, I want to rate a quest I have forked | ✅ | `CreateReviewUseCase`, `UpdateReviewUseCase`, `POST /api/v1/marketplace/quests/{id}/reviews` |
| 4.4 | As a Learner, I want to save a quest to favorites | ✅ | `AddFavoriteUseCase`, `RemoveFavoriteUseCase`, `POST /api/v1/marketplace/quests/{id}/favorites` |
| 4.5 | As a Creator, I want to see quest analytics | ✅ | `GetQuestAnalyticsQuery`, `GET /api/v1/quests/{id}/analytics` — fork count, avg rating, completion rate, task drop-off |

---

## Epic 5 — World · `com.questhub.modules.world`

| # | User Story | Status | Implementation |
|---|-----------|--------|----------------|
| 5.1 | As a Learner, I want to see my Knowledge World | ✅ | `GetUserWorldQuery`, `GET /api/v1/world` |
| 5.2 | As a Learner, I want to see district details with buildings | ✅ | `GetDistrictDetailQuery`, `BuildingUnlockService` |
| 5.3 | As a Visitor, I want to view another user's World | ✅ | `GetWorldQuery`, `GET /api/v1/worlds/{username}` — public profiles only |
| 5.4 | As a Learner, I want to unlock achievements | ✅ | `AchievementUnlockService` — triggered on `task.completed` event |
| 5.5 | As a User, I want to see a leaderboard | ✅ | `GetLeaderboardQuery`, `GetLeaderboardStatsQuery` |

---

## Epic 6 — Social · `social` (Go side service)

| # | User Story | Status | Notes |
|---|-----------|--------|-------|
| 6.1 | As a Learner, I want to follow other users | ⬜ | User.followerCount/followingCount counter có trong monolith; Follow table cần Go service |
| 6.2 | As a Learner, I want to see an activity feed | ⬜ | Monolith publish `quest.completed`, `quest.forked`, `quest.published` events — Social service cần consume |
| 6.3 | As a Learner, I want to comment on quests and discussions | ⬜ | |

---

## Epic 7 — AI · `ai-service` (Python side service)

| # | User Story | Status | Notes |
|---|-----------|--------|-------|
| 7.1 | As a Learner, I want quest recommendations based on my goal | ⬜ | |
| 7.2 | As a Learner, I want AI to generate a quest | ⬜ | |
| 7.3 | As a Learner, I want AI to grade my SUBMISSION/PRACTICE task | ⬜ | `SubmissionGrade` entity chưa có migration trong monolith — cần làm đồng thời với AI service |
| 7.4 | As a Learner, I want to chat with an AI coach | ⬜ | |

---

## Epic 8 — Notification · `notification` (Go side service)

| # | User Story | Status | Notes |
|---|-----------|--------|-------|
| 8.1 | As the system, I want to notify User on key learning events | ⬜ | **Next up** — Monolith publish đủ events qua outbox: `quest.completed`, `task.completed`, `achievement.unlocked`, `quest.forked` |
| 8.2 | As the system, I want to notify User when an Achievement is unlocked | ⬜ | `achievement.unlocked` event cần được publish từ `AchievementUnlockService` |
| 8.3 | As a User, I want to manage notification preferences | ⬜ | `User.notificationPrefs` (JSONB) đã có trong monolith |

---

## Epic 9 — Admin · `com.questhub.modules.admin`

| # | User Story | Status | Implementation |
|---|-----------|--------|----------------|
| 9.1 | As an Admin, I want to hide a quest | ✅ | `HideQuestUseCase`, `RestoreQuestUseCase` |
| 9.2 | As an Admin, I want to manage skill domains | ✅ | `CreateSkillDomainUseCase`, `UpdateSkillDomainUseCase`, `DeactivateSkillDomainUseCase` |
| 9.3 | As an Admin, I want to toggle feature flags | ✅ | `ToggleFeatureFlagUseCase` |

---

## Roadmap

```
Phase 1 — Monolith core      ████████████████████  DONE (25/26 US)
Phase 2 — Notification (Go)  ░░░░░░░░░░░░░░░░░░░░  Next
Phase 3 — Social (Go)        ░░░░░░░░░░░░░░░░░░░░  After notification
Phase 4 — AI (Python)        ░░░░░░░░░░░░░░░░░░░░  After social
```

### Còn lại Phase 1
Không còn gì. **Phase 1 — Java monolith hoàn thành 26/26 US.**
