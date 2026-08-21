# QuestHub — Roadmap 12 Tháng

Sprint planning · kết hợp đọc DDIA + CTCI · cập nhật theo từng tháng · tháng 1 chi tiết

---

## Giả định & Nhịp độ

### Hồ sơ người học

- Đã quen Spring Boot / JPA — không cần học Java lại
- ~10h/tuần: sáng sớm trước giờ làm + chủ nhật
- Thứ tự frontend: **Web → React Native → Admin page → Landing page**

### Nhịp làm việc

- **2 tuần = 1 sprint**, 1 tháng = 2 sprints
- Đọc sách **không bao giờ cắt** dù bị trễ việc
- Bị trễ tháng nào → chỉ làm "Output chính", lùi stretch goals
- Mỗi sprint có Definition of Done (điều kiện hoàn thành)

---

## Nguyên tắc thứ tự

- **Backend lõi trước, frontend sau** — web cần API ổn định
- **Module phụ thuộc nhau làm trước**: Identity → Quest → Marketplace / World (đọc dữ liệu Quest) → Social / AI / Admin (supporting)
- **Đọc sách song song**: chương DDIA nào, sprint đó áp dụng ngay (học bằng làm)
- **Hạ tầng lồng vào từng tháng**: CI từ Sprint 1, K8s / observability để cuối Phase 1

---

## Roadmap 12 tháng

| Tháng | Dev focus | DDIA | CTCI | Output chính |
|-------|-----------|------|------|--------------|
| **M1** (now) | Skeleton monolith DDD + Identity + Quest CRUD | Ch 1–2 | Ch 1 | Đăng ký/login + tạo & publish quest |
| M2 | Fork, task completion, quiz, CompletionRule engine + Outbox pattern | Ch 3–4 | Ch 2–3 | Track tiến độ thật, sự kiện async |
| M3 | World (district/building/achievement) + Marketplace browse | Ch 5–6 | Ch 3–4 | World + duyệt quest |
| M4 | Marketplace search (Elasticsearch) + review + favorite + analytics | Ch 7 | Ch 4–5 | Marketplace hoàn chỉnh |
| M5 | Social (Go) + Notification (Go) + RabbitMQ | Ch 8–9 | Ch 5–7 | Event-driven thật, feed/comment |
| M6 | Web Next.js: auth, marketplace, quest detail, tracker | Ch 10 | Ch 7–8 | MVP web chạy được |
| M7 | Web: world, profile, social, review + deploy staging | Ch 11 | Ch 8–9 | Web đầy đủ trên staging |
| M8 | AI service (Python): gợi ý, sinh quest, chấm bài (grader), coach (tool calling) + Admin backend | Ch 12 | Ch 9–10 | Gợi ý quest + chấm bài AI + AI coach + admin backend |
| M9 | Phase 2: tách microservices, API Gateway Go, idempotent consumers | Ôn tập | Problems | Kiến trúc tách |
| M10 | K8s (k3s) + DevSecOps pipeline + Observability (OTel/Grafana) | Ôn tập | Problems | Prod-grade infra |
| M11 | React Native app (tracking-first) | — | Problems + mock | App mobile |
| M12 | Admin page + landing page + polish + deploy prod | — | Tổng ôn | Full launch |

---

## Tháng 1 — Chi tiết

### Mục tiêu tháng

Monolith skeleton chạy Docker, Identity (US-20, US-21), Quest tạo được (US-01, 02, 03, 04, 10). Kết quả: đăng ký → tạo path → tạo quest kèm chapters/tasks → config completion rule → publish.

> **Đọc sách:** DDIA Ch 1–4 · CTCI Ch 1 (Arrays & Strings) + Ch 2 (Linked Lists)

---

### Sprint 1 · Tuần 1–2 · Nền móng + Identity

| Ngày | Công việc |
|------|-----------|
| W1 · D1-2 | Git + repo, Spring Boot 3 + Java 21, package DDD (`identity/quest/marketplace/world/admin/...`), Docker Compose (postgres:16) |
| W1 · D3 | Flyway migration `001_users`, JPA User entity + repository |
| W1 · D4-5 | US-20 register (email + bcrypt), validation, GitHub Actions build+test |
| W2 · D1-2 | JWT access + refresh token (rotation), Spring Security, method-level security |
| W2 · D3 | World tự tạo khi đăng ký (AC US-20) |
| W2 · D4-5 | US-21 profile update (avatar/bio/social links, public/private), test identity, sprint review |

> **Reading:** DDIA Ch 1 (reliable/scalable/maintainable) → áp dụng: thiết kế retry, error handling. DDIA Ch 2 (data models) → áp dụng: JSONB vs relation cho profile/quest. CTCI Ch 1 lý thuyết + giải vài bài arrays/strings.

---

### Sprint 2 · Tuần 3–4 · Quest CRUD

| Ngày | Công việc |
|------|-----------|
| W3 · D1-2 | Domain model Quest/Chapter/Task/Resource/CompletionRule (VO, mặc định `ALL_TASKS`) + Flyway migration |
| W3 · D3 | US-01 create path (title/description/difficulty, thuộc domain, private mặc định) |
| W3 · D4-5 | US-02 create quest (gắn path hoặc độc lập, ≥1 chapter ≥1 task, DRAFT) |
| W4 · D1-2 | US-03 chapters/tasks/resources + reorder |
| W4 · D3 | US-10 publish/unpublish (DRAFT→PUBLIC) |
| W4 · D4 | US-04 completion rule config (default ALL_TASKS, QUIZ_SCORE/SUBMISSION/ALL_OF/ANY_OF) |
| W4 · D5 | Sprint review: e2e "register → create path → create quest → publish" + tests xanh |

> **Reading:** DDIA Ch 3 (storage & retrieval) → áp dụng: index cho query quest list. DDIA Ch 4 (encoding & evolution) → áp dụng: schema versioning, event payload. CTCI Ch 2 lý thuyết + luyện.

---

### Definition of Done

- **Sprint 1:** register/login/refresh token hoạt động qua API, có test, CI build pass
- **Sprint 2:** tạo được quest 3 cấp (path/quest/chapter/task), publish được, quest xuất hiện trong danh sách public

> **Defer cho tháng 1:** OAuth (chỉ email/password), Elasticsearch, mọi thứ supporting (Social/AI/Admin). Tập trung 100% vào Identity + Quest CRUD.

---

## Điểm cần quyết định (sẽ hỏi khi tới tháng)

- **Tháng 5:** Social/Notification là Go app riêng nhưng Phase 1 chưa có RabbitMQ → relay sẽ HTTP-push hay kéo qua polling?
- **Tháng 9:** có cần tách microservices sớm hay giữ monolith lâu hơn?
- **Mọi tháng:** pacing thực tế — cập nhật lại roadmap file này mỗi đầu tháng
