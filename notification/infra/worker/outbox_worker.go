// Package worker contains background workers for the notification service.
package worker

import (
	"context"
	"encoding/json"
	"time"

	"questhub/notification/util/logger"

	"gorm.io/gorm"
)

// notificationEventTypes lists the outbox event types this service consumes.
// Phase 2: replace polling with RabbitMQ fan-out exchange.
var notificationEventTypes = []string{
	"task.completed",
	"quest.completed",
	"achievement.unlocked",
	"comment.created",
	"discussion.created",
	"user.followed",
	"submission.graded",
}

const (
	outboxBatchSize = 50
	statusProcessed = "PROCESSED"
	statusFailed    = "FAILED"
)

// OutboxEvent is one pending row read from outbox_events.
type OutboxEvent struct {
	ID        string `gorm:"column:id"`
	EventType string `gorm:"column:event_type"`
	Payload   []byte `gorm:"column:payload"`
}

// OutboxWorker polls the Java monolith's transactional outbox and dispatches
// events to the EventHandler.
type OutboxWorker struct {
	db       *gorm.DB
	handler  *EventHandler
	interval time.Duration
}

// NewOutboxWorker returns a worker polling at the given interval.
func NewOutboxWorker(db *gorm.DB, handler *EventHandler, interval time.Duration) *OutboxWorker {
	return &OutboxWorker{db: db, handler: handler, interval: interval}
}

// Run polls the outbox_events table on each tick until ctx is cancelled.
func (w *OutboxWorker) Run(ctx context.Context) {
	ticker := time.NewTicker(w.interval)
	defer ticker.Stop()
	logger.Log.Info().Dur("interval", w.interval).Msg("outbox worker started")

	for {
		select {
		case <-ctx.Done():
			logger.Log.Info().Msg("outbox worker stopped")
			return
		case <-ticker.C:
			if err := w.poll(ctx); err != nil {
				logger.Log.Error().Err(err).Msg("outbox poll error")
			}
		}
	}
}

// poll locks a batch of PENDING rows with FOR UPDATE SKIP LOCKED, processes
// them and updates status in the same transaction.
func (w *OutboxWorker) poll(ctx context.Context) error {
	return w.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		var events []OutboxEvent
		if err := tx.Raw(
			`SELECT id, event_type, payload
			 FROM outbox_events
			 WHERE status = 'PENDING'
			   AND event_type = ANY(?)
			 ORDER BY created_at
			 LIMIT ?
			 FOR UPDATE SKIP LOCKED`,
			notificationEventTypes, outboxBatchSize,
		).Scan(&events).Error; err != nil {
			return err
		}

		for _, e := range events {
			status := statusProcessed
			if err := w.handler.Handle(ctx, OutboxEvent{ID: e.ID, EventType: e.EventType, Payload: json.RawMessage(e.Payload)}); err != nil {
				logger.Log.Error().Str("eventId", e.ID).Str("type", e.EventType).Err(err).Msg("event handling failed")
				status = statusFailed
			}
			if err := tx.Exec(
				`UPDATE outbox_events SET status = ?, processed_at = NOW() WHERE id = ?`,
				status, e.ID,
			).Error; err != nil {
				logger.Log.Error().Str("eventId", e.ID).Err(err).Msg("failed to update outbox status")
			}
		}
		return nil
	})
}
