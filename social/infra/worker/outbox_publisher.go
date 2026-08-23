// Package worker contains the outbox consumer and the outbox event publisher
// for the social service.
package worker

import (
	"context"
	"encoding/json"
	"fmt"
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

// outboxEvent mirrors one row in the shared outbox_events table.
type outboxEvent struct {
	ID            string    `gorm:"column:id"`
	AggregateType string    `gorm:"column:aggregate_type"`
	AggregateID   string    `gorm:"column:aggregate_id"`
	EventType     string    `gorm:"column:event_type"`
	Payload       []byte    `gorm:"column:payload"`
	Status        string    `gorm:"column:status"`
	CreatedAt     time.Time `gorm:"column:created_at"`
}

func (outboxEvent) TableName() string { return "outbox_events" }

// OutboxPublisher writes events into the shared outbox_events table so
// downstream services (notification, identity) can consume them.
type OutboxPublisher struct{ db *gorm.DB }

// NewOutboxPublisher returns a publisher backed by the given DB handle.
func NewOutboxPublisher(db *gorm.DB) *OutboxPublisher { return &OutboxPublisher{db: db} }

// Publish inserts a PENDING event row into outbox_events.
func (p *OutboxPublisher) Publish(ctx context.Context, eventType string, payload map[string]any) error {
	b, err := json.Marshal(payload)
	if err != nil {
		return fmt.Errorf("marshal outbox payload: %w", err)
	}
	e := outboxEvent{
		ID:            uuid.New().String(),
		AggregateType: "social",
		AggregateID:   uuid.New().String(),
		EventType:     eventType,
		Payload:       b,
		Status:        "PENDING",
	}
	return p.db.WithContext(ctx).Table("outbox_events").Create(&e).Error
}
