CREATE TABLE personal_quests
(
    id                UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    user_id           UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    quest_id          UUID         REFERENCES quests (id) ON DELETE SET NULL,
    learning_path_id  UUID         REFERENCES learning_paths (id) ON DELETE SET NULL,
    title             VARCHAR(100) NOT NULL,
    completion_rule   JSONB        NOT NULL,
    status            VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE' CHECK ( status IN ('ACTIVE', 'COMPLETED', 'ABANDONED') ),
    progress          INT          NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    completed_at      TIMESTAMPTZ
);

CREATE TABLE personal_chapters
(
    id                UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    personal_quest_id UUID         NOT NULL REFERENCES personal_quests (id) ON DELETE CASCADE,
    source_chapter_id UUID         REFERENCES chapters (id) ON DELETE SET NULL,
    title             VARCHAR(100) NOT NULL,
    description       VARCHAR(1000),
    position          INT          NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE personal_tasks
(
    id                   UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    personal_chapter_id  UUID         NOT NULL REFERENCES personal_chapters (id) ON DELETE CASCADE,
    source_task_id       UUID         REFERENCES tasks (id) ON DELETE SET NULL,
    type                 VARCHAR(20)  NOT NULL CHECK ( type IN ('LEARN', 'QUIZ', 'PRACTICE', 'SUBMISSION', 'REFLECTION')),
    title                VARCHAR(100) NOT NULL,
    description          VARCHAR(1000),
    "order"              INT          NOT NULL DEFAULT 0,
    config               JSONB        NOT NULL DEFAULT '{}'::jsonb,
    is_completed         BOOLEAN      NOT NULL DEFAULT FALSE,
    completed_at         TIMESTAMPTZ,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uq_personal_quests_user_quest ON personal_quests (user_id, quest_id) WHERE quest_id IS NOT NULL;
CREATE INDEX idx_personal_quests_user_status ON personal_quests (user_id, status);
CREATE INDEX idx_personal_chapters_pq_position ON personal_chapters (personal_quest_id, position);
CREATE INDEX idx_personal_tasks_pc_order ON personal_tasks (personal_chapter_id, "order");