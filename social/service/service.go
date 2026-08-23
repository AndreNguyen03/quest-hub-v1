package service

import (
	"context"

	"questhub/social/repository"
)

const (
	defaultPage  = 1
	defaultLimit = 20
	maxLimit     = 100
)

// IOutboxPublisher writes outbox events so downstream services (e.g. notification)
// can consume them. The social service is both a consumer AND a producer.
type IOutboxPublisher interface {
	Publish(ctx context.Context, eventType string, payload map[string]any) error
}

// normalizePage applies pagination defaults and caps.
func normalizePage(page, limit int) (int, int) {
	if page < 1 {
		page = defaultPage
	}
	if limit <= 0 {
		limit = defaultLimit
	}
	if limit > maxLimit {
		limit = maxLimit
	}
	return page, limit
}

// ServiceContainer aggregates every service owned by the social service.
type ServiceContainer struct {
	Follow     IFollowService
	Activity   IActivityService
	Comment    ICommentService
	Discussion IDiscussionService
}

// ServiceFactory injects repositories and outbox publisher into services.
type ServiceFactory struct {
	repos  *repository.RepositoryContainer
	outbox IOutboxPublisher
}

func NewServiceFactory(repos *repository.RepositoryContainer, outbox IOutboxPublisher) *ServiceFactory {
	return &ServiceFactory{repos: repos, outbox: outbox}
}

// CreateServices instantiates and wires every service.
func (f *ServiceFactory) CreateServices() *ServiceContainer {
	return &ServiceContainer{
		Follow:     NewFollowService(f.repos.Follow, f.outbox),
		Activity:   NewActivityService(f.repos.Activity),
		Comment:    NewCommentService(f.repos.Comment, f.outbox),
		Discussion: NewDiscussionService(f.repos.Discussion, f.outbox),
	}
}
