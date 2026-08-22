// Package db contains database infrastructure for the notification service.
package db

import (
	"fmt"
	"time"

	"questhub/notification/util/config"
	"questhub/notification/util/logger"

	"gorm.io/driver/postgres"
	"gorm.io/gorm"
)

// Postgres wraps the shared *gorm.DB used by all repositories and workers.
type Postgres struct {
	DB *gorm.DB
}

// NewPostgres opens a GORM connection pool to PostgreSQL and verifies it
// with a ping.
func NewPostgres(cfg config.Config) (*Postgres, error) {
	gdb, err := gorm.Open(postgres.Open(cfg.DBURL), &gorm.Config{
		Logger: logger.NewGormLogger(),
	})
	if err != nil {
		return nil, fmt.Errorf("gorm open: %w", err)
	}

	sqlDB, err := gdb.DB()
	if err != nil {
		return nil, fmt.Errorf("sql db handle: %w", err)
	}
	sqlDB.SetMaxOpenConns(25)
	sqlDB.SetMaxIdleConns(5)
	sqlDB.SetConnMaxLifetime(time.Hour)
	if err := sqlDB.Ping(); err != nil {
		return nil, fmt.Errorf("db ping: %w", err)
	}
	return &Postgres{DB: gdb}, nil
}

// Close releases all pool connections.
func (p *Postgres) Close() {
	if sqlDB, err := p.DB.DB(); err == nil {
		_ = sqlDB.Close()
	}
}
