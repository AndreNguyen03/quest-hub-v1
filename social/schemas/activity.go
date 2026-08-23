package schemas

import (
	"time"

	"questhub/social/util/jsonb"

	"github.com/google/uuid"
)

// ActivityType enumerates the types of feed events.
type ActivityType string

const (
	ActivityQuestCompleted   ActivityType = "QUEST_COMPLETED"
	ActivityQuestForked      ActivityType = "QUEST_FORKED"
	ActivityTaskCompleted    ActivityType = "TASK_COMPLETED"
	ActivityQuestPublished   ActivityType = "QUEST_PUBLISHED"
	ActivityAchievementUnlocked ActivityType = "ACHIEVEMENT_UNLOCKED"
)

// Activity is one feed event row, always scoped to a single actor (user).
type Activity struct {
	ID        uuid.UUID    `gorm:"type:uuid;primaryKey;column:id"      json:"id"`
	UserID    uuid.UUID    `gorm:"type:uuid;index;column:user_id"      json:"userId"`
	Type      ActivityType `gorm:"type:text;column:type"               json:"type"`
	Payload   jsonb.JSONB  `gorm:"type:jsonb;column:payload"           json:"payload"`
	CreatedAt time.Time    `gorm:"column:created_at"                   json:"createdAt"`
}

func (Activity) TableName() string { return "activities" }

// FeedResponse is the paginated feed payload.
type FeedResponse struct {
	Data  []Activity `json:"data"`
	Page  int        `json:"page"`
	Limit int        `json:"limit"`
}
