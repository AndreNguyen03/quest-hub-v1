CREATE TABLE districts
(
    id               UUID PRIMARY KEY,
    world_id         UUID NOT NULL REFERENCES worlds (id) ON DELETE CASCADE,
    domain_id        UUID NOT NULL REFERENCES skill_domains (id) ON DELETE CASCADE,
    completion_count INT          NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (world_id, domain_id)
);

CREATE INDEX idx_districts_world ON districts (world_id);

CREATE TABLE district_events
(
    event_id    UUID PRIMARY KEY,
    district_id UUID         NOT NULL REFERENCES districts (id) ON DELETE CASCADE,
    delta       INT          NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);