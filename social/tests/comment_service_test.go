package tests

import (
	"context"
	"testing"

	"questhub/social/schemas"
	"questhub/social/service"

	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"gorm.io/gorm"
)

func buildCommentSvc(repo *fakeCommentRepo, outbox *fakeOutbox) service.ICommentService {
	return service.NewCommentService(repo, outbox)
}

func TestCommentService_CreateRootComment(t *testing.T) {
	repo := newFakeCommentRepo()
	svc := buildCommentSvc(repo, &fakeOutbox{})
	questID := uuid.New()

	c, err := svc.Create(context.Background(), questID, &schemas.CreateCommentRequest{
		AuthorID: uuid.New().String(),
		Content:  "Great quest!",
	})
	require.NoError(t, err)
	require.NotNil(t, c)

	assert.Equal(t, 10, len(c.Path), "root path must be 10 chars")
	assert.Equal(t, 0, c.Depth())
	assert.Equal(t, questID, c.QuestID)
}

func TestCommentService_CreateReply(t *testing.T) {
	repo := newFakeCommentRepo()
	svc := buildCommentSvc(repo, &fakeOutbox{})
	questID := uuid.New()

	root, err := svc.Create(context.Background(), questID, &schemas.CreateCommentRequest{
		AuthorID: uuid.New().String(),
		Content:  "Root comment",
	})
	require.NoError(t, err)

	reply, err := svc.Create(context.Background(), questID, &schemas.CreateCommentRequest{
		AuthorID:   uuid.New().String(),
		Content:    "Reply",
		ParentPath: root.Path,
	})
	require.NoError(t, err)

	assert.Equal(t, 20, len(reply.Path), "reply path must be 20 chars")
	assert.Equal(t, 1, reply.Depth())
	assert.Equal(t, root.Path, reply.ParentPath())
}

func TestCommentService_MaxDepthEnforced(t *testing.T) {
	repo := newFakeCommentRepo()
	svc := buildCommentSvc(repo, &fakeOutbox{})
	questID := uuid.New()

	// Create root → reply (depth 1) → try depth 2 (should fail at maxCommentDepth=1)
	root, _ := svc.Create(context.Background(), questID, &schemas.CreateCommentRequest{
		AuthorID: uuid.New().String(), Content: "root",
	})
	reply, _ := svc.Create(context.Background(), questID, &schemas.CreateCommentRequest{
		AuthorID: uuid.New().String(), Content: "reply", ParentPath: root.Path,
	})

	_, err := svc.Create(context.Background(), questID, &schemas.CreateCommentRequest{
		AuthorID: uuid.New().String(), Content: "too deep", ParentPath: reply.Path,
	})
	require.Error(t, err)
	assert.Contains(t, err.Error(), "depth")
}

func TestCommentService_InvalidParentPath_ReturnsError(t *testing.T) {
	repo := newFakeCommentRepo()
	// FindByPath returns nil (not found) by default
	svc := buildCommentSvc(repo, &fakeOutbox{})

	_, err := svc.Create(context.Background(), uuid.New(), &schemas.CreateCommentRequest{
		AuthorID:   uuid.New().String(),
		Content:    "reply to ghost",
		ParentPath: "0000000099", // doesn't exist in repo
	})
	require.Error(t, err)
}

func TestCommentService_FindByPathError_Propagates(t *testing.T) {
	repo := newFakeCommentRepo()
	repo.findErr = gorm.ErrInvalidDB
	svc := buildCommentSvc(repo, &fakeOutbox{})

	_, err := svc.Create(context.Background(), uuid.New(), &schemas.CreateCommentRequest{
		AuthorID:   uuid.New().String(),
		Content:    "reply",
		ParentPath: "0000000001",
	})
	require.Error(t, err)
}

func TestCommentService_PublishesOutboxEvent(t *testing.T) {
	repo := newFakeCommentRepo()
	outbox := &fakeOutbox{}
	svc := buildCommentSvc(repo, outbox)

	_, err := svc.Create(context.Background(), uuid.New(), &schemas.CreateCommentRequest{
		AuthorID: uuid.New().String(),
		Content:  "hi",
	})
	require.NoError(t, err)

	outbox.mu.Lock()
	defer outbox.mu.Unlock()
	require.Len(t, outbox.published, 1)
	assert.Equal(t, "comment.created", outbox.published[0].EventType)
}

func TestCommentService_ListByQuest_ReturnsAll(t *testing.T) {
	repo := newFakeCommentRepo()
	svc := buildCommentSvc(repo, &fakeOutbox{})
	questID := uuid.New()

	for range 3 {
		_, err := svc.Create(context.Background(), questID, &schemas.CreateCommentRequest{
			AuthorID: uuid.New().String(), Content: "comment",
		})
		require.NoError(t, err)
	}

	resp, err := svc.ListByQuest(context.Background(), questID)
	require.NoError(t, err)
	assert.Len(t, resp.Data, 3)
}
