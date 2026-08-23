package repository

import (
	"context"
	"fmt"

	"questhub/social/schemas"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

// IDiscussionRepository defines data access for quest discussions.
type IDiscussionRepository interface {
	Create(ctx context.Context, d *schemas.Discussion) error
	NextPathSeq(ctx context.Context) (int64, error)
	// ListByQuest returns root discussions and their replies ordered by path.
	ListByQuest(ctx context.Context, questID uuid.UUID) ([]schemas.Discussion, error)
	// ListReplies returns all replies under a root discussion path prefix.
	ListReplies(ctx context.Context, rootPath string) ([]schemas.Discussion, error)
	FindByPath(ctx context.Context, path string) (*schemas.Discussion, error)
}

// DiscussionRepository implements IDiscussionRepository on PostgreSQL via GORM.
type DiscussionRepository struct{ db *gorm.DB }

func NewDiscussionRepository(db *gorm.DB) *DiscussionRepository {
	return &DiscussionRepository{db: db}
}

func (r *DiscussionRepository) Create(ctx context.Context, d *schemas.Discussion) error {
	return r.db.WithContext(ctx).Create(d).Error
}

func (r *DiscussionRepository) NextPathSeq(ctx context.Context) (int64, error) {
	var seq int64
	if err := r.db.WithContext(ctx).Raw("SELECT NEXTVAL('discussion_path_seq')").Scan(&seq).Error; err != nil {
		return 0, fmt.Errorf("discussion_path_seq: %w", err)
	}
	return seq, nil
}

func (r *DiscussionRepository) ListByQuest(ctx context.Context, questID uuid.UUID) ([]schemas.Discussion, error) {
	var results []schemas.Discussion
	err := r.db.WithContext(ctx).
		Where("quest_id = ?", questID).
		Order("path").
		Find(&results).Error
	return results, err
}

func (r *DiscussionRepository) ListReplies(ctx context.Context, rootPath string) ([]schemas.Discussion, error) {
	var results []schemas.Discussion
	// All rows whose path starts with rootPath and is longer (i.e. descendants).
	err := r.db.WithContext(ctx).
		Where("path LIKE ? AND path != ?", rootPath+"%", rootPath).
		Order("path").
		Find(&results).Error
	return results, err
}

func (r *DiscussionRepository) FindByPath(ctx context.Context, path string) (*schemas.Discussion, error) {
	var d schemas.Discussion
	if err := r.db.WithContext(ctx).Where("path = ?", path).First(&d).Error; err != nil {
		return nil, err
	}
	return &d, nil
}
