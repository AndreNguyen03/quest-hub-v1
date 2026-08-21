# QuestHub — Module Ownership

Mỗi module chỉ được write vào table nó sở hữu — ranh giới rõ ràng ngăn coupling ẩn

---

**Nguyên tắc:** Một table chỉ có 1 module được phép INSERT/UPDATE/DELETE (**OWN**). Module khác muốn data đó → gọi API hoặc nhận event. Direct cross-module DB write là anti-pattern trong Modular Monolith + DDD — boundaries phải rõ ràng ngay từ Phase 1 để việc tách microservices sau này không đau.

**Phase 1 (Modular Monolith):** Identity, Quest, Marketplace, World, Admin chạy trong 1 Spring Boot app (port 8080, share cùng PostgreSQL). Social + Notification là Go/Fiber apps riêng (port 8081/8082). AI là Python/FastAPI riêng (port 8090). Boundaries được enforce bởi package structure + ArchUnit tests (Java) và DB roles.

**Phase 2 (Microservices):** Mỗi module tách thành service riêng với port và DB riêng.

**Legend:**
- **OWN** — read + write, chạy migrations
- **READ** — read-only khi cần thiết (tránh nếu có thể, dùng API call)
- **—** — không access

---

## Modules — ownership chi tiết

### Identity Module

**Java / Spring Boot · Port 9090**

| Ownership | Tables |
|---|---|
| **Owns** | `users` |
| **Không access** | tất cả tables khác |

Session/refresh tokens lưu ở Redis, không phải DB. Đăng ký → publish `user.registered` để World tạo World mặc định.

---

### Quest Module

**Java / Spring Boot · Port 9090**

| Ownership | Tables |
|---|---|
| **Owns** | `learning_paths`, `quests`, `chapters`, `tasks`, `resources`, `personal_quests`, `personal_chapters`, `personal_tasks`, `task_completions`, `quiz_attempts` |
| **Read-only (avoid if possible)** | `users`, `skill_domains` |

---

### Marketplace Module

**Java / Spring Boot · Port 9090**

| Ownership | Tables |
|---|---|
| **Owns** | `reviews`, `favorites` |
| **Read-only** | `quests`, `learning_paths`, `users`, `skill_domains` |

`quests.avg_rating / fork_count` là denorm của Quest Module — Marketplace chỉ publish `quest.rated`, không write trực tiếp.

---

### World Module

**Java / Spring Boot · Port 9090**

| Ownership | Tables |
|---|---|
| **Owns** | `worlds`, `districts`, `buildings`, `achievements`, `user_achievements` |
| **Read-only** | `users`, `skill_domains` |

District/achievement update qua event `task.completed` + `quest.completed` — không sync call từ Quest Module.

---

### Social Module

**Go / Fiber · Port 8081**

| Ownership | Tables |
|---|---|
| **Owns** | `follows`, `activities`, `comments`, `discussions` |
| **Read-only** | `users` |

Writer duy nhất của `activities` — module khác INSERT outbox_events, Social listener tạo Activity.

---

### Notification Module

**Go / Fiber · Port 8082 · internal**

| Ownership | Tables |
|---|---|
| **Owns** | `notifications` |
| **Read-only** | `users` |

Đọc `users.notification_prefs` + push token, gửi Expo Push / Resend email.

---

### AI Module

**Python / FastAPI · Port 8090 · separate app**

| Ownership | Tables |
|---|---|
| **Owns** | `submission_grades`, `coach_sessions`, `coach_messages` |
| **Read-only (training + inference)** | `quests`, `tasks`, `learning_paths`, `personal_quests`, `task_completions`, `quiz_attempts`, `reviews`, `favorites`, `users`, `skill_domains` |

Dùng read replica hoặc scheduled export khi training — không query production DB trực tiếp. AI Coach chỉ READ — không tool nào write quest.

---

### Admin Module

**Java / Spring Boot · Port 9090**

| Ownership | Tables |
|---|---|
| **Owns** | `skill_domains`, `feature_flags` |
| **Read-only (tất cả tables — audit only)** | `users`, `quests`, `activities`, `reviews`, + all others |

Domain CRUD ảnh hưởng cả LearningPath lẫn District — thay đổi qua event, không write xuyên module.

---

## Quick-reference matrix

| Table | Identity | Quest | Marketplace | World | Social | Notif. | AI | Admin |
|---|---|---|---|---|---|---|---|---|
| `users` | **W** | R | R | R | R | R | R | R |
| `skill_domains` | — | R | R | R | — | — | — | **W** |
| `learning_paths` | — | **W** | R | — | — | — | — | R |
| `quests` | — | **W** | R | — | R | — | R | R |
| `chapters` | — | **W** | — | — | — | — | R | R |
| `tasks` | — | **W** | — | — | — | — | R | R |
| `resources` | — | **W** | — | — | — | — | — | R |
| `worlds` | — | — | — | **W** | — | — | — | R |
| `districts` | — | — | — | **W** | — | — | — | R |
| `buildings` | — | — | — | **W** | — | — | — | R |
| `personal_quests` | — | **W** | — | — | — | — | R | R |
| `personal_chapters` | — | **W** | — | — | — | — | — | R |
| `personal_tasks` | — | **W** | — | — | — | — | — | R |
| `task_completions` | — | **W** | — | — | — | — | R | R |
| `quiz_attempts` | — | **W** | — | — | — | — | — | R |
| `reviews` | — | — | **W** | — | — | — | R | R |
| `comments` | — | — | — | — | **W** | — | — | R |
| `discussions` | — | — | — | — | **W** | — | — | R |
| `achievements` | — | — | — | **W** | — | — | — | R |
| `user_achievements` | — | — | — | **W** | — | — | — | R |
| `notifications` | — | — | — | — | — | **W** | — | R |
| `favorites` | — | — | **W** | — | — | — | R | R |
| `follows` | — | — | — | — | **W** | — | — | R |
| `activities` | — | — | — | — | **W** | — | — | R |
| `feature_flags` | — | R | R | R | — | — | R | **W** |
| `submission_grades` | — | R | — | — | — | — | **W** | R |
| `coach_sessions` | — | — | — | — | — | — | **W** | R |
| `coach_messages` | — | — | — | — | — | — | **W** | R |
| `outbox_events` (infra) | **W** | **W** | **W** | **W** | **W** | — | — | — |

> **W** = OWN (read + write) · **R** = READ-only · **—** = no access

---

## Rules — enforced khi code

1. **Không bao giờ JOIN cross-module.** Quest Module không được JOIN quests với activities (Social Module's table). Nếu cần data, gọi internal API hoặc nhận từ event.

2. **Denormalized fields là exception có chủ đích.** `quests.fork_count / avg_rating`, `districts.completion_count`, `users.follower_count`, `personal_quests.progress`, `personal_tasks.is_completed` được update trong module sở hữu hoặc async qua event — không phải cross-module write.

3. **Social Module là writer duy nhất của `activities`.** Quest/World/Identity không INSERT activities trực tiếp — INSERT outbox_events → Outbox Relay → Social listener xử lý.

4. **AI Module không query production DB khi training.** Dùng scheduled export hoặc read replica để tránh ảnh hưởng performance. Inference endpoint (serving) có thể query normal.

5. **Migrations chỉ do owner module chạy.** Quest Module chạy Flyway migrations cho `learning_paths, quests, chapters, tasks, resources, personal_*`. World cho `worlds, districts, buildings, achievements`. Không share migration script.

6. **`outbox_events` là bảng infra — không có module owner.** Module nào publish domain event thì ghi row trong cùng transaction; Outbox Relay xử lý với idempotency key `eventId`.

7. **AI Module là writer duy nhất của `submission_grades` / `coach_sessions` / `coach_messages`.** Quest Module chỉ đọc `submission_grades` để biết kết quả PASS — không INSERT. AI Coach tools chỉ READ, không có tool write quest.
