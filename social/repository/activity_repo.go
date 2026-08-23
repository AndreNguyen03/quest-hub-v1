package repository

import (
	"context"

	"questhub/social/schemas"
	"questhub/social/util/jsonb"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

// IActivityRepository defines data access for feed activities.
type IActivityRepository interface {
	Create(ctx context.Context, a *schemas.Activity) error
	// Feed returns activities from users that followerID follows, newest first.
	Feed(ctx context.Context, followerID uuid.UUID, limit, offset int) ([]schemas.Activity, error)
	// ListByUser returns activities for a specific user's public profile.
	ListByUser(ctx context.Context, userID uuid.UUID, limit, offset int) ([]schemas.Activity, error)
}

// ActivityRepository implements IActivityRepository on PostgreSQL via GORM.
type ActivityRepository struct{ db *gorm.DB }

func NewActivityRepository(db *gorm.DB) *ActivityRepository { return &ActivityRepository{db: db} }

func (r *ActivityRepository) Create(ctx context.Context, a *schemas.Activity) error {
	if a.Payload == nil {
		a.Payload = jsonb.JSONB{}
	}
	return r.db.WithContext(ctx).Create(a).Error
}

func (r *ActivityRepository) Feed(ctx context.Context, followerID uuid.UUID, limit, offset int) ([]schemas.Activity, error) {
	var results []schemas.Activity
	err := r.db.WithContext(ctx).
		Where("user_id IN (SELECT followee_id FROM follows WHERE follower_id = ?)", followerID).
		Order("created_at DESC").
		Limit(limit).Offset(offset).
		Find(&results).Error
	return results, err
}

func (r *ActivityRepository) ListByUser(ctx context.Context, userID uuid.UUID, limit, offset int) ([]schemas.Activity, error) {
	var results []schemas.Activity
	err := r.db.WithContext(ctx).
		Where("user_id = ?", userID).
		Order("created_at DESC").
		Limit(limit).Offset(offset).
		Find(&results).Error
	return results, err
}
