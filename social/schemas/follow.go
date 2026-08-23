package schemas

import (
	"time"

	"github.com/google/uuid"
)

// Follow represents a directed follow relationship: Follower follows Followee.
type Follow struct {
	FollowerID uuid.UUID `gorm:"type:uuid;primaryKey;column:follower_id"`
	FolloweeID uuid.UUID `gorm:"type:uuid;primaryKey;column:followee_id"`
	CreatedAt  time.Time `gorm:"column:created_at"`
}

func (Follow) TableName() string { return "follows" }

// FollowUserRequest is the body for POST /users/:username/follow.
type FollowUserRequest struct {
	FollowerID string `json:"followerId" binding:"required,uuid"`
}

// FollowResponse is a single follow entry returned in list responses.
type FollowResponse struct {
	UserID    uuid.UUID `json:"userId"`
	Username  string    `json:"username"`
	CreatedAt time.Time `json:"createdAt"`
}
