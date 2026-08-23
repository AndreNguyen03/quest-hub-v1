package service

import (
	"questhub/notification/infra/email"
	"questhub/notification/infra/push"
	"questhub/notification/infra/sse"
	"questhub/notification/repository"
)

// ServiceContainer aggregates every service owned by this service.
// Adding a new module = add a field here + register it in CreateServices.
type ServiceContainer struct {
	Notification INotificationService
	DeviceToken  IDeviceTokenService
}

// ServiceFactory injects repositories and infrastructure into services.
type ServiceFactory struct {
	repos  *repository.RepositoryContainer
	hub    *sse.Hub
	fcm    *push.FCMClient
	mailer *email.Mailer
}

// NewServiceFactory returns a factory wired to the given dependencies.
func NewServiceFactory(
	repos *repository.RepositoryContainer,
	hub *sse.Hub,
	fcm *push.FCMClient,
	mailer *email.Mailer,
) *ServiceFactory {
	return &ServiceFactory{repos: repos, hub: hub, fcm: fcm, mailer: mailer}
}

// CreateServices instantiates and wires every service.
func (f *ServiceFactory) CreateServices() *ServiceContainer {
	return &ServiceContainer{
		Notification: NewNotificationService(
			f.repos.Notification,
			f.repos.DeviceToken,
			f.repos.UserEmail,
			f.hub,
			f.fcm,
			f.mailer,
		),
		DeviceToken: NewDeviceTokenService(f.repos.DeviceToken),
	}
}
