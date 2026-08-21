# Memory — Đọc mỗi khi code

> **BẮT BUỘC 1:** Trước khi viết/sửa bất kỳ file nào trong `backend/src/main/java`, phải đọc `docs/ddd-convention.md` và tuân thủ 10 convention DDD trong đó.

> **BẮT BUỘC 2:** Trước khi ra bất kỳ quyết định kiến trúc/code nào (tạo BC mới, đổi API, tách module, chọn DB/query), phải đọc docs liên quan trước: `docs/high-level-design.html` (vị trí service), `docs/api-design.html` (contract API), `docs/service-ownership.html`, `docs/database-schema.html`, `docs/event-contracts.html`, `docs/modules-user-stories.html`. Không tự suy đoán, phải đối chiếu docs.

- Bounded Context thực tế theo `high-level-design.html:436`: Java monolith `identity, quest, marketplace, world, admin` (5) + Side Apps `social, notification (Go), ai-service (Python)` — không tự tạo BC Java cho `notification/challenge` nếu high-level-design đã đặt ở Go/side.
- Trong mỗi context: `domain/model|repository|event|service`, `application/usecase|command|query|dto`, `infrastructure/persistence|messaging`, `presentation/rest`
- `request` đã refactor thành `command` (ví dụ `CreateQuestCommand`) và `query` (`GetQuestQuery`) — không dùng `*Request` trong `application`
- SQL chỉ ở `SpringData*Repository` với `@Query`, `application` chỉ gọi repo, `@Transactional` đủ 4 params, dùng `import` không `java.util.*` inline

Xem chi tiết: `docs/ddd-convention.md` + `docs/high-level-design.html`
