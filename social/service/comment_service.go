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

const maxCommentDepth = 1 // 0 = root, 1 = first-level reply (max 2 levels total)

// ICommentService defines comment business operations.
type ICommentService interface {
	Create(ctx context.Context, questID uuid.UUID, req *schemas.CreateCommentRequest) (*schemas.Comment, error)
	ListByQuest(ctx context.Context, questID uuid.UUID) (*schemas.ListCommentsResponse, error)
}

// CommentService implements ICommentService.
type CommentService struct {
	repo   repository.ICommentRepository
	outbox IOutboxPublisher
}

func NewCommentService(repo repository.ICommentRepository, outbox IOutboxPublisher) *CommentService {
	return &CommentService{repo: repo, outbox: outbox}
}

// Create adds a root comment or a reply. Replies are capped at maxCommentDepth.
func (s *CommentService) Create(ctx context.Context, questID uuid.UUID, req *schemas.CreateCommentRequest) (*schemas.Comment, error) {
	authorID, _ := uuid.Parse(req.AuthorID)
	parentPath := req.ParentPath

	// Validate parent exists and depth limit.
	if parentPath != "" {
		parent, err := s.repo.FindByPath(ctx, parentPath)
		if err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				return nil, fmt.Errorf("parent comment not found")
			}
			return nil, err
		}
		if parent == nil {
			return nil, fmt.Errorf("parent comment not found")
		}
		if parent.Depth() >= maxCommentDepth {
			return nil, fmt.Errorf("max reply depth (%d) reached", maxCommentDepth+1)
		}
	}

	seq, err := s.repo.NextPathSeq(ctx)
	if err != nil {
		return nil, err
	}

	comment := &schemas.Comment{
		ID:       uuid.New(),
		QuestID:  questID,
		AuthorID: authorID,
		Path:     schemas.BuildPath(parentPath, seq),
		Content:  req.Content,
	}
	if err := s.repo.Create(ctx, comment); err != nil {
		return nil, err
	}

	// Publish event so notification service notifies the quest owner.
	_ = s.outbox.Publish(ctx, "comment.created", map[string]any{
		"questId":        questID.String(),
		"commentId":      comment.ID.String(),
		"authorId":       authorID.String(),
		"parentPath":     parentPath,
	})

	return comment, nil
}

// ListByQuest returns all comments for a quest in depth-first order.
func (s *CommentService) ListByQuest(ctx context.Context, questID uuid.UUID) (*schemas.ListCommentsResponse, error) {
	rows, err := s.repo.ListByQuest(ctx, questID)
	if err != nil {
		return nil, err
	}
	resp := make([]schemas.CommentResponse, len(rows))
	for i, c := range rows {
		resp[i] = schemas.CommentResponse{Comment: c}
	}
	return &schemas.ListCommentsResponse{Data: resp}, nil
}
