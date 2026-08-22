// Package service contains business logic for the notification service.
package service

import (
	"context"

	"questhub/notification/repository"
	"questhub/notification/schemas"

	"github.com/google/uuid"
)

const (
	defaultPage  = 1
	defaultLimit = 20
	maxLimit     = 100
)

// INotificationService defines notification business operations.
type INotificationService interface {
	ListByUser(ctx context.Context, userID uuid.UUID, page, limit int) (*schemas.ListNotificationsResponse, error)
	MarkRead(ctx context.Context, id uuid.UUID) error
	MarkAllRead(ctx context.Context, userID uuid.UUID) error
	UnreadCount(ctx context.Context, userID uuid.UUID) (*schemas.UnreadCountResponse, error)
}

// NotificationService implements INotificationService.
type NotificationService struct {
	repo repository.INotificationRepository
}

// NewNotificationService returns a service backed by the given repository.
func NewNotificationService(repo repository.INotificationRepository) *NotificationService {
	return &NotificationService{repo: repo}
}

// ListByUser returns the user's inbox with pagination defaults applied:
// page >= 1 and 1 <= limit <= 100 (default 20).
func (s *NotificationService) ListByUser(ctx context.Context, userID uuid.UUID, page, limit int) (*schemas.ListNotificationsResponse, error) {
	if page < defaultPage {
		page = defaultPage
	}
	if limit <= 0 {
		limit = defaultLimit
	}
	if limit > maxLimit {
		limit = maxLimit
	}

	items, err := s.repo.ListByUser(ctx, userID, limit, (page-1)*limit)
	if err != nil {
		return nil, err
	}
	return &schemas.ListNotificationsResponse{Data: items, Page: page, Limit: limit}, nil
}

// MarkRead flags a single notification as read.
func (s *NotificationService) MarkRead(ctx context.Context, id uuid.UUID) error {
	return s.repo.MarkRead(ctx, id)
}

// MarkAllRead flags every unread notification of the user as read.
func (s *NotificationService) MarkAllRead(ctx context.Context, userID uuid.UUID) error {
	return s.repo.MarkAllRead(ctx, userID)
}

// UnreadCount returns how many unread notifications the user has.
func (s *NotificationService) UnreadCount(ctx context.Context, userID uuid.UUID) (*schemas.UnreadCountResponse, error) {
	count, err := s.repo.CountUnread(ctx, userID)
	if err != nil {
		return nil, err
	}
	return &schemas.UnreadCountResponse{Count: count}, nil
}
