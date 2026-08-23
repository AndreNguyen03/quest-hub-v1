package repository

import (
	"notification/infra/db"
)

// RepositoryContainer aggregates every repository owned by this service.
// Adding a new module = add a field here + register it in CreateRepositories.
type RepositoryContainer struct {
	Notification INotificationRepository
	DeviceToken  IDeviceTokenRepository
	UserEmail    IUserEmailRepository
}

// RepositoryFactory builds repositories from shared infrastructure dependencies.
type RepositoryFactory struct {
	psql *db.Postgres
}

// NewRepositoryFactory returns a factory bound to the given database handle.
func NewRepositoryFactory(psql *db.Postgres) *RepositoryFactory {
	return &RepositoryFactory{psql: psql}
}

// CreateRepositories instantiates and wires every repository.
func (f *RepositoryFactory) CreateRepositories() *RepositoryContainer {
	return &RepositoryContainer{
		Notification: NewNotificationRepository(f.psql.DB),
		DeviceToken:  NewDeviceTokenRepository(f.psql.DB),
		UserEmail:    NewUserEmailRepository(f.psql.DB),
	}
}
