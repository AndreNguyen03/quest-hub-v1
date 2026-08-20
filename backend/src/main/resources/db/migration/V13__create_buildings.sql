CREATE TABLE buildings
(
    id          UUID PRIMARY KEY,
    district_id UUID        NOT NULL REFERENCES districts (id) ON DELETE CASCADE,
    type        VARCHAR(50) NOT NULL,
    unlocked_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    position    INT         NOT NULL,
    UNIQUE (district_id, position)
);

CREATE INDEX idx_buildings_district ON buildings (district_id);