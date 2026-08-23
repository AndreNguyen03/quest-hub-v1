// Package repository contains data access implementations for the
// notification service.
package repository

import (
	"context"

	"notification/schemas"
	"notification/util/jsonb"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

// INotificationRepository defines data access for notifications.
type INotificationRepository interface {
	Create(ctx context.Context, n *schemas.Notification) error
	ListByUser(ctx context.Context, userID uuid.UUID, limit, offset int) ([]schemas.Notification, error)
	MarkRead(ctx context.Context, id uuid.UUID) error
	MarkAllRead(ctx context.Context, userID uuid.UUID) error
	CountUnread(ctx context.Context, userID uuid.UUID) (int, error)
}

// NotificationRepository implements INotificationRepository on PostgreSQL via GORM.
type NotificationRepository struct {
	db *gorm.DB
}

// NewNotificationRepository returns a repository bound to the given DB handle.
func NewNotificationRepository(db *gorm.DB) *NotificationRepository {
	return &NotificationRepository{db: db}
}

// Create inserts a new notification row.
func (r *NotificationRepository) Create(ctx context.Context, n *schemas.Notification) error {
	if n.Payload == nil {
		n.Payload = jsonb.JSONB{}
	}
	return r.db.WithContext(ctx).Create(n).Error
}

// ListByUser returns the user's inbox, newest first.
func (r *NotificationRepository) ListByUser(ctx context.Context, userID uuid.UUID, limit, offset int) ([]schemas.Notification, error) {
	var results []schemas.Notification
	err := r.db.WithContext(ctx).
		Where("user_id = ?", userID).
		Order("created_at DESC").
		Limit(limit).
		Offset(offset).
		Find(&results).Error
	return results, err
}

// MarkRead flags a single notification as read.
func (r *NotificationRepository) MarkRead(ctx context.Context, id uuid.UUID) error {
	return r.db.WithContext(ctx).
		Model(&schemas.Notification{}).
		Where("id = ?", id).
		Update("is_read", true).Error
}

// MarkAllRead flags every unread notification of the user as read.
func (r *NotificationRepository) MarkAllRead(ctx context.Context, userID uuid.UUID) error {
	return r.db.WithContext(ctx).
		Model(&schemas.Notification{}).
		Where("user_id = ? AND is_read = ?", userID, false).
		Update("is_read", true).Error
}

// CountUnread returns how many unread notifications the user has.
func (r *NotificationRepository) CountUnread(ctx context.Context, userID uuid.UUID) (int, error) {
	var count int64
	err := r.db.WithContext(ctx).
		Model(&schemas.Notification{}).
		Where("user_id = ? AND is_read = ?", userID, false).
		Count(&count).Error
	return int(count), err
}
