# QuestHub — Event Contracts

Event payload schemas — producer và consumer phải đồng ý trước khi code

---

**Phase 1 (Modular Monolith):** Events được deliver qua **Outbox Relay → Spring ApplicationEventPublisher**. Không có RabbitMQ. Exchange/routing-key structure bên dưới là contract cho Phase 2.

**Phase 2 (Microservices):** Outbox Relay thay `ApplicationEvent` bằng `RabbitTemplate.send(exchange, routingKey, payload)` — payload schema không thay đổi.

**Convention:** Mọi event có `eventId` (UUID), `timestamp` (ISO 8601 UTC), `version` ("1.0"). Consumer phải idempotent — xử lý trùng `eventId` phải an toàn. Routing key format: `domain.action`.

---

## Exchanges — 4 tổng

| Exchange | Type | Producer | Events |
|---|---|---|---|
| `quest.events` | topic | Quest Module, Marketplace Module | `quest.*`, `task.*` |
| `user.events` | topic | Identity Module, Social Module | `user.*` |
| `social.events` | topic | Social Module, World Module | `social.*`, `achievement.*` |
| `notification.fanout` | fanout | Consumer duy nhất: Notification Module | Nhận event từ các exchange khác qua shovel |

---

## Events — 11 contracts

---

### user.registered

Trigger sau khi Identity Module tạo user thành công. World Module dùng để tạo World + District mặc định.

| | |
|---|---|
| **Exchange** | `user.events` |
| **Routing Key** | `user.registered` |
| **Producer** | Identity Module |
| **Consumers** | World Module |
| **Durable** | persistent |

**Payload Schema**

```json
{
  "eventId":   "uuid-v4",
  "timestamp": "2026-08-16T10:00:00Z",
  "version":   "1.0",

  "userId":    "uuid-v4",
  "username":  "john_doe",
  "email":     "john@example.com"
}
```

| Field | Type | Note |
|---|---|---|
| `eventId` | string (UUID) | Idempotency key — skip nếu đã xử lý |
| `userId` | string (UUID) | FK → users.id |
| `username` | string | Dùng làm World name default |
| `email` | string | Informational only |

**Notes:**
- **World Module:** INSERT worlds (user_id) sau khi nhận event — bảng worlds chỉ có user_id + timestamps.
- **Idempotent:** CHECK EXISTS trước khi INSERT. Trùng userId thì skip.

---

### quest.published

Quest chuyển từ DRAFT → PUBLIC. Kích hoạt ES indexing và activity feed.

| | |
|---|---|
| **Exchange** | `quest.events` |
| **Routing Key** | `quest.published` |
| **Producer** | Marketplace Module |
| **Consumers** | Marketplace Module, Social Module |

**Payload Schema**

```json
{
  "eventId":        "uuid-v4",
  "timestamp":      "2026-08-16T10:00:00Z",
  "version":        "1.0",

  "questId":        "uuid-v4",
  "learningPathId": "uuid-v4",
  "title":          "Learn TypeScript",
  "description":    "A complete guide...",
  "skillDomainId":  "uuid-v4",
  "difficulty":     "INTERMEDIATE",
  "taskCount":      12,
  "creatorId":      "uuid-v4",
  "creatorUsername": "john_doe",
  "publishedAt":    "2026-08-16T10:00:00Z"
}
```

| Field | Type | Note |
|---|---|---|
| `questId` | string (UUID) | PK của quest |
| `learningPathId` | string (UUID) \| null | ES filter + hiển thị path |
| `skillDomainId` | string (UUID) | ES filter + World mapping |
| `difficulty` | string | BEGINNER \| INTERMEDIATE \| ADVANCED |
| `taskCount` | int | Tổng task — hiển thị trên card |
| `creatorUsername` | string | Denorm cho activity display |

**Notes:**
- **Marketplace Module:** Index document vào Elasticsearch, invalidate cache:trending.
- **Social Module:** INSERT activities (type=QUEST_PUBLISHED, user_id=creatorId).

---

### quest.forked

User fork một Quest thành PersonalQuest. Cập nhật fork_count và activity feed.

| | |
|---|---|
| **Exchange** | `quest.events` |
| **Routing Key** | `quest.forked` |
| **Producer** | Quest Module |
| **Consumers** | Quest Module, Social Module, Marketplace Module |

**Payload Schema**

```json
{
  "eventId":        "uuid-v4",
  "timestamp":      "2026-08-16T10:00:00Z",
  "version":        "1.0",

  "questId":        "uuid-v4",
  "questTitle":     "Learn TypeScript",
  "learningPathId": "uuid-v4",
  "personalQuestId": "uuid-v4",
  "userId":         "uuid-v4",
  "username":       "jane_doe"
}
```

| Field | Type | Note |
|---|---|---|
| `questId` | string (UUID) | Quest gốc (template) |
| `questTitle` | string | Denorm cho activity display |
| `learningPathId` | string (UUID) \| null | Path context khi fork từ trong path |
| `personalQuestId` | string (UUID) | PersonalQuest mới tạo |
| `userId` | string (UUID) | User đã fork |

**Notes:**
- **Quest Module:** UPDATE quests SET fork_count = fork_count + 1.
- **Social Module:** INSERT activities (type=QUEST_FORKED, user_id=userId, quest_id=questId).
- **Marketplace Module:** Invalidate Redis cache cho quest card.

---

### task.completed

Event phức tạp nhất — 3 consumers chạy song song. Kèm flag `isQuestCompleted` để biết quest vừa chạm đích.

| | |
|---|---|
| **Exchange** | `quest.events` |
| **Routing Key** | `task.completed` |
| **Producer** | Quest Module |
| **Consumers** | World Module, Social Module, Notification Module |

**Payload Schema**

```json
{
  "eventId":         "uuid-v4",
  "timestamp":       "2026-08-16T10:00:00Z",
  "version":         "1.0",

  "userId":          "uuid-v4",
  "username":        "jane_doe",
  "personalQuestId": "uuid-v4",
  "questId":         "uuid-v4",
  "questTitle":      "Learn TypeScript",
  "chapterId":       "uuid-v4",
  "chapterTitle":    "Type Basics",
  "taskId":          "uuid-v4",
  "taskTitle":       "Setup tsconfig",
  "taskType":        "PRACTICE",
  "skillDomainId":   "uuid-v4",
  "districtId":      "uuid-v4",
  "newProgress":     62,
  "isQuestCompleted": false
}
```

| Field | Type | Note |
|---|---|---|
| `skillDomainId` | string (UUID) | World Module cần để map task → district |
| `districtId` | string (UUID) | Denorm — World Module dùng để UPDATE districts |
| `taskType` | string | LEARN \| QUIZ \| PRACTICE \| SUBMISSION \| REFLECTION |
| `newProgress` | int (0-100) | % hoàn thành sau task này |
| `isQuestCompleted` | boolean | true nếu completion_rule vừa thỏa |
| `chapterTitle` | string | Denorm cho Notification/Feed |

**Notes:**
- **World Module:** UPDATE districts SET completion_count += 1 WHERE id=districtId; kiểm tra mở khóa Building.
- **Social Module:** INSERT activities (TASK_COMPLETED), nếu isQuestCompleted=true thêm activities (QUEST_COMPLETED).
- **Notification Module:** Push "🎯 Completed: {taskTitle}". Nếu isQuestCompleted push "🏆 Quest {questTitle} completed!".

---

### quest.completed

Quest hoàn thành theo completion_rule. Dùng để unlock achievement và cập nhật learning path progress.

| | |
|---|---|
| **Exchange** | `quest.events` |
| **Routing Key** | `quest.completed` |
| **Producer** | Quest Module |
| **Consumers** | World Module, Notification Module |

**Payload Schema**

```json
{
  "eventId":         "uuid-v4",
  "timestamp":       "2026-08-16T10:00:00Z",
  "version":         "1.0",

  "userId":          "uuid-v4",
  "personalQuestId": "uuid-v4",
  "questId":         "uuid-v4",
  "questTitle":      "Learn TypeScript",
  "learningPathId":  "uuid-v4",
  "skillDomainId":   "uuid-v4",
  "completedAt":     "2026-08-16T10:00:00Z"
}
```

| Field | Type | Note |
|---|---|---|
| `learningPathId` | string (UUID) \| null | Cập nhật tiến độ path nếu có |
| `skillDomainId` | string (UUID) | World Module kiểm tra achievement theo domain |
| `completedAt` | timestamp | — |

**Notes:**
- **World Module:** Kiểm tra và unlock Achievement (QUEST_COUNT, FIRST_QUEST, DOMAIN_TASK_COUNT...) → publish achievement.unlocked nếu có.
- **Notification Module:** "🏆 Quest {questTitle} completed!" nếu chưa được push bởi task.completed.

---

### quest.rated

User submit hoặc update review. Quest Module cập nhật denormalized avg_rating / rating_count.

| | |
|---|---|
| **Exchange** | `quest.events` |
| **Routing Key** | `quest.rated` |
| **Producer** | Marketplace Module |
| **Consumers** | Quest Module |

**Payload Schema**

```json
{
  "eventId":       "uuid-v4",
  "timestamp":     "2026-08-16T10:00:00Z",
  "version":       "1.0",

  "questId":       "uuid-v4",
  "userId":        "uuid-v4",
  "newScore":      5,
  "previousScore": null,
  "action":        "CREATED"
}
```

| Field | Type | Note |
|---|---|---|
| `newScore` | int (1-5) | Rating vừa được set |
| `previousScore` | int \| null | null nếu lần đầu rate |
| `action` | CREATED \| UPDATED \| DELETED | Quest Module recalculate avg dựa vào action |

**Notes:**
- **Quest Module:** Recalculate avg_rating = (avg * rating_count - previousScore + newScore) / rating_count (UPDATED) hoặc (avg * count + newScore) / (count+1) (CREATED).

---

### achievement.unlocked

User mở khóa một achievement — feed + notification + profile.

| | |
|---|---|
| **Exchange** | `social.events` |
| **Routing Key** | `achievement.unlocked` |
| **Producer** | World Module |
| **Consumers** | Social Module, Notification Module |

**Payload Schema**

```json
{
  "eventId":       "uuid-v4",
  "timestamp":     "2026-08-16T10:00:00Z",
  "version":       "1.0",

  "userId":        "uuid-v4",
  "achievementId": "uuid-v4",
  "code":          "FIRST_QUEST_COMPLETED",
  "title":         "First Blood",
  "unlockedAt":    "2026-08-16T10:00:00Z"
}
```

| Field | Type | Note |
|---|---|---|
| `code` | string | Unique code từ achievements |
| `title` | string | Denorm cho feed/notification |

**Notes:**
- **Social Module:** INSERT activities (ACHIEVEMENT_UNLOCKED, user_id=userId).
- **Notification Module:** Push "🏅 {title}" cho user.

---

### comment.created

Comment mới trên Quest hoặc Discussion — thông báo người liên quan.

| | |
|---|---|
| **Exchange** | `social.events` |
| **Routing Key** | `social.comment` |
| **Producer** | Social Module |
| **Consumers** | Notification Module |

**Payload Schema**

```json
{
  "eventId":       "uuid-v4",
  "timestamp":     "2026-08-16T10:00:00Z",
  "version":       "1.0",

  "commentId":     "uuid-v4",
  "authorId":      "uuid-v4",
  "authorUsername": "jane_doe",
  "targetType":    "DISCUSSION",
  "targetId":      "uuid-v4",
  "questId":       "uuid-v4",
  "parentId":      null,
  "body":          "Bạn ơi, chapter 3 nên làm trước..."
}
```

| Field | Type | Note |
|---|---|---|
| `targetType` | DISCUSSION \| QUEST | — |
| `questId` | string (UUID) \| null | Thông báo creator quest |
| `parentId` | string (UUID) \| null | Thông báo author comment gốc khi reply |

**Notes:**
- **Notification Module:** Reply → thông báo author comment gốc. Comment trên quest → thông báo creator quest.

---

### discussion.created

Discussion mới trên Quest — thông báo creator + follower của author.

| | |
|---|---|
| **Exchange** | `social.events` |
| **Routing Key** | `social.discussion` |
| **Producer** | Social Module |
| **Consumers** | Notification Module |

**Payload Schema**

```json
{
  "eventId":      "uuid-v4",
  "timestamp":    "2026-08-16T10:00:00Z",
  "version":      "1.0",

  "discussionId": "uuid-v4",
  "authorId":     "uuid-v4",
  "authorUsername": "jane_doe",
  "questId":      "uuid-v4",
  "title":        "Mẹo học chapter 2 nhanh hơn"
}
```

| Field | Type | Note |
|---|---|---|
| `questId` | string (UUID) \| null | Thông báo creator quest |

**Notes:**
- **Notification Module:** Thông báo creator quest có discussion mới.

---

### user.followed

User A follow User B. Notification Module gửi push cho User B.

| | |
|---|---|
| **Exchange** | `user.events` |
| **Routing Key** | `user.followed` |
| **Producer** | Social Module |
| **Consumers** | Notification Module, Identity Module |

**Payload Schema**

```json
{
  "eventId":          "uuid-v4",
  "timestamp":        "2026-08-16T10:00:00Z",
  "version":          "1.0",

  "followerId":       "uuid-v4",
  "followerUsername": "jane_doe",
  "followedUserId":   "uuid-v4"
}
```

| Field | Type | Note |
|---|---|---|
| `followerId` | string (UUID) | User đã follow |
| `followerUsername` | string | Dùng trong notification text |
| `followedUserId` | string (UUID) | User nhận notification |

**Notes:**
- **Notification Module:** Push tới followedUserId: "{followerUsername} đã follow bạn 👋".
- **Identity Module:** UPDATE users SET follower_count += 1 WHERE id=followedUserId, following_count += 1 WHERE id=followerId.

---

### submission.graded

AI chấm xong bài SUBMISSION/PRACTICE. Quest Module dùng để tạo TaskCompletion (nếu PASS) + evaluate completion_rule; Notification gửi kết quả.

| | |
|---|---|
| **Exchange** | `quest.events` |
| **Routing Key** | `submission.graded` |
| **Producer** | AI Module |
| **Consumers** | Quest Module, Notification Module |

**Payload Schema**

```json
{
  "eventId":        "uuid-v4",
  "timestamp":      "2026-08-16T10:00:00Z",
  "version":        "1.0",

  "gradeId":        "uuid-v4",
  "userId":         "uuid-v4",
  "personalTaskId": "uuid-v4",
  "questId":        "uuid-v4",
  "status":         "PASS",
  "score":          92,
  "feedback":       "Đúng trọng tâm rubric...",
  "gradedAt":       "2026-08-16T10:00:00Z"
}
```

| Field | Type | Note |
|---|---|---|
| `gradeId` | string (UUID) | Idempotency key — skip nếu đã xử lý |
| `status` | PASS \| FAIL \| NEEDS_REVISION | Chỉ PASS mới trigger TaskCompletion |
| `personalTaskId` | string (UUID) | Task được chấm |
| `feedback` | string | Hiển thị cho user + notification text |

**Notes:**
- **Quest Module:** status=PASS → INSERT task_completions + UPDATE personal_tasks SET is_completed=true → evaluate completion_rule (có thể raise quest.completed). Idempotent — task_completions.personal_task_id UNIQUE chặn duplicate.
- **Notification Module:** Push "✅ {taskTitle} đạt yêu cầu — AI chấm {score} điểm" (PASS) hoặc "📝 {taskTitle} cần chỉnh sửa — xem feedback" (FAIL/NEEDS_REVISION).
