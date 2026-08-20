CREATE TABLE task_completions
(
    id                UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    personal_task_id  UUID         NOT NULL UNIQUE REFERENCES personal_tasks (id) ON DELETE CASCADE,
    user_id           UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    evidence          JSONB        NOT NULL DEFAULT '{}'::jsonb,
    completed_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_task_completions_user_completed ON task_completions (user_id, completed_at);