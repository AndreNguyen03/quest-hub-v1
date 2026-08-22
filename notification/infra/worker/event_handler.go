package worker

import (
	"context"
	"encoding/json"
	"fmt"
	"questhub/notification/util/logger"

	"questhub/notification/repository"
	"questhub/notification/schemas"

	"github.com/google/uuid"
)

// EventHandler converts outbox events into notification rows via the
// notification repository.
type EventHandler struct {
	repo repository.INotificationRepository
}

// NewEventHandler returns a handler backed by the given repository interface.
func NewEventHandler(repo repository.INotificationRepository) *EventHandler {
	return &EventHandler{repo: repo}
}

// Handle dispatches a single outbox event to its typed handler.
func (h *EventHandler) Handle(ctx context.Context, event OutboxEvent) error {
	switch event.EventType {
	case "task.completed":
		return h.handleTaskCompleted(ctx, event.Payload)
	case "quest.completed":
		return h.handleQuestCompleted(ctx, event.Payload)
	case "achievement.unlocked":
		return h.handleAchievementUnlocked(ctx, event.Payload)
	case "comment.created":
		return h.handleCommentCreated(ctx, event.Payload)
	case "discussion.created":
		return h.handleDiscussionCreated(ctx, event.Payload)
	case "user.followed":
		return h.handleUserFollowed(ctx, event.Payload)
	case "submission.graded":
		return h.handleSubmissionGraded(ctx, event.Payload)
	default:
		return fmt.Errorf("unknown event type: %s", event.EventType)
	}
}

// handleTaskCompleted creates TASK_COMPLETED (and optionally QUEST_COMPLETED
// when the payload's isQuestCompleted flag is set).
func (h *EventHandler) handleTaskCompleted(ctx context.Context, raw json.RawMessage) error {
	var p struct {
		UserID           string `json:"userId"`
		TaskTitle        string `json:"taskTitle"`
		QuestTitle       string `json:"questTitle"`
		QuestID          string `json:"questId"`
		IsQuestCompleted bool   `json:"isQuestCompleted"`
	}
	if err := json.Unmarshal(raw, &p); err != nil {
		return err
	}
	userID, err := uuid.Parse(p.UserID)
	if err != nil {
		return fmt.Errorf("parse userId: %w", err)
	}

	if err := h.repo.Create(ctx, &schemas.Notification{
		ID:      uuid.New(),
		UserID:  userID,
		Type:    schemas.TypeTaskCompleted,
		Title:   fmt.Sprintf("🎯 Completed: %s", p.TaskTitle),
		Payload: map[string]any{"questId": p.QuestID},
	}); err != nil {
		return err
	}

	// task.completed carries isQuestCompleted flag — send a second notification
	if p.IsQuestCompleted {
		return h.repo.Create(ctx, &schemas.Notification{
			ID:      uuid.New(),
			UserID:  userID,
			Type:    schemas.TypeQuestCompleted,
			Title:   fmt.Sprintf("🏆 Quest completed: %s", p.QuestTitle),
			Payload: map[string]any{"questId": p.QuestID},
		})
	}
	return nil
}

// handleQuestCompleted creates a QUEST_COMPLETED notification.
func (h *EventHandler) handleQuestCompleted(ctx context.Context, raw json.RawMessage) error {
	var p struct {
		UserID     string `json:"userId"`
		QuestTitle string `json:"questTitle"`
		QuestID    string `json:"questId"`
	}
	if err := json.Unmarshal(raw, &p); err != nil {
		return err
	}
	userID, err := uuid.Parse(p.UserID)
	if err != nil {
		return fmt.Errorf("parse userId: %w", err)
	}
	return h.repo.Create(ctx, &schemas.Notification{
		ID:      uuid.New(),
		UserID:  userID,
		Type:    schemas.TypeQuestCompleted,
		Title:   fmt.Sprintf("🏆 Quest completed: %s", p.QuestTitle),
		Payload: map[string]any{"questId": p.QuestID},
	})
}

// handleAchievementUnlocked creates an ACHIEVEMENT notification.
func (h *EventHandler) handleAchievementUnlocked(ctx context.Context, raw json.RawMessage) error {
	var p struct {
		UserID        string `json:"userId"`
		Title         string `json:"title"`
		AchievementID string `json:"achievementId"`
	}
	if err := json.Unmarshal(raw, &p); err != nil {
		return err
	}
	userID, err := uuid.Parse(p.UserID)
	if err != nil {
		return fmt.Errorf("parse userId: %w", err)
	}
	return h.repo.Create(ctx, &schemas.Notification{
		ID:      uuid.New(),
		UserID:  userID,
		Type:    schemas.TypeAchievement,
		Title:   fmt.Sprintf("🏅 %s", p.Title),
		Payload: map[string]any{"achievementId": p.AchievementID},
	})
}

// handleCommentCreated creates a COMMENT notification for the quest owner.
// recipientUserId must be enriched by Java monolith before publishing to outbox.
// Phase 2: derive recipient from RabbitMQ routing.
func (h *EventHandler) handleCommentCreated(ctx context.Context, raw json.RawMessage) error {
	var p struct {
		RecipientUserID string `json:"recipientUserId"`
		AuthorUsername  string `json:"authorUsername"`
		QuestID         string `json:"questId"`
		CommentID       string `json:"commentId"`
	}
	if err := json.Unmarshal(raw, &p); err != nil {
		return err
	}
	if p.RecipientUserID == "" {
		logger.Log.Warn().Msg("comment.created missing recipientUserId, skipping")
		return nil
	}
	recipientID, err := uuid.Parse(p.RecipientUserID)
	if err != nil {
		return fmt.Errorf("parse recipientUserId: %w", err)
	}
	return h.repo.Create(ctx, &schemas.Notification{
		ID:      uuid.New(),
		UserID:  recipientID,
		Type:    schemas.TypeComment,
		Title:   fmt.Sprintf("💬 %s commented on your quest", p.AuthorUsername),
		Payload: map[string]any{"questId": p.QuestID, "commentId": p.CommentID},
	})
}

// handleDiscussionCreated creates a COMMENT notification when someone opens a
// discussion on your quest. recipientUserId = quest creator, enriched by the
// Java monolith.
func (h *EventHandler) handleDiscussionCreated(ctx context.Context, raw json.RawMessage) error {
	var p struct {
		RecipientUserID string `json:"recipientUserId"`
		AuthorUsername  string `json:"authorUsername"`
		QuestID         string `json:"questId"`
		DiscussionID    string `json:"discussionId"`
		Title           string `json:"title"`
	}
	if err := json.Unmarshal(raw, &p); err != nil {
		return err
	}
	if p.RecipientUserID == "" {
		logger.Log.Warn().Msg("discussion.created missing recipientUserId, skipping")
		return nil
	}
	recipientID, err := uuid.Parse(p.RecipientUserID)
	if err != nil {
		return fmt.Errorf("parse recipientUserId: %w", err)
	}
	return h.repo.Create(ctx, &schemas.Notification{
		ID:      uuid.New(),
		UserID:  recipientID,
		Type:    schemas.TypeComment,
		Title:   fmt.Sprintf("💬 %s started a discussion on your quest", p.AuthorUsername),
		Payload: map[string]any{"questId": p.QuestID, "discussionId": p.DiscussionID},
	})
}

// handleUserFollowed creates a FOLLOWED notification for the followed user.
func (h *EventHandler) handleUserFollowed(ctx context.Context, raw json.RawMessage) error {
	var p struct {
		FollowerUsername string `json:"followerUsername"`
		FollowedUserID   string `json:"followedUserId"`
	}
	if err := json.Unmarshal(raw, &p); err != nil {
		return err
	}
	recipientID, err := uuid.Parse(p.FollowedUserID)
	if err != nil {
		return fmt.Errorf("parse followedUserId: %w", err)
	}
	return h.repo.Create(ctx, &schemas.Notification{
		ID:      uuid.New(),
		UserID:  recipientID,
		Type:    schemas.TypeFollowed,
		Title:   fmt.Sprintf("👋 %s followed you", p.FollowerUsername),
		Payload: map[string]any{},
	})
}

// handleSubmissionGraded creates a REVIEW notification from AI grading results.
func (h *EventHandler) handleSubmissionGraded(ctx context.Context, raw json.RawMessage) error {
	var p struct {
		UserID  string  `json:"userId"`
		QuestID string  `json:"questId"`
		Status  string  `json:"status"`
		Score   float64 `json:"score"`
	}
	if err := json.Unmarshal(raw, &p); err != nil {
		return err
	}
	userID, err := uuid.Parse(p.UserID)
	if err != nil {
		return fmt.Errorf("parse userId: %w", err)
	}

	var title string
	if p.Status == "PASS" {
		title = fmt.Sprintf("✅ Submission passed — AI score: %.0f", p.Score)
	} else {
		title = "📝 Submission needs revision — check feedback"
	}

	return h.repo.Create(ctx, &schemas.Notification{
		ID:      uuid.New(),
		UserID:  userID,
		Type:    schemas.TypeReview,
		Title:   title,
		Payload: map[string]any{"questId": p.QuestID, "status": p.Status, "score": p.Score},
	})
}
