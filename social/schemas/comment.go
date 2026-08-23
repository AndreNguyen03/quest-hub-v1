package schemas

import (
	"fmt"
	"time"

	"github.com/google/uuid"
)

const pathSegmentLen = 10 // zero-padded digits per tree level

// Comment is one row in the comments table.
//
// Tree structure is encoded in the `path` column using fixed-width
// materialized path (lexicographically sortable). Each node contributes
// exactly 10 zero-padded digits to the path:
//
//	root comment  → "0000000001"
//	reply to root → "00000000010000000002"
//
// ORDER BY path produces correct depth-first traversal order.
// Parent path is derived by trimming the last pathSegmentLen characters.
// No parent_id column is stored.
type Comment struct {
	ID        uuid.UUID `gorm:"type:uuid;primaryKey;column:id"                   json:"id"`
	QuestID   uuid.UUID `gorm:"type:uuid;index:idx_comment_quest;column:quest_id" json:"questId"`
	AuthorID  uuid.UUID `gorm:"type:uuid;column:author_id"                       json:"authorId"`
	// Path encodes the tree position. Index covers (quest_id, path) for efficient subtree scans.
	Path      string    `gorm:"type:text;uniqueIndex;column:path"                 json:"path"`
	Content   string    `gorm:"type:text;column:content"                         json:"content"`
	CreatedAt time.Time `gorm:"column:created_at"                                json:"createdAt"`
}

func (Comment) TableName() string { return "comments" }

// Depth returns 0 for root comments, 1 for first-level replies, etc.
func (c Comment) Depth() int { return len(c.Path)/pathSegmentLen - 1 }

// ParentPath returns the path of the parent comment, or "" for root comments.
func (c Comment) ParentPath() string {
	if len(c.Path) <= pathSegmentLen {
		return ""
	}
	return c.Path[:len(c.Path)-pathSegmentLen]
}

// BuildPath constructs a child path from a parent path and the next sequence value.
func BuildPath(parentPath string, seq int64) string {
	return parentPath + fmt.Sprintf("%0*d", pathSegmentLen, seq)
}

// CreateCommentRequest is the body for POST /quests/:id/comments.
type CreateCommentRequest struct {
	AuthorID   string `json:"authorId"   binding:"required,uuid"`
	Content    string `json:"content"    binding:"required,min=1,max=2000"`
	// ParentPath is the path of the parent comment for replies. Empty = root comment.
	ParentPath string `json:"parentPath"`
}

// CommentResponse adds optional author info to the comment row.
type CommentResponse struct {
	Comment
	AuthorUsername string `json:"authorUsername"`
}

// ListCommentsResponse is the paginated comment list for a quest.
type ListCommentsResponse struct {
	Data []CommentResponse `json:"data"`
}
