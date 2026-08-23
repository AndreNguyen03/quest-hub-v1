package schemas

import "github.com/google/uuid"

// UserEmail caches a user's email address, populated from the user.registered
// outbox event so the notification service stays decoupled from identity.
type UserEmail struct {
	UserID uuid.UUID `gorm:"type:uuid;primaryKey;column:user_id"`
	Email  string    `gorm:"column:email"`
}

// TableName overrides GORM table name.
func (UserEmail) TableName() string { return "notification_user_emails" }
