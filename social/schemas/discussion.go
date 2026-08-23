package schemas

import (
	"time"

	"github.com/google/uuid"
)

// Discussion is one row in the discussions table.
// Root discussions have a title; replies inherit the same materialized path
// logic as comments — no parent_id stored.
type Discussion struct {
	ID        uuid.UUID `gorm:"type:uuid;primaryKey;column:id"                       json:"id"`
	QuestID   uuid.UUID `gorm:"type:uuid;index:idx_discussion_quest;column:quest_id"  json:"questId"`
	AuthorID  uuid.UUID `gorm:"type:uuid;column:author_id"                           json:"authorId"`
	Path      string    `gorm:"type:text;uniqueIndex;column:path"                     json:"path"`
	// Title is set only for root discussions (Depth() == 0).
	Title     *string   `gorm:"type:text;column:title"                               json:"title,omitempty"`
	Content   string    `gorm:"type:text;column:content"                             json:"content"`
	CreatedAt time.Time `gorm:"column:created_at"                                    json:"createdAt"`
}

func (Discussion) TableName() string { return "discussions" }

// Depth and ParentPath mirror Comment semantics — path arithmetic is identical.
func (d Discussion) Depth() int { return len(d.Path)/pathSegmentLen - 1 }

func (d Discussion) ParentPath() string {
	if len(d.Path) <= pathSegmentLen {
		return ""
	}
	return d.Path[:len(d.Path)-pathSegmentLen]
}

// CreateDiscussionRequest is the body for POST /quests/:id/discussions.
type CreateDiscussionRequest struct {
	AuthorID string `json:"authorId" binding:"required,uuid"`
	Title    string `json:"title"    binding:"required,min=3,max=200"`
	Content  string `json:"content"  binding:"required,min=1,max=5000"`
}

// ReplyDiscussionRequest is the body for POST /discussions/:id/comments.
type ReplyDiscussionRequest struct {
	AuthorID   string `json:"authorId"   binding:"required,uuid"`
	Content    string `json:"content"    binding:"required,min=1,max=2000"`
	ParentPath string `json:"parentPath" binding:"required"`
}

// DiscussionResponse wraps the Discussion row with optional author info.
type DiscussionResponse struct {
	Discussion
	AuthorUsername string `json:"authorUsername"`
}

// ListDiscussionsResponse is the paginated discussion list for a quest.
type ListDiscussionsResponse struct {
	Data []DiscussionResponse `json:"data"`
}
