package repository

import (
	"context"
	"fmt"

	"questhub/social/schemas"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

// ICommentRepository defines data access for quest comments.
type ICommentRepository interface {
	// Create inserts the comment. The path must be set by the caller using
	// BuildPath(parentPath, NextCommentPathSeq()).
	Create(ctx context.Context, c *schemas.Comment) error
	// NextPathSeq returns the next value from comment_path_seq.
	NextPathSeq(ctx context.Context) (int64, error)
	// ListByQuest returns all comments for a quest ordered by path (depth-first).
	ListByQuest(ctx context.Context, questID uuid.UUID) ([]schemas.Comment, error)
	// FindByPath returns the comment with the given path (for reply validation).
	FindByPath(ctx context.Context, path string) (*schemas.Comment, error)
}

// CommentRepository implements ICommentRepository on PostgreSQL via GORM.
type CommentRepository struct{ db *gorm.DB }

func NewCommentRepository(db *gorm.DB) *CommentRepository { return &CommentRepository{db: db} }

func (r *CommentRepository) Create(ctx context.Context, c *schemas.Comment) error {
	return r.db.WithContext(ctx).Create(c).Error
}

func (r *CommentRepository) NextPathSeq(ctx context.Context) (int64, error) {
	var seq int64
	if err := r.db.WithContext(ctx).Raw("SELECT NEXTVAL('comment_path_seq')").Scan(&seq).Error; err != nil {
		return 0, fmt.Errorf("comment_path_seq: %w", err)
	}
	return seq, nil
}

func (r *CommentRepository) ListByQuest(ctx context.Context, questID uuid.UUID) ([]schemas.Comment, error) {
	var results []schemas.Comment
	err := r.db.WithContext(ctx).
		Where("quest_id = ?", questID).
		Order("path"). // lexicographic order = depth-first traversal
		Find(&results).Error
	return results, err
}

func (r *CommentRepository) FindByPath(ctx context.Context, path string) (*schemas.Comment, error) {
	var c schemas.Comment
	if err := r.db.WithContext(ctx).Where("path = ?", path).First(&c).Error; err != nil {
		return nil, err
	}
	return &c, nil
}
