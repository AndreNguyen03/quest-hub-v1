CREATE TABLE outbox_events
(
    id             UUID PRIMARY KEY,
    aggregate_type TEXT        NOT NULL,
    aggregate_id   UUID        NOT NULL,
    event_type     TEXT        NOT NULL,
    payload        JSONB       NOT NULL DEFAULT '{}'::jsonb,
    status         TEXT        NOT NULL DEFAULT 'PENDING' CHECK ( status IN ('PENDING', 'PROCESSING', 'PROCESSED', 'FAILED') ),
    retry_count    INT         NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    processed_at   TIMESTAMPTZ
);

CREATE INDEX idx_outbox_events_pending ON outbox_events (status, created_at) WHERE status = 'PENDING';