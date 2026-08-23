package tests

import (
	"context"
	"testing"
	"time"

	"questhub/notification/infra/email"
	"questhub/notification/infra/push"
	"questhub/notification/infra/sse"
	"questhub/notification/schemas"
	"questhub/notification/service"

	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func buildNotifSvc(repo *fakeNotifRepo, hub *sse.Hub) service.INotificationService {
	return service.NewNotificationService(
		repo,
		&fakeTokenRepo{},
		newFakeEmailRepo(),
		hub,
		push.NewFCMClient(""),
		email.NewMailer("", 0, "", "", ""),
	)
}

func TestNotify_PersistsNotification(t *testing.T) {
	repo := &fakeNotifRepo{}
	svc := buildNotifSvc(repo, sse.NewHub())

	n := &schemas.Notification{ID: uuid.New(), UserID: uuid.New(), Type: schemas.TypeAdmin, Title: "hi"}
	require.NoError(t, svc.Notify(context.Background(), n))

	repo.mu.Lock()
	defer repo.mu.Unlock()
	require.Len(t, repo.created, 1)
	assert.Equal(t, n.ID, repo.created[0].ID)
}

func TestNotify_PushesSSERealTime(t *testing.T) {
	repo := &fakeNotifRepo{}
	hub := sse.NewHub()
	svc := buildNotifSvc(repo, hub)

	userID := uuid.New()
	ch := hub.Subscribe(userID)
	defer hub.Unsubscribe(userID, ch)

	n := &schemas.Notification{ID: uuid.New(), UserID: userID, Type: schemas.TypeAdmin, Title: "sse test"}
	require.NoError(t, svc.Notify(context.Background(), n))

	select {
	case got := <-ch:
		assert.Equal(t, n.ID, got.ID)
	case <-time.After(200 * time.Millisecond):
		t.Fatal("SSE push not received within timeout")
	}
}

func TestNotify_RepoErrorPropagates(t *testing.T) {
	repo := &fakeNotifRepo{createErr: assert.AnError}
	svc := buildNotifSvc(repo, sse.NewHub())

	err := svc.Notify(context.Background(), &schemas.Notification{
		ID: uuid.New(), UserID: uuid.New(), Type: schemas.TypeAdmin, Title: "x",
	})
	require.Error(t, err)
}

func TestBroadcast_CreatesOneNotifPerUser(t *testing.T) {
	repo := &fakeNotifRepo{}
	svc := buildNotifSvc(repo, sse.NewHub())

	userIDs := []string{uuid.New().String(), uuid.New().String(), uuid.New().String()}
	require.NoError(t, svc.Broadcast(context.Background(), &schemas.BroadcastRequest{
		UserIDs: userIDs,
		Type:    schemas.TypeAdmin,
		Title:   "system update",
	}))

	repo.mu.Lock()
	defer repo.mu.Unlock()
	assert.Len(t, repo.created, len(userIDs))
}

func TestBroadcast_NotifTypeAndTitlePreserved(t *testing.T) {
	repo := &fakeNotifRepo{}
	svc := buildNotifSvc(repo, sse.NewHub())

	require.NoError(t, svc.Broadcast(context.Background(), &schemas.BroadcastRequest{
		UserIDs: []string{uuid.New().String()},
		Type:    schemas.TypeAchievement,
		Title:   "Special event",
		Body:    "Details here",
	}))

	repo.mu.Lock()
	defer repo.mu.Unlock()
	require.Len(t, repo.created, 1)
	assert.Equal(t, schemas.TypeAchievement, repo.created[0].Type)
	assert.Equal(t, "Special event", repo.created[0].Title)
	require.NotNil(t, repo.created[0].Body)
	assert.Equal(t, "Details here", *repo.created[0].Body)
}

func TestBroadcast_InvalidUUIDReturnsError(t *testing.T) {
	svc := buildNotifSvc(&fakeNotifRepo{}, sse.NewHub())
	err := svc.Broadcast(context.Background(), &schemas.BroadcastRequest{
		UserIDs: []string{"not-a-uuid"},
		Type:    schemas.TypeAdmin,
		Title:   "x",
	})
	require.Error(t, err)
	assert.Contains(t, err.Error(), "not-a-uuid")
}

func TestListByUser_DefaultsApplied(t *testing.T) {
	svc := buildNotifSvc(&fakeNotifRepo{}, sse.NewHub())
	resp, err := svc.ListByUser(context.Background(), uuid.New(), 0, 0)
	require.NoError(t, err)
	assert.Equal(t, 1, resp.Page)
	assert.Equal(t, 20, resp.Limit)
}

func TestListByUser_LimitCappedAt100(t *testing.T) {
	svc := buildNotifSvc(&fakeNotifRepo{}, sse.NewHub())
	resp, err := svc.ListByUser(context.Background(), uuid.New(), 1, 999)
	require.NoError(t, err)
	assert.Equal(t, 100, resp.Limit)
}

func TestListByUser_NegativePageBecomesOne(t *testing.T) {
	svc := buildNotifSvc(&fakeNotifRepo{}, sse.NewHub())
	resp, err := svc.ListByUser(context.Background(), uuid.New(), -5, 10)
	require.NoError(t, err)
	assert.Equal(t, 1, resp.Page)
}

func TestUnreadCount_ReturnsRepoValue(t *testing.T) {
	repo := &fakeNotifRepo{countVal: 42}
	svc := buildNotifSvc(repo, sse.NewHub())
	resp, err := svc.UnreadCount(context.Background(), uuid.New())
	require.NoError(t, err)
	assert.Equal(t, 42, resp.Count)
}
