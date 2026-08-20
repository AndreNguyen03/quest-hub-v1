CREATE TABLE quests
(
    id                 UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    learning_path_id   UUID         REFERENCES learning_paths (id) ON DELETE SET NULL,
    creator_id         UUID         NOT NULL REFERENCES users (id) ON DELETE SET NULL,
    title              VARCHAR(100) NOT NULL,
    description        VARCHAR(1000),
    difficulty         VARCHAR(20) CHECK ( difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED') ),
    estimated_duration INT          NOT NULL DEFAULT 0,
    completion_rule    JSONB        NOT NULL DEFAULT '{
      "type": "ALL_TASKS"
    }'::jsonb,
    reward             JSONB        NOT NULL DEFAULT '{}'::jsonb,
    visibility         VARCHAR(10)  NOT NULL DEFAULT 'DRAFT' CHECK ( visibility IN ('DRAFT', 'PUBLIC', 'HIDDEN') ),
    fork_count         INT          NOT NULL DEFAULT 0,
    avg_rating         NUMERIC(3, 2),
    rating_count       INT          NOT NULL DEFAULT 0,
    published_at       TIMESTAMPTZ,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE chapters
(
    id          UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    quest_id    UUID         NOT NULL REFERENCES quests (id) ON DELETE CASCADE,
    title       VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    position    INT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE tasks
(
    id          UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    chapter_id  UUID         NOT NULL REFERENCES chapters (id) ON DELETE CASCADE,
    type        VARCHAR(20)  NOT NULL CHECK ( type IN ('LEARN', 'QUIZ', 'PRACTICE', 'SUBMISSION', 'REFLECTION')),
    title       VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    "order"     INT          NOT NULL DEFAULT 0,
    config      JSONB        NOT NULL DEFAULT '{}'::jsonb,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_quests_visibility_published ON quests (visibility, published_at);
CREATE INDEX idx_chapters_quest_position ON chapters (quest_id, position);
CREATE INDEX idx_tasks_chapter_order ON tasks (chapter_id, "order");
