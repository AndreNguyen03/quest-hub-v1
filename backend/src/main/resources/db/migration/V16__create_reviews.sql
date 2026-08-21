CREATE TABLE reviews
(
    id          UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    quest_id    UUID         NOT NULL REFERENCES quests (id) ON DELETE CASCADE,
    user_id     UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    score       SMALLINT     NOT NULL CHECK ( score >= 1 AND score <= 5 ),
    content     TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_reviews_quest_user UNIQUE (quest_id, user_id)
);

CREATE INDEX idx_reviews_quest_id ON reviews (quest_id);
CREATE INDEX idx_reviews_user_id ON reviews (user_id);
