CREATE TABLE skill_domains
(
    id          UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL UNIQUE,
    slug        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(1000),
    icon        VARCHAR(50),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE learning_paths
(
    id                 UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    domain_id          UUID         NOT NULL REFERENCES skill_domains (id) ON DELETE RESTRICT,
    author_id          UUID         NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    title              VARCHAR(100) NOT NULL,
    description        VARCHAR(1000),
    difficulty         VARCHAR(20) CHECK ( difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED') ),
    estimated_duration INT          NOT NULL DEFAULT 0,
    is_public          BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_lp_creator ON learning_paths (author_id);
CREATE INDEX idx_lp_domain ON learning_paths (domain_id);