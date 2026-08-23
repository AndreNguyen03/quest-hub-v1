// Package controller contains HTTP handlers: bind JSON/query, validate,
// call the service layer, respond via helpers.
package controller

import (
	"questhub/notification/infra/sse"
	"questhub/notification/service"
)

// ControllerContainer aggregates every controller owned by this service.
// Adding a new module = add a field here + register it in CreateControllers.
type ControllerContainer struct {
	Notification *NotificationController
	DeviceToken  *DeviceTokenController
}

// ControllerFactory injects services into controllers.
type ControllerFactory struct {
	services *service.ServiceContainer
	hub      *sse.Hub
}

// NewControllerFactory returns a factory wired to the given service container and SSE hub.
func NewControllerFactory(services *service.ServiceContainer, hub *sse.Hub) *ControllerFactory {
	return &ControllerFactory{services: services, hub: hub}
}

// CreateControllers instantiates and wires every controller.
func (f *ControllerFactory) CreateControllers() *ControllerContainer {
	return &ControllerContainer{
		Notification: NewNotificationController(f.services.Notification, f.hub),
		DeviceToken:  NewDeviceTokenController(f.services.DeviceToken),
	}
}
