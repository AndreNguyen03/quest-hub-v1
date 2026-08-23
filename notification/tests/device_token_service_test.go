package tests

import (
	"context"
	"testing"

	"notification/schemas"
	"notification/service"

	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestDeviceToken_Register_UpsertsCalled(t *testing.T) {
	repo := &fakeTokenRepo{}
	svc := service.NewDeviceTokenService(repo)

	userID := uuid.New()
	require.NoError(t, svc.Register(context.Background(), &schemas.RegisterDeviceTokenRequest{
		UserID:   userID.String(),
		Token:    "fcm-token-abc",
		Platform: schemas.PlatformAndroid,
	}))

	repo.mu.Lock()
	defer repo.mu.Unlock()
	require.Len(t, repo.upserted, 1)
	assert.Equal(t, userID, repo.upserted[0].UserID)
	assert.Equal(t, "fcm-token-abc", repo.upserted[0].Token)
	assert.Equal(t, schemas.PlatformAndroid, repo.upserted[0].Platform)
}

func TestDeviceToken_Register_AllPlatforms(t *testing.T) {
	cases := []schemas.Platform{schemas.PlatformAndroid, schemas.PlatformIOS, schemas.PlatformWeb}
	for _, p := range cases {
		repo := &fakeTokenRepo{}
		svc := service.NewDeviceTokenService(repo)

		err := svc.Register(context.Background(), &schemas.RegisterDeviceTokenRequest{
			UserID:   uuid.New().String(),
			Token:    "tok",
			Platform: p,
		})
		require.NoError(t, err, "platform %s", p)

		repo.mu.Lock()
		assert.Equal(t, p, repo.upserted[0].Platform, "platform %s", p)
		repo.mu.Unlock()
	}
}

func TestDeviceToken_Deregister_DeleteCalled(t *testing.T) {
	repo := &fakeTokenRepo{}
	svc := service.NewDeviceTokenService(repo)

	userID := uuid.New()
	require.NoError(t, svc.Deregister(context.Background(), &schemas.DeregisterDeviceTokenRequest{
		UserID: userID.String(),
		Token:  "fcm-token-xyz",
	}))

	repo.mu.Lock()
	defer repo.mu.Unlock()
	require.Len(t, repo.deleted, 1)
	assert.Equal(t, userID, repo.deleted[0].userID)
	assert.Equal(t, "fcm-token-xyz", repo.deleted[0].token)
}
