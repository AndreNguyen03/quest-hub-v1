// Package schemas defines request/response DTOs and the GORM entity for the
// notification service.
package schemas

import (
	"time"

	"notification/util/jsonb"

	"github.com/google/uuid"
)

// NotificationType enumerates values allowed by notifications.type CHECK.
type NotificationType string

const (
	// TypeTaskCompleted is emitted when a user completes a personal task.
	TypeTaskCompleted NotificationType = "TASK_COMPLETED"
	// TypeQuestCompleted is emitted when a user completes a whole quest.
	TypeQuestCompleted NotificationType = "QUEST_COMPLETED"
	// TypeAchievement is emitted when an achievement unlocks.
	TypeAchievement NotificationType = "ACHIEVEMENT"
	// TypeFollowed is emitted when another user follows you.
	TypeFollowed NotificationType = "FOLLOWED"
	// TypeComment is emitted for comment/discussion activity on your quest.
	TypeComment NotificationType = "COMMENT"
	// TypeReview is emitted for AI submission grading results.
	TypeReview NotificationType = "REVIEW"
	// TypeAdmin is used for admin broadcast messages.
	TypeAdmin NotificationType = "ADMIN"
)

// Notification mirrors one row of the notifications table (GORM entity).
type Notification struct {
	ID        uuid.UUID        `gorm:"type:uuid;primaryKey;column:id" json:"id"`
	UserID    uuid.UUID        `gorm:"type:uuid;index;column:user_id" json:"userId"`
	Type      NotificationType `gorm:"type:text;column:type" json:"type"`
	Title     string           `gorm:"column:title" json:"title"`
	Body      *string          `gorm:"column:body" json:"body,omitempty"` // nullable
	Payload   jsonb.JSONB      `gorm:"type:jsonb;column:payload" json:"payload"`
	IsRead    bool             `gorm:"column:is_read" json:"isRead"`
	CreatedAt time.Time        `gorm:"column:created_at" json:"createdAt"`
}

// TableName overrides the default GORM table naming convention.
func (Notification) TableName() string { return "notifications" }

// ListNotificationsRequest is the query DTO for GET /api/v1/notifications.
type ListNotificationsRequest struct {
	UserID string `form:"userId" binding:"required,uuid"`
	Page   int    `form:"page" binding:"omitempty,min=1"`
	Limit  int    `form:"limit" binding:"omitempty,min=1,max=100"`
}

// ListNotificationsResponse is the payload for the inbox endpoint.
type ListNotificationsResponse struct {
	Data  []Notification `json:"data"`
	Page  int            `json:"page"`
	Limit int            `json:"limit"`
}

// MarkAllReadRequest is the query DTO for PATCH /api/v1/notifications/read-all.
type MarkAllReadRequest struct {
	UserID string `form:"userId" binding:"required,uuid"`
}

// UnreadCountRequest is the query DTO for GET /api/v1/notifications/unread-count.
type UnreadCountRequest struct {
	UserID string `form:"userId" binding:"required,uuid"`
}

// UnreadCountResponse is the payload for the unread badge count.
type UnreadCountResponse struct {
	Count int `json:"count"`
}

// BroadcastRequest is the body for POST /api/v1/notifications/broadcast.
// UserIDs is optional — omit to broadcast to all users (admin use only).
type BroadcastRequest struct {
	UserIDs []string         `json:"userIds"`
	Type    NotificationType `json:"type" binding:"required,oneof=TASK_COMPLETED QUEST_COMPLETED ACHIEVEMENT FOLLOWED COMMENT REVIEW ADMIN"`
	Title   string           `json:"title" binding:"required"`
	Body    string           `json:"body"`
}
