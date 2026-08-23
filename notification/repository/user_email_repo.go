package repository

import (
	"context"
	"errors"

	"notification/schemas"

	"github.com/google/uuid"
	"gorm.io/gorm"
	"gorm.io/gorm/clause"
)

// IUserEmailRepository caches user email addresses for email notification delivery.
type IUserEmailRepository interface {
	Upsert(ctx context.Context, userID uuid.UUID, email string) error
	FindByUser(ctx context.Context, userID uuid.UUID) (string, error)
}

// UserEmailRepository implements IUserEmailRepository on PostgreSQL via GORM.
type UserEmailRepository struct {
	db *gorm.DB
}

// NewUserEmailRepository returns a repository bound to the given DB handle.
func NewUserEmailRepository(db *gorm.DB) *UserEmailRepository {
	return &UserEmailRepository{db: db}
}

// Upsert inserts or updates the email for a user.
func (r *UserEmailRepository) Upsert(ctx context.Context, userID uuid.UUID, email string) error {
	return r.db.WithContext(ctx).
		Clauses(clause.OnConflict{
			Columns:   []clause.Column{{Name: "user_id"}},
			DoUpdates: clause.AssignmentColumns([]string{"email"}),
		}).
		Create(&schemas.UserEmail{UserID: userID, Email: email}).Error
}

// FindByUser returns the cached email for the user, or ("", nil) if not found.
func (r *UserEmailRepository) FindByUser(ctx context.Context, userID uuid.UUID) (string, error) {
	var ue schemas.UserEmail
	err := r.db.WithContext(ctx).Where("user_id = ?", userID).First(&ue).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return "", nil
	}
	return ue.Email, err
}
