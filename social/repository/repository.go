package repository

import "social/infra/db"

// RepositoryContainer aggregates every repository owned by the social service.
type RepositoryContainer struct {
	Follow     IFollowRepository
	Activity   IActivityRepository
	Comment    ICommentRepository
	Discussion IDiscussionRepository
}

// RepositoryFactory builds repositories from shared infrastructure.
type RepositoryFactory struct{ psql *db.Postgres }

func NewRepositoryFactory(psql *db.Postgres) *RepositoryFactory {
	return &RepositoryFactory{psql: psql}
}

// CreateRepositories instantiates and wires every repository.
func (f *RepositoryFactory) CreateRepositories() *RepositoryContainer {
	return &RepositoryContainer{
		Follow:     NewFollowRepository(f.psql.DB),
		Activity:   NewActivityRepository(f.psql.DB),
		Comment:    NewCommentRepository(f.psql.DB),
		Discussion: NewDiscussionRepository(f.psql.DB),
	}
}
