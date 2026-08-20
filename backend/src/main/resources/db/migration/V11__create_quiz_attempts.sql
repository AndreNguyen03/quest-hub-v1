CREATE TABLE quiz_attempts
(
    id                UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    personal_task_id  UUID         NOT NULL REFERENCES personal_tasks (id) ON DELETE CASCADE,
    user_id           UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    score             NUMERIC(5,2) NOT NULL,
    max_score         NUMERIC(5,2) NOT NULL,
    passed            BOOLEAN      NOT NULL DEFAULT FALSE,
    answers           JSONB        NOT NULL DEFAULT '{}'::jsonb,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_quiz_attempts_task_created ON quiz_attempts (personal_task_id, created_at DESC);
CREATE INDEX idx_quiz_attempts_user_created ON quiz_attempts (user_id, created_at DESC);