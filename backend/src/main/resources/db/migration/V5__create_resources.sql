CREATE TABLE resources
(
    id                UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    task_id           UUID         NOT NULL REFERENCES tasks (id) ON DELETE CASCADE,
    type              VARCHAR(20)  NOT NULL CHECK ( type IN
                                                    ('VIDEO', 'ARTICLE', 'BOOK', 'DOCUMENT', 'COURSE', 'PODCAST',
                                                     'FILE', 'LINK') ),
    title             VARCHAR(200) NOT NULL,
    url               VARCHAR(200) NOT NULL,
    estimated_minutes INT,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_resources_task ON resources (task_id);