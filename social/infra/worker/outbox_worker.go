package worker

import (
	"context"
	"encoding/json"
	"time"

	"social/util/logger"

	"gorm.io/gorm"
)

// socialEventTypes lists outbox event types this service consumes from the
// Java monolith to build the activity feed.
var socialEventTypes = []string{
	"quest.published",
	"quest.forked",
	"quest.completed",
	"task.completed",
	"achievement.unlocked",
}

const (
	batchSize       = 50
	statusProcessed = "PROCESSED"
	statusFailed    = "FAILED"
)

// OutboxEvent is one pending row read from outbox_events.
type OutboxEvent struct {
	ID        string          `gorm:"column:id"`
	EventType string          `gorm:"column:event_type"`
	Payload   json.RawMessage `gorm:"column:payload"`
}

// OutboxWorker polls the Java monolith's transactional outbox for activity events.
type OutboxWorker struct {
	db       *gorm.DB
	handler  *EventHandler
	interval time.Duration
}

func NewOutboxWorker(db *gorm.DB, handler *EventHandler, interval time.Duration) *OutboxWorker {
	return &OutboxWorker{db: db, handler: handler, interval: interval}
}

// Run polls on each tick until ctx is cancelled.
func (w *OutboxWorker) Run(ctx context.Context) {
	ticker := time.NewTicker(w.interval)
	defer ticker.Stop()
	logger.Log.Info().Dur("interval", w.interval).Msg("social outbox worker started")

	for {
		select {
		case <-ctx.Done():
			logger.Log.Info().Msg("social outbox worker stopped")
			return
		case <-ticker.C:
			if err := w.poll(ctx); err != nil {
				logger.Log.Error().Err(err).Msg("social outbox poll error")
			}
		}
	}
}

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
			socialEventTypes, batchSize,
		).Scan(&events).Error; err != nil {
			return err
		}

		for _, e := range events {
			status := statusProcessed
			if err := w.handler.Handle(ctx, e); err != nil {
				logger.Log.Error().Str("eventId", e.ID).Str("type", e.EventType).Err(err).Msg("social event handling failed")
				status = statusFailed
			}
			tx.Exec(`UPDATE outbox_events SET status = ?, processed_at = NOW() WHERE id = ?`, status, e.ID)
		}
		return nil
	})
}
