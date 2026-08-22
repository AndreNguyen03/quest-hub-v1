// Package controller contains HTTP handlers: bind JSON/query, validate,
// call the service layer, respond via helpers.
package controller

import "questhub/notification/service"

// ControllerContainer aggregates every controller owned by this service.
// Adding a new module = add a field here + register it in CreateControllers.
type ControllerContainer struct {
	Notification *NotificationController
}

// ControllerFactory injects services into controllers.
type ControllerFactory struct {
	services *service.ServiceContainer
}

// NewControllerFactory returns a factory wired to the given service container.
func NewControllerFactory(services *service.ServiceContainer) *ControllerFactory {
	return &ControllerFactory{services: services}
}

// CreateControllers instantiates and wires every controller.
func (f *ControllerFactory) CreateControllers() *ControllerContainer {
	return &ControllerContainer{
		Notification: NewNotificationController(f.services.Notification),
	}
}
