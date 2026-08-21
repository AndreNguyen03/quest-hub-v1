ALTER TABLE skill_domains ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;

CREATE TABLE feature_flags
(
    key         VARCHAR(100) PRIMARY KEY,
    value       JSONB        NOT NULL DEFAULT '{}'::jsonb,
    description TEXT,
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
