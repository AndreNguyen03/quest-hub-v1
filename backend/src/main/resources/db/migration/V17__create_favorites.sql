CREATE TABLE favorites
(
    user_id    UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    quest_id   UUID         NOT NULL REFERENCES quests (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_favorites PRIMARY KEY (user_id, quest_id)
);

CREATE INDEX idx_favorites_user_id ON favorites (user_id);
CREATE INDEX idx_favorites_quest_id ON favorites (quest_id);
