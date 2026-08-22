// Package config loads application configuration via viper from app.env
// with environment variable overrides.
package config

import (
	"time"

	"github.com/spf13/viper"
)

// Config holds all runtime configuration for the notification service.
type Config struct {
	DBURL        string        // PostgreSQL DSN
	Port         string        // HTTP server port
	PollInterval time.Duration // Interval between outbox polls
	LogFilePath  string        // Rotated log file path
}

// Load reads app.env from the working directory when present, applies
// defaults, and lets environment variables override file values.
func Load() Config {
	v := viper.New()
	v.SetConfigName("app")
	v.SetConfigType("env")
	v.AddConfigPath(".")
	v.AutomaticEnv()

	// Environment variable aliases (take precedence over app.env).
	_ = v.BindEnv("database.url", "DATABASE_URL")
	_ = v.BindEnv("server.port", "NOTIFICATION_PORT")
	_ = v.BindEnv("outbox.poll_interval_secs", "OUTBOX_POLL_INTERVAL_SECS")
	_ = v.BindEnv("log.file_path", "LOG_FILE_PATH")

	v.SetDefault("database.url", "postgres://questhub:questhub@localhost:5432/questhub")
	v.SetDefault("server.port", "8082")
	v.SetDefault("outbox.poll_interval_secs", 5)
	v.SetDefault("log.file_path", "logs/notification.log")

	// Missing app.env is fine — defaults + env vars still work.
	_ = v.ReadInConfig()

	return Config{
		DBURL:        v.GetString("database.url"),
		Port:         v.GetString("server.port"),
		PollInterval: time.Duration(v.GetInt("outbox.poll_interval_secs")) * time.Second,
		LogFilePath:  v.GetString("log.file_path"),
	}
}
