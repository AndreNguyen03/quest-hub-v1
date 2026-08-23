package schemas

import (
	"time"

	"github.com/google/uuid"
)

// Platform enumerates supported push notification platforms.
type Platform string

const (
	PlatformAndroid Platform = "ANDROID"
	PlatformIOS     Platform = "IOS"
	PlatformWeb     Platform = "WEB"
)

// DeviceToken stores an FCM registration token for a user's device.
type DeviceToken struct {
	ID        uuid.UUID `gorm:"type:uuid;primaryKey;column:id" json:"id"`
	UserID    uuid.UUID `gorm:"type:uuid;index;column:user_id" json:"userId"`
	Token     string    `gorm:"uniqueIndex;column:token" json:"token"`
	Platform  Platform  `gorm:"type:text;column:platform" json:"platform"`
	CreatedAt time.Time `gorm:"column:created_at" json:"createdAt"`
}

// TableName overrides GORM table name.
func (DeviceToken) TableName() string { return "device_tokens" }

// RegisterDeviceTokenRequest is the body for POST /api/v1/device-tokens.
type RegisterDeviceTokenRequest struct {
	UserID   string   `json:"userId" binding:"required,uuid"`
	Token    string   `json:"token" binding:"required"`
	Platform Platform `json:"platform" binding:"required,oneof=ANDROID IOS WEB"`
}

// DeregisterDeviceTokenRequest is the body for DELETE /api/v1/device-tokens.
type DeregisterDeviceTokenRequest struct {
	UserID string `json:"userId" binding:"required,uuid"`
	Token  string `json:"token" binding:"required"`
}
