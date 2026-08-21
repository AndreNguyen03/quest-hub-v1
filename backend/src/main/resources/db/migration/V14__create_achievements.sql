CREATE TABLE achievements (
    id          UUID PRIMARY KEY,
    code        VARCHAR(50) NOT NULL UNIQUE,
    title       VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,
    criteria    JSONB NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE user_achievements (
    user_id        UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    achievement_id UUID NOT NULL REFERENCES achievements(id) ON DELETE CASCADE,
    unlocked_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, achievement_id)
);

CREATE INDEX idx_user_achievements_user ON user_achievements(user_id);

-- Seed 6 achievements
INSERT INTO achievements (id, code, title, description, criteria) VALUES
  (gen_random_uuid(), 'FIRST_QUEST', 'First Quest Completed', 'Hoàn thành quest đầu tiên', '{"type":"QUEST_COUNT","threshold":1}'),
  (gen_random_uuid(), 'FIVE_QUESTS', 'Quest Collector', 'Hoàn thành 5 quests', '{"type":"QUEST_COUNT","threshold":5}'),
  (gen_random_uuid(), 'TEN_TASKS', 'Task Master', 'Hoàn thành 10 tasks', '{"type":"TASK_COUNT","threshold":10}'),
  (gen_random_uuid(), 'TWENTY_TASKS', 'Dedicated Learner', 'Hoàn thành 20 tasks', '{"type":"TASK_COUNT","threshold":20}'),
  (gen_random_uuid(), 'DOMAIN_FIVE', 'Domain Explorer', 'Hoàn thành 5 tasks trong cùng 1 domain', '{"type":"DOMAIN_TASK_COUNT","threshold":5}'),
  (gen_random_uuid(), 'DOMAIN_TEN', 'Domain Master', 'Hoàn thành 10 tasks trong cùng 1 domain', '{"type":"DOMAIN_TASK_COUNT","threshold":10}');
