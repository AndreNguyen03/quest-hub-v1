package repository

import (
	"context"
	"errors"

	"social/schemas"

	"github.com/google/uuid"
	"gorm.io/gorm"
	"gorm.io/gorm/clause"
)

// IFollowRepository defines data access for follow relationships.
type IFollowRepository interface {
	Follow(ctx context.Context, followerID, followeeID uuid.UUID) error
	Unfollow(ctx context.Context, followerID, followeeID uuid.UUID) error
	IsFollowing(ctx context.Context, followerID, followeeID uuid.UUID) (bool, error)
	ListFollowing(ctx context.Context, followerID uuid.UUID, limit, offset int) ([]schemas.Follow, error)
	ListFollowers(ctx context.Context, followeeID uuid.UUID, limit, offset int) ([]schemas.Follow, error)
}

// FollowRepository implements IFollowRepository on PostgreSQL via GORM.
type FollowRepository struct{ db *gorm.DB }

func NewFollowRepository(db *gorm.DB) *FollowRepository { return &FollowRepository{db: db} }

func (r *FollowRepository) Follow(ctx context.Context, followerID, followeeID uuid.UUID) error {
	f := schemas.Follow{FollowerID: followerID, FolloweeID: followeeID}
	return r.db.WithContext(ctx).
		Clauses(clause.OnConflict{DoNothing: true}).
		Create(&f).Error
}

func (r *FollowRepository) Unfollow(ctx context.Context, followerID, followeeID uuid.UUID) error {
	return r.db.WithContext(ctx).
		Where("follower_id = ? AND followee_id = ?", followerID, followeeID).
		Delete(&schemas.Follow{}).Error
}

func (r *FollowRepository) IsFollowing(ctx context.Context, followerID, followeeID uuid.UUID) (bool, error) {
	var f schemas.Follow
	err := r.db.WithContext(ctx).
		Where("follower_id = ? AND followee_id = ?", followerID, followeeID).
		First(&f).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return false, nil
	}
	return err == nil, err
}

func (r *FollowRepository) ListFollowing(ctx context.Context, followerID uuid.UUID, limit, offset int) ([]schemas.Follow, error) {
	var results []schemas.Follow
	err := r.db.WithContext(ctx).
		Where("follower_id = ?", followerID).
		Order("created_at DESC").
		Limit(limit).Offset(offset).
		Find(&results).Error
	return results, err
}

func (r *FollowRepository) ListFollowers(ctx context.Context, followeeID uuid.UUID, limit, offset int) ([]schemas.Follow, error) {
	var results []schemas.Follow
	err := r.db.WithContext(ctx).
		Where("followee_id = ?", followeeID).
		Order("created_at DESC").
		Limit(limit).Offset(offset).
		Find(&results).Error
	return results, err
}
