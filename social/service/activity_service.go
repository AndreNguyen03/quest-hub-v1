package service

import (
	"context"

	"social/repository"
	"social/schemas"
	"social/util/jsonb"

	"github.com/google/uuid"
)

// IActivityService defines feed activity operations.
type IActivityService interface {
	CreateActivity(ctx context.Context, userID uuid.UUID, actType schemas.ActivityType, payload jsonb.JSONB) error
	Feed(ctx context.Context, followerID uuid.UUID, page, limit int) (*schemas.FeedResponse, error)
	ListByUser(ctx context.Context, userID uuid.UUID, page, limit int) (*schemas.FeedResponse, error)
}

// ActivityService implements IActivityService.
type ActivityService struct {
	repo repository.IActivityRepository
}

func NewActivityService(repo repository.IActivityRepository) *ActivityService {
	return &ActivityService{repo: repo}
}

func (s *ActivityService) CreateActivity(ctx context.Context, userID uuid.UUID, actType schemas.ActivityType, payload jsonb.JSONB) error {
	return s.repo.Create(ctx, &schemas.Activity{
		ID:      uuid.New(),
		UserID:  userID,
		Type:    actType,
		Payload: payload,
	})
}

func (s *ActivityService) Feed(ctx context.Context, followerID uuid.UUID, page, limit int) (*schemas.FeedResponse, error) {
	page, limit = normalizePage(page, limit)
	items, err := s.repo.Feed(ctx, followerID, limit, (page-1)*limit)
	if err != nil {
		return nil, err
	}
	return &schemas.FeedResponse{Data: items, Page: page, Limit: limit}, nil
}

func (s *ActivityService) ListByUser(ctx context.Context, userID uuid.UUID, page, limit int) (*schemas.FeedResponse, error) {
	page, limit = normalizePage(page, limit)
	items, err := s.repo.ListByUser(ctx, userID, limit, (page-1)*limit)
	if err != nil {
		return nil, err
	}
	return &schemas.FeedResponse{Data: items, Page: page, Limit: limit}, nil
}
