// Package service contains business logic for the notification service.
package service

import (
	"context"
	"fmt"

	"notification/infra/email"
	"notification/infra/push"
	"notification/infra/sse"
	"notification/repository"
	"notification/schemas"
	"notification/util/logger"

	"github.com/google/uuid"
)

const (
	defaultPage  = 1
	defaultLimit = 20
	maxLimit     = 100
)

// emailNotificationTypes are the notification types that also trigger an email.
var emailNotificationTypes = map[schemas.NotificationType]bool{
	schemas.TypeQuestCompleted: true,
	schemas.TypeAchievement:    true,
	schemas.TypeReview:         true,
}

// INotificationService defines notification business operations.
type INotificationService interface {
	// Notify saves a notification and dispatches SSE, FCM push and email side-effects.
	Notify(ctx context.Context, n *schemas.Notification) error
	// Broadcast creates notifications for a list of users (or all users when empty).
	Broadcast(ctx context.Context, req *schemas.BroadcastRequest) error
	ListByUser(ctx context.Context, userID uuid.UUID, page, limit int) (*schemas.ListNotificationsResponse, error)
	MarkRead(ctx context.Context, id uuid.UUID) error
	MarkAllRead(ctx context.Context, userID uuid.UUID) error
	UnreadCount(ctx context.Context, userID uuid.UUID) (*schemas.UnreadCountResponse, error)
}

// NotificationService implements INotificationService.
type NotificationService struct {
	repo        repository.INotificationRepository
	tokenRepo   repository.IDeviceTokenRepository
	emailRepo   repository.IUserEmailRepository
	sseHub      *sse.Hub
	fcmClient   *push.FCMClient
	emailMailer *email.Mailer
}

// NewNotificationService returns a service backed by the given dependencies.
func NewNotificationService(
	repo repository.INotificationRepository,
	tokenRepo repository.IDeviceTokenRepository,
	emailRepo repository.IUserEmailRepository,
	hub *sse.Hub,
	fcm *push.FCMClient,
	mailer *email.Mailer,
) *NotificationService {
	return &NotificationService{
		repo:        repo,
		tokenRepo:   tokenRepo,
		emailRepo:   emailRepo,
		sseHub:      hub,
		fcmClient:   fcm,
		emailMailer: mailer,
	}
}

// Notify saves the notification then fans out to SSE, FCM and email asynchronously.
func (s *NotificationService) Notify(ctx context.Context, n *schemas.Notification) error {
	if err := s.repo.Create(ctx, n); err != nil {
		return err
	}

	// SSE — synchronous, in-memory, negligible cost.
	s.sseHub.Push(n.UserID, *n)

	// FCM and email are I/O-bound; run in background goroutines so the outbox
	// worker transaction is not held open waiting for external services.
	go s.sendPush(n)
	go s.sendEmail(n)

	return nil
}

// sendPush sends an FCM push to every device registered for the user.
func (s *NotificationService) sendPush(n *schemas.Notification) {
	tokens, err := s.tokenRepo.ListByUser(context.Background(), n.UserID)
	if err != nil {
		logger.Log.Error().Err(err).Str("userId", n.UserID.String()).Msg("fcm: list tokens failed")
		return
	}
	body := ""
	if n.Body != nil {
		body = *n.Body
	}
	for _, dt := range tokens {
		if err := s.fcmClient.Send(context.Background(), dt.Token, n.Title, body); err != nil {
			logger.Log.Warn().Err(err).Str("token", dt.Token).Msg("fcm: send failed")
		}
	}
}

// sendEmail delivers an email for notification types that warrant one.
func (s *NotificationService) sendEmail(n *schemas.Notification) {
	if !emailNotificationTypes[n.Type] {
		return
	}
	emailAddr, err := s.emailRepo.FindByUser(context.Background(), n.UserID)
	if err != nil || emailAddr == "" {
		return
	}
	body := ""
	if n.Body != nil {
		body = *n.Body
	}
	html := fmt.Sprintf(
		`<h2>%s</h2><p>%s</p><hr><small>QuestHub — <a href="#">Unsubscribe</a></small>`,
		n.Title, body,
	)
	if err := s.emailMailer.Send(emailAddr, n.Title, html); err != nil {
		logger.Log.Warn().Err(err).Str("to", emailAddr).Msg("email: send failed")
	}
}

// Broadcast creates a notification for each listed user (or returns an error
// when UserIDs is empty — callers should validate first).
func (s *NotificationService) Broadcast(ctx context.Context, req *schemas.BroadcastRequest) error {
	for _, rawID := range req.UserIDs {
		uid, err := uuid.Parse(rawID)
		if err != nil {
			return fmt.Errorf("invalid userId %q: %w", rawID, err)
		}
		body := req.Body
		n := &schemas.Notification{
			ID:     uuid.New(),
			UserID: uid,
			Type:   req.Type,
			Title:  req.Title,
			Body:   &body,
		}
		if err := s.Notify(ctx, n); err != nil {
			logger.Log.Error().Err(err).Str("userId", rawID).Msg("broadcast: notify failed")
		}
	}
	return nil
}

// ListByUser returns the user's inbox with pagination defaults applied.
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
