package service

import (
	"context"
	"fmt"

	"social/repository"
	"social/schemas"

	"github.com/google/uuid"
)

// IFollowService defines follow/unfollow business operations.
type IFollowService interface {
	Follow(ctx context.Context, followerID, followeeID uuid.UUID) error
	Unfollow(ctx context.Context, followerID, followeeID uuid.UUID) error
	IsFollowing(ctx context.Context, followerID, followeeID uuid.UUID) (bool, error)
	ListFollowing(ctx context.Context, followerID uuid.UUID, page, limit int) ([]schemas.Follow, error)
	ListFollowers(ctx context.Context, followeeID uuid.UUID, page, limit int) ([]schemas.Follow, error)
}

// FollowService implements IFollowService.
type FollowService struct {
	repo     repository.IFollowRepository
	outbox   IOutboxPublisher
}

func NewFollowService(repo repository.IFollowRepository, outbox IOutboxPublisher) *FollowService {
	return &FollowService{repo: repo, outbox: outbox}
}

func (s *FollowService) Follow(ctx context.Context, followerID, followeeID uuid.UUID) error {
	if followerID == followeeID {
		return fmt.Errorf("cannot follow yourself")
	}
	if err := s.repo.Follow(ctx, followerID, followeeID); err != nil {
		return err
	}
	// Publish event so notification service can notify the followee.
	return s.outbox.Publish(ctx, "user.followed", map[string]any{
		"followerUserId": followerID.String(),
		"followedUserId": followeeID.String(),
	})
}

func (s *FollowService) Unfollow(ctx context.Context, followerID, followeeID uuid.UUID) error {
	return s.repo.Unfollow(ctx, followerID, followeeID)
}

func (s *FollowService) IsFollowing(ctx context.Context, followerID, followeeID uuid.UUID) (bool, error) {
	return s.repo.IsFollowing(ctx, followerID, followeeID)
}

func (s *FollowService) ListFollowing(ctx context.Context, followerID uuid.UUID, page, limit int) ([]schemas.Follow, error) {
	page, limit = normalizePage(page, limit)
	return s.repo.ListFollowing(ctx, followerID, limit, (page-1)*limit)
}

func (s *FollowService) ListFollowers(ctx context.Context, followeeID uuid.UUID, page, limit int) ([]schemas.Follow, error) {
	page, limit = normalizePage(page, limit)
	return s.repo.ListFollowers(ctx, followeeID, limit, (page-1)*limit)
}
