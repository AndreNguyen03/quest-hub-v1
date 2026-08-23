package repository

import (
	"context"

	"questhub/notification/schemas"

	"github.com/google/uuid"
	"gorm.io/gorm"
	"gorm.io/gorm/clause"
)

// IDeviceTokenRepository defines data access for FCM device tokens.
type IDeviceTokenRepository interface {
	Upsert(ctx context.Context, dt *schemas.DeviceToken) error
	ListByUser(ctx context.Context, userID uuid.UUID) ([]schemas.DeviceToken, error)
	Delete(ctx context.Context, userID uuid.UUID, token string) error
}

// DeviceTokenRepository implements IDeviceTokenRepository on PostgreSQL via GORM.
type DeviceTokenRepository struct {
	db *gorm.DB
}

// NewDeviceTokenRepository returns a repository bound to the given DB handle.
func NewDeviceTokenRepository(db *gorm.DB) *DeviceTokenRepository {
	return &DeviceTokenRepository{db: db}
}

// Upsert inserts or updates a device token (unique on token column).
func (r *DeviceTokenRepository) Upsert(ctx context.Context, dt *schemas.DeviceToken) error {
	return r.db.WithContext(ctx).
		Clauses(clause.OnConflict{
			Columns:   []clause.Column{{Name: "token"}},
			DoUpdates: clause.AssignmentColumns([]string{"user_id", "platform", "created_at"}),
		}).
		Create(dt).Error
}

// ListByUser returns all device tokens registered for the given user.
func (r *DeviceTokenRepository) ListByUser(ctx context.Context, userID uuid.UUID) ([]schemas.DeviceToken, error) {
	var results []schemas.DeviceToken
	err := r.db.WithContext(ctx).Where("user_id = ?", userID).Find(&results).Error
	return results, err
}

// Delete removes a specific token for the given user.
func (r *DeviceTokenRepository) Delete(ctx context.Context, userID uuid.UUID, token string) error {
	return r.db.WithContext(ctx).
		Where("user_id = ? AND token = ?", userID, token).
		Delete(&schemas.DeviceToken{}).Error
}
