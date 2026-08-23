package service

import (
	"context"
	"errors"
	"fmt"

	"questhub/social/repository"
	"questhub/social/schemas"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

const maxDiscussionDepth = 1

// IDiscussionService defines discussion business operations.
type IDiscussionService interface {
	Create(ctx context.Context, questID uuid.UUID, req *schemas.CreateDiscussionRequest) (*schemas.Discussion, error)
	Reply(ctx context.Context, questID uuid.UUID, req *schemas.ReplyDiscussionRequest) (*schemas.Discussion, error)
	ListByQuest(ctx context.Context, questID uuid.UUID) (*schemas.ListDiscussionsResponse, error)
	ListReplies(ctx context.Context, rootPath string) (*schemas.ListDiscussionsResponse, error)
}

// DiscussionService implements IDiscussionService.
type DiscussionService struct {
	repo   repository.IDiscussionRepository
	outbox IOutboxPublisher
}

func NewDiscussionService(repo repository.IDiscussionRepository, outbox IOutboxPublisher) *DiscussionService {
	return &DiscussionService{repo: repo, outbox: outbox}
}

// Create opens a new root discussion thread on a quest.
func (s *DiscussionService) Create(ctx context.Context, questID uuid.UUID, req *schemas.CreateDiscussionRequest) (*schemas.Discussion, error) {
	authorID, _ := uuid.Parse(req.AuthorID)

	seq, err := s.repo.NextPathSeq(ctx)
	if err != nil {
		return nil, err
	}

	title := req.Title
	d := &schemas.Discussion{
		ID:       uuid.New(),
		QuestID:  questID,
		AuthorID: authorID,
		Path:     schemas.BuildPath("", seq),
		Title:    &title,
		Content:  req.Content,
	}
	if err := s.repo.Create(ctx, d); err != nil {
		return nil, err
	}

	_ = s.outbox.Publish(ctx, "discussion.created", map[string]any{
		"questId":      questID.String(),
		"discussionId": d.ID.String(),
		"authorId":     authorID.String(),
		"title":        title,
	})

	return d, nil
}

// Reply adds a reply under an existing discussion node.
func (s *DiscussionService) Reply(ctx context.Context, questID uuid.UUID, req *schemas.ReplyDiscussionRequest) (*schemas.Discussion, error) {
	authorID, _ := uuid.Parse(req.AuthorID)

	parent, err := s.repo.FindByPath(ctx, req.ParentPath)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, fmt.Errorf("parent discussion not found")
		}
		return nil, err
	}
	if parent.Depth() >= maxDiscussionDepth {
		return nil, fmt.Errorf("max reply depth (%d) reached", maxDiscussionDepth+1)
	}

	seq, err := s.repo.NextPathSeq(ctx)
	if err != nil {
		return nil, err
	}

	d := &schemas.Discussion{
		ID:       uuid.New(),
		QuestID:  questID,
		AuthorID: authorID,
		Path:     schemas.BuildPath(req.ParentPath, seq),
		Content:  req.Content,
	}
	if err := s.repo.Create(ctx, d); err != nil {
		return nil, err
	}

	_ = s.outbox.Publish(ctx, "comment.created", map[string]any{
		"questId":      questID.String(),
		"discussionId": d.ID.String(),
		"authorId":     authorID.String(),
		"parentPath":   req.ParentPath,
	})

	return d, nil
}

func (s *DiscussionService) ListByQuest(ctx context.Context, questID uuid.UUID) (*schemas.ListDiscussionsResponse, error) {
	rows, err := s.repo.ListByQuest(ctx, questID)
	if err != nil {
		return nil, err
	}
	resp := make([]schemas.DiscussionResponse, len(rows))
	for i, d := range rows {
		resp[i] = schemas.DiscussionResponse{Discussion: d}
	}
	return &schemas.ListDiscussionsResponse{Data: resp}, nil
}

func (s *DiscussionService) ListReplies(ctx context.Context, rootPath string) (*schemas.ListDiscussionsResponse, error) {
	rows, err := s.repo.ListReplies(ctx, rootPath)
	if err != nil {
		return nil, err
	}
	resp := make([]schemas.DiscussionResponse, len(rows))
	for i, d := range rows {
		resp[i] = schemas.DiscussionResponse{Discussion: d}
	}
	return &schemas.ListDiscussionsResponse{Data: resp}, nil
}
