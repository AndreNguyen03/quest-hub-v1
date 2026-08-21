# QuestHub — Sequence Diagrams

7 key flows — request/response + async event chains

**Contents:**
1. [Complete Task](#flow-1-complete-task)
2. [Fork Quest](#flow-2-fork-quest)
3. [Publish Quest](#flow-3-publish-quest)
4. [Search Quest](#flow-4-search-quest)
5. [Auth & Token Refresh](#flow-5-auth--login--token-refresh)
6. [AI Grade Submission](#flow-6-ai-grade-submission-ai-grader)
7. [AI Coach Chat](#flow-7-ai-coach-chat-tool-calling)

---

## Flow 1: Complete Task

Outbox Pattern — DB write + outbox INSERT trong 1 transaction, Relay deliver async

```mermaid
sequenceDiagram
    actor L as Learner
    participant W as Web App
    participant QM as Quest Module
    participant DB as PostgreSQL
    participant OR as Outbox Relay
    participant WM as World Module
    participant SM as Social Module
    participant NM as Notification Module

    L->>W: tick task checkbox
    W->>QM: PATCH /personal-quests/:id/tasks/:taskId/complete
    activate QM

    rect rgb(240, 253, 244)
        Note over QM,DB: 1 DB transaction — atomic · nếu COMMIT fail → không có gì xảy ra
        QM->>DB: INSERT task_completions (personal_task_id)
        QM->>DB: UPDATE personal_tasks SET is_completed = true
        QM->>DB: recalculate & UPDATE personal_quests.progress
        QM->>DB: evaluate completion_rule (ALL_TASKS / QUIZ_SCORE / ...)
        alt completion_rule thỏa mãn
            QM->>DB: UPDATE personal_quests SET status='COMPLETED', completed_at=NOW()
            QM->>DB: INSERT outbox_events (event_type='quest.completed', status='PENDING')
        end
        QM->>DB: INSERT outbox_events (event_type='task.completed', status='PENDING')
    end

    QM-->>W: 200 OK {progress, isCompleted}
    deactivate QM
    W-->>L: update progress bar in UI

    Note over OR,NM: Outbox Relay — @Scheduled mỗi ~1s, chạy trong cùng app (Phase 1)
    OR->>DB: SELECT * FROM outbox_events WHERE status='PENDING' FOR UPDATE SKIP LOCKED LIMIT 100
    DB-->>OR: rows cần xử lý
    OR->>DB: UPDATE outbox_events SET status='PROCESSING'

    par World update
        OR->>WM: ApplicationEvent(TaskCompleted)
        WM->>DB: UPSERT districts SET completion_count += 1
        WM->>DB: unlock building nếu đạt milestone
    and Feed update
        OR->>SM: ApplicationEvent(TaskCompleted)
        SM->>DB: INSERT activities (TASK_COMPLETED)
    and Notification
        OR->>NM: ApplicationEvent(TaskCompleted)
        NM-->>L: push "Task completed! 🎯"
    end

    OR->>DB: UPDATE outbox_events SET status='PROCESSED', processed_at=NOW()

    Note over OR,NM: Quest vừa hoàn thành → Relay cũng deliver quest.completed
    OR->>WM: ApplicationEvent(QuestCompleted)
    WM->>DB: kiểm tra achievement theo skill_domain
    alt achievement mới unlock
        WM->>DB: INSERT user_achievements + outbox_events (achievement.unlocked)
    end
```

**Notes:**
- **Atomic:** DB write + outbox INSERT trong 1 transaction. Nếu crash sau COMMIT, Relay retry. Không bao giờ mất event.
- **CompletionRule:** Cấu hình được — ALL_TASKS, QUIZ_SCORE, SUBMISSION, ALL_OF, ANY_OF. Task QUIZ hoàn thành qua `POST .../quiz-attempts` (score ≥ passThreshold), không qua PATCH complete.
- **SKIP LOCKED:** Nhiều Relay instance chạy song song vẫn an toàn — mỗi instance lock row riêng.
- **At-least-once:** Relay có thể deliver 2 lần nếu crash giữa chừng. Consumers phải idempotent — check eventId trước khi xử lý.
- **Phase 2:** Khi tách microservices, Relay thay `ApplicationEvent` bằng `RabbitTemplate.send()` — transaction logic không thay đổi.

---

## Flow 2: Fork Quest

Copy-on-fork pattern — tạo PersonalQuest độc lập, cập nhật fork_count và activity async via Outbox

```mermaid
sequenceDiagram
    actor L as Learner
    participant W as Web App
    participant QM as Quest Module
    participant DB as PostgreSQL
    participant OR as Outbox Relay
    participant SoM as Social Module

    L->>W: click "Use Quest"
    W->>QM: POST /quests/:id/fork
    activate QM

    QM->>DB: check UNIQUE(user_id, quest_id) — đã fork chưa?
    alt đã fork rồi
        QM-->>W: 409 Conflict {error: "ALREADY_FORKED"}
        W-->>L: redirect tới existing PersonalQuest
    else chưa fork
        rect rgb(240, 253, 244)
            Note over QM,DB: Transaction — atomic
            QM->>DB: SELECT quest + chapters + tasks
            QM->>DB: INSERT personal_quests {status:'ACTIVE', progress:0, completion_rule snapshot}
            QM->>DB: INSERT personal_chapters + personal_tasks (copy từng chapter/task)
            QM->>DB: INSERT outbox_events {type:'quest.forked', questId}
        end
        QM-->>W: 201 Created {personalQuestId, title, taskCount}
        deactivate QM
        W-->>L: redirect tới Personal Quest Tracker
    end

    Note over OR,SoM: Async — Outbox Relay ~1s sau COMMIT
    OR->>DB: SELECT status='PENDING' FOR UPDATE SKIP LOCKED
    OR->>QM: ApplicationEvent(QuestForked) — Quest Module listener
    QM->>DB: UPDATE quests SET fork_count = fork_count + 1
    QM->>DB: invalidate Redis cache cho quest card
    OR->>SoM: ApplicationEvent(QuestForked) — Social Module listener
    SoM->>DB: INSERT activities {type:'QUEST_FORKED', userId, questId}
    OR->>DB: UPDATE outbox_events SET status='PROCESSED'
```

**Notes:**
- **Copy-on-fork:** Chapters + tasks được copy sang personal_chapters/personal_tasks kèm snapshot completion_rule. Quest gốc thay đổi sau đó không ảnh hưởng Learner.
- **Ownership:** Quest Module owns quests → update fork_count. Social Module owns activities → insert feed entry. Không module nào cross-write.
- **Outbox đảm bảo:** INSERT outbox_events trong cùng transaction với INSERT personal_quests. Relay giao event at-least-once — consumers phải idempotent (check eventId).

---

## Flow 3: Publish Quest

Visibility change bởi Quest Module + Elasticsearch indexing async via Outbox

```mermaid
sequenceDiagram
    actor C as Creator
    participant W as Web App
    participant QM as Quest Module
    participant DB as PostgreSQL
    participant OR as Outbox Relay
    participant MM as Marketplace Module
    participant ES as Elasticsearch
    participant Cache as Redis

    C->>W: click "Publish"
    W->>QM: POST /quests/:id/publish
    activate QM

    QM->>DB: SELECT quest WHERE id=? AND creator_id=current_user
    alt quest không tồn tại hoặc không phải creator
        QM-->>W: 403 Forbidden
    else không có chapter hoặc task nào
        QM-->>W: 400 Bad Request {error: "QUEST_NEEDS_CONTENT"}
    else OK
        rect rgb(240, 253, 244)
            Note over QM,DB: Transaction — atomic
            QM->>DB: UPDATE quests SET visibility='PUBLIC', published_at=NOW()
            QM->>DB: INSERT outbox_events {type:'quest.published', questId, title, skillDomainId}
        end
        QM-->>W: 200 OK
        deactivate QM
        W-->>C: show toast "Quest is now live ✓"
    end

    Note over OR,Cache: Async — search index cập nhật sau vài giây
    OR->>DB: SELECT status='PENDING' FOR UPDATE SKIP LOCKED
    OR->>MM: ApplicationEvent(QuestPublished) — Marketplace Module listener
    MM->>ES: index document {id, title, description, skillDomain, creator}
    MM->>Cache: invalidate cache:trending, cache:popular
    OR->>DB: UPDATE outbox_events SET status='PROCESSED'
```

**Notes:**
- **Ownership:** Quest Module owns quests → chỉ Quest Module được UPDATE visibility. Marketplace Module lắng nghe event để index Elasticsearch.
- **Search lag:** ES index cập nhật async (~1-3s). Quest chưa xuất hiện ngay trong search sau publish.
- **Unpublish:** Reverse flow — Quest Module set visibility=DRAFT, INSERT outbox_events(quest.unpublished), Marketplace Module xóa ES doc + invalidate cache.

---

## Flow 4: Search Quest

Cache-first strategy — Redis → Elasticsearch fallback

```mermaid
sequenceDiagram
    actor U as User
    participant W as Web App
    participant GW as API Gateway
    participant MM as Marketplace Module
    participant Cache as Redis
    participant ES as Elasticsearch

    U->>W: nhập keyword + chọn filter skill domain
    W->>GW: GET /marketplace/quests?q=keyword&domain=backend
    GW->>GW: verify JWT (optional — guest OK)
    GW->>MM: forward request

    MM->>MM: hash(params) → cache_key
    MM->>Cache: GET cache:search:{cache_key}

    alt Cache hit (TTL 5 phút)
        Cache-->>MM: cached results
        MM-->>GW: 200 OK (X-Cache: HIT)
        GW-->>W: results
    else Cache miss
        MM->>ES: query {query:{multi_match:{query:keyword, fields:[title,description]}}, filter:{term:{domain}}, sort:[{avg_rating:desc},{fork_count:desc}]}
        ES-->>MM: ranked results
        MM->>Cache: SET cache:search:{cache_key} TTL=300s
        MM-->>GW: 200 OK (X-Cache: MISS)
        GW-->>W: results
    end

    W-->>U: render quest cards
```

**Notes:**
- **Cache key:** Hash của (q, domain, sort, page, limit). Khác params = khác cache entry.
- **Cache invalidation:** Khi quest mới publish hoặc rating thay đổi, cache liên quan bị xóa.
- **Guest support:** Search không cần login. Gateway skip JWT check nếu không có Authorization header.

---

## Flow 5: Auth — Login & Token Refresh

JWT access token (15 phút) + httpOnly refresh token (7 ngày), rotation strategy

```mermaid
sequenceDiagram
    actor U as User
    participant W as Web App
    participant IM as Identity Module
    participant DB as PostgreSQL
    participant Cache as Redis

    rect rgb(240, 253, 244)
        Note over U,Cache: Login flow
        U->>W: submit email + password
        W->>IM: POST /auth/login
        IM->>DB: SELECT user WHERE email=?
        IM->>IM: bcrypt.verify(password, hash)
        alt sai mật khẩu
            IM-->>W: 401 Unauthorized
        else đúng
            IM->>IM: generate accessToken (JWT, 15m)
            IM->>IM: generate refreshToken (opaque, 7d)
            IM->>Cache: SET refresh:{userId}:{tokenId} = hash(refreshToken) TTL=7d
            IM-->>W: {accessToken} + Set-Cookie: refreshToken (httpOnly, Secure)
            W->>W: store accessToken in memory (NOT localStorage)
            W-->>U: logged in
        end
    end

    rect rgb(239, 246, 255)
        Note over U,Cache: Token refresh flow (accessToken hết hạn)
        W->>IM: POST /auth/refresh (cookie: refreshToken)
        IM->>IM: decode refreshToken → extract userId + tokenId
        IM->>Cache: GET refresh:{userId}:{tokenId}
        alt token không tồn tại hoặc revoked
            IM-->>W: 401 → force re-login
        else valid
            IM->>IM: generate new accessToken (15m)
            IM->>IM: generate new refreshToken (rotate)
            IM->>Cache: DEL refresh:{userId}:{old_tokenId}
            IM->>Cache: SET refresh:{userId}:{new_tokenId} TTL=7d
            IM-->>W: {newAccessToken} + Set-Cookie: newRefreshToken
        end
    end

    rect rgb(255, 241, 242)
        Note over U,Cache: Logout
        W->>IM: POST /auth/logout (cookie: refreshToken)
        IM->>Cache: DEL refresh:{userId}:{tokenId}
        IM-->>W: 200 OK + Set-Cookie: refreshToken='' Max-Age=0
        W->>W: clear accessToken from memory
    end
```

**Notes:**
- **Access token:** JWT, signed HS256, 15 phút. Stored in memory — không bị XSS steal từ localStorage.
- **Refresh token rotation:** Mỗi refresh sinh token mới, revoke token cũ. Phát hiện token reuse = force logout all sessions.
- **httpOnly cookie:** refreshToken không accessible từ JS. Bảo vệ khỏi XSS. SameSite=Strict chống CSRF.

---

## Flow 6: AI Grade Submission (AI Grader)

Chấm bài SUBMISSION/PRACTICE theo rubric — PASS tự hoàn thành task qua event

```mermaid
sequenceDiagram
    actor L as Learner
    participant W as Web App
    participant AIM as AI Module
    participant QM as Quest Module
    participant Claude as Claude API
    participant DB as PostgreSQL
    participant NM as Notification Module

    L->>W: nộp bài + bấm "Chấm bằng AI"
    W->>AIM: POST /api/v1/ai/grade {personalTaskId, evidence}
    activate AIM
    AIM->>QM: internal GET task + rubric (task.config)
    QM-->>AIM: task, rubric
    AIM->>Claude: prompt: chấm theo rubric
    Claude-->>AIM: {status, score, feedback}
    alt JSON invalid / score ngoài 0-100
        AIM-->>W: 422 Validation Error
    else LLM lỗi / timeout
        AIM-->>W: 503 Service Unavailable (không tự PASS)
    else valid
        AIM->>DB: INSERT submission_grades (attempt_no, status, score, feedback, rubric_snapshot)
        AIM->>DB: INSERT outbox_events (event_type='submission.graded', status='PENDING')
        AIM-->>W: 200 {gradeId, status, score, feedback}
        deactivate AIM
        W-->>L: hiện feedback + nút resubmit (nếu FAIL/NEEDS_REVISION)
        Note over DB,NM: Outbox Relay deliver submission.graded
        alt status = PASS
            QM->>DB: INSERT task_completions (personal_task_id) + UPDATE personal_tasks SET is_completed=true
            QM->>DB: evaluate completion_rule (có thể raise quest.completed)
            NM->>L: push "✅ Task đạt yêu cầu — AI chấm {score}"
        else FAIL / NEEDS_REVISION
            NM->>L: push "📝 Task cần chỉnh sửa — xem feedback"
        end
    end
```

**Notes:**
- **AI không tự PASS:** AI chỉ publish kết quả; Quest Module quyết định tạo TaskCompletion. task_completions.personal_task_id UNIQUE chặn duplicate.
- **attempt_no:** mỗi lần nộp = 1 row grade. Resubmit tăng attempt_no, chỉ grade mới nhất có hiệu lực.
- **Rate limit:** 20 req/day per user — chống spam token.

---

## Flow 7: AI Coach Chat (tool calling)

Read-only agent — Claude gọi tool đọc progress thật, streaming reply qua SSE

```mermaid
sequenceDiagram
    actor L as Learner
    participant W as Web App
    participant AIM as AI Module
    participant Claude as Claude API
    participant QM as Quest Module
    participant DB as PostgreSQL

    L->>W: mở AI Coach, gửi tin nhắn
    W->>AIM: POST /api/v1/ai/coach/sessions/:id/messages {content}
    activate AIM
    AIM->>DB: INSERT coach_messages (role=USER)
    AIM->>Claude: conversation + tools (get_progress, get_streak, get_achievements, get_upcoming_tasks)
    loop tool calling loop
        Claude-->>AIM: tool_use (name + args)
        AIM->>QM: internal READ-only query (personal_quests, task_completions, quiz_attempts, achievements)
        QM-->>AIM: progress data
        AIM->>Claude: tool_result
    end
    Claude-->>AIM: streaming reply (SSE)
    AIM-->>W: SSE stream → hiển thị từng token
    AIM->>DB: INSERT coach_messages (role=ASSISTANT) + INSERT coach_messages (role=TOOL, tool_calls)
    deactivate AIM
```

**Notes:**
- **Read-only agent:** mọi tool chỉ query internal API — không có tool nào write quest. User không bao giờ bị AI tự ý đổi progress.
- **Streaming:** FastAPI StreamingResponse (SSE). Message ASSISTANT lưu sau khi stream xong để giữ lịch sử.
- **Rate limit:** 60 messages/day per user. Session tối đa 5/ngày.
