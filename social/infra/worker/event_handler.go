package worker

import (
	"context"
	"encoding/json"
	"fmt"

	"questhub/social/schemas"
	"questhub/social/service"
	"questhub/social/util/jsonb"

	"github.com/google/uuid"
)

// EventHandler converts outbox events into activity rows via the activity service.
type EventHandler struct {
	actSvc service.IActivityService
}

func NewEventHandler(actSvc service.IActivityService) *EventHandler {
	return &EventHandler{actSvc: actSvc}
}

// Handle dispatches a single outbox event to its typed handler.
func (h *EventHandler) Handle(ctx context.Context, event OutboxEvent) error {
	switch event.EventType {
	case "quest.published":
		return h.handleQuestPublished(ctx, event.Payload)
	case "quest.forked":
		return h.handleQuestForked(ctx, event.Payload)
	case "quest.completed":
		return h.handleQuestCompleted(ctx, event.Payload)
	case "task.completed":
		return h.handleTaskCompleted(ctx, event.Payload)
	case "achievement.unlocked":
		return h.handleAchievementUnlocked(ctx, event.Payload)
	default:
		return fmt.Errorf("unknown event type: %s", event.EventType)
	}
}

func (h *EventHandler) handleQuestPublished(ctx context.Context, raw json.RawMessage) error {
	var p struct {
		UserID     string `json:"userId"`
		QuestID    string `json:"questId"`
		QuestTitle string `json:"questTitle"`
	}
	if err := json.Unmarshal(raw, &p); err != nil {
		return err
	}
	userID, err := uuid.Parse(p.UserID)
	if err != nil {
		return fmt.Errorf("parse userId: %w", err)
	}
	return h.actSvc.CreateActivity(ctx, userID, schemas.ActivityQuestPublished, jsonb.JSONB{
		"questId": p.QuestID, "questTitle": p.QuestTitle,
	})
}

func (h *EventHandler) handleQuestForked(ctx context.Context, raw json.RawMessage) error {
	var p struct {
		UserID     string `json:"userId"`
		QuestID    string `json:"questId"`
		QuestTitle string `json:"questTitle"`
	}
	if err := json.Unmarshal(raw, &p); err != nil {
		return err
	}
	userID, err := uuid.Parse(p.UserID)
	if err != nil {
		return fmt.Errorf("parse userId: %w", err)
	}
	return h.actSvc.CreateActivity(ctx, userID, schemas.ActivityQuestForked, jsonb.JSONB{
		"questId": p.QuestID, "questTitle": p.QuestTitle,
	})
}

func (h *EventHandler) handleQuestCompleted(ctx context.Context, raw json.RawMessage) error {
	var p struct {
		UserID     string `json:"userId"`
		QuestID    string `json:"questId"`
		QuestTitle string `json:"questTitle"`
	}
	if err := json.Unmarshal(raw, &p); err != nil {
		return err
	}
	userID, err := uuid.Parse(p.UserID)
	if err != nil {
		return fmt.Errorf("parse userId: %w", err)
	}
	return h.actSvc.CreateActivity(ctx, userID, schemas.ActivityQuestCompleted, jsonb.JSONB{
		"questId": p.QuestID, "questTitle": p.QuestTitle,
	})
}

func (h *EventHandler) handleTaskCompleted(ctx context.Context, raw json.RawMessage) error {
	var p struct {
		UserID           string `json:"userId"`
		QuestID          string `json:"questId"`
		TaskTitle        string `json:"taskTitle"`
		IsQuestCompleted bool   `json:"isQuestCompleted"`
	}
	if err := json.Unmarshal(raw, &p); err != nil {
		return err
	}
	// Only create TASK_COMPLETED feed item for milestone tasks to avoid feed noise.
	// When the quest is also completed, quest.completed event will be the primary feed entry.
	if p.IsQuestCompleted {
		return nil
	}
	userID, err := uuid.Parse(p.UserID)
	if err != nil {
		return fmt.Errorf("parse userId: %w", err)
	}
	return h.actSvc.CreateActivity(ctx, userID, schemas.ActivityTaskCompleted, jsonb.JSONB{
		"questId": p.QuestID, "taskTitle": p.TaskTitle,
	})
}

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
	return h.actSvc.CreateActivity(ctx, userID, schemas.ActivityAchievementUnlocked, jsonb.JSONB{
		"achievementId": p.AchievementID, "title": p.Title,
	})
}
