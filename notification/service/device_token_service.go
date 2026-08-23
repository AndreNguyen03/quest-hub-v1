package service

import (
	"context"

	"notification/repository"
	"notification/schemas"

	"github.com/google/uuid"
)

// IDeviceTokenService defines operations for managing FCM device tokens.
type IDeviceTokenService interface {
	Register(ctx context.Context, req *schemas.RegisterDeviceTokenRequest) error
	Deregister(ctx context.Context, req *schemas.DeregisterDeviceTokenRequest) error
}

// DeviceTokenService implements IDeviceTokenService.
type DeviceTokenService struct {
	repo repository.IDeviceTokenRepository
}

// NewDeviceTokenService returns a service backed by the given repository.
func NewDeviceTokenService(repo repository.IDeviceTokenRepository) *DeviceTokenService {
	return &DeviceTokenService{repo: repo}
}

// Register upserts a device token for the user.
func (s *DeviceTokenService) Register(ctx context.Context, req *schemas.RegisterDeviceTokenRequest) error {
	userID, _ := uuid.Parse(req.UserID)
	return s.repo.Upsert(ctx, &schemas.DeviceToken{
		ID:       uuid.New(),
		UserID:   userID,
		Token:    req.Token,
		Platform: req.Platform,
	})
}

// Deregister removes a device token for the user.
func (s *DeviceTokenService) Deregister(ctx context.Context, req *schemas.DeregisterDeviceTokenRequest) error {
	userID, _ := uuid.Parse(req.UserID)
	return s.repo.Delete(ctx, userID, req.Token)
}
