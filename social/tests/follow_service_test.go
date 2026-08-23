package tests

import (
	"context"
	"testing"

	"social/service"

	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func buildFollowSvc(repo *fakeFollowRepo, outbox *fakeOutbox) service.IFollowService {
	return service.NewFollowService(repo, outbox)
}

func TestFollowService_Follow_StoresRelationship(t *testing.T) {
	repo := newFakeFollowRepo()
	svc := buildFollowSvc(repo, &fakeOutbox{})
	follower, followee := uuid.New(), uuid.New()

	require.NoError(t, svc.Follow(context.Background(), follower, followee))

	ok, err := svc.IsFollowing(context.Background(), follower, followee)
	require.NoError(t, err)
	assert.True(t, ok)
}

func TestFollowService_Unfollow_RemovesRelationship(t *testing.T) {
	repo := newFakeFollowRepo()
	svc := buildFollowSvc(repo, &fakeOutbox{})
	follower, followee := uuid.New(), uuid.New()

	require.NoError(t, svc.Follow(context.Background(), follower, followee))
	require.NoError(t, svc.Unfollow(context.Background(), follower, followee))

	ok, err := svc.IsFollowing(context.Background(), follower, followee)
	require.NoError(t, err)
	assert.False(t, ok)
}

func TestFollowService_CannotFollowSelf(t *testing.T) {
	repo := newFakeFollowRepo()
	svc := buildFollowSvc(repo, &fakeOutbox{})
	id := uuid.New()

	err := svc.Follow(context.Background(), id, id)
	require.Error(t, err)
	assert.Contains(t, err.Error(), "yourself")
}

func TestFollowService_PublishesUserFollowedEvent(t *testing.T) {
	repo := newFakeFollowRepo()
	outbox := &fakeOutbox{}
	svc := buildFollowSvc(repo, outbox)
	follower, followee := uuid.New(), uuid.New()

	require.NoError(t, svc.Follow(context.Background(), follower, followee))

	outbox.mu.Lock()
	defer outbox.mu.Unlock()
	require.Len(t, outbox.published, 1)
	assert.Equal(t, "user.followed", outbox.published[0].EventType)
	assert.Equal(t, follower.String(), outbox.published[0].Payload["followerUserId"])
	assert.Equal(t, followee.String(), outbox.published[0].Payload["followedUserId"])
}

func TestFollowService_IsFollowing_FalseWhenNotFollowing(t *testing.T) {
	svc := buildFollowSvc(newFakeFollowRepo(), &fakeOutbox{})
	ok, err := svc.IsFollowing(context.Background(), uuid.New(), uuid.New())
	require.NoError(t, err)
	assert.False(t, ok)
}
