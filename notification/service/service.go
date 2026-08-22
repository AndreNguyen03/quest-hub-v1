package service

import "questhub/notification/repository"

// ServiceContainer aggregates every service owned by this service.
// Adding a new module = add a field here + register it in CreateServices.
type ServiceContainer struct {
	Notification INotificationService
}

// ServiceFactory injects repositories into services.
type ServiceFactory struct {
	repos *repository.RepositoryContainer
}

// NewServiceFactory returns a factory wired to the given repository container.
func NewServiceFactory(repos *repository.RepositoryContainer) *ServiceFactory {
	return &ServiceFactory{repos: repos}
}

// CreateServices instantiates and wires every service.
func (f *ServiceFactory) CreateServices() *ServiceContainer {
	return &ServiceContainer{
		Notification: NewNotificationService(f.repos.Notification),
	}
}
