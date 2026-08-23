// Package config loads social service configuration via viper.
package config

import (
	"time"

	"github.com/spf13/viper"
)

// Config holds all runtime configuration for the social service.
type Config struct {
	DBURL        string
	Port         string
	PollInterval time.Duration
	LogFilePath  string
}

// Load reads app.env from the working directory then applies env var overrides.
func Load() Config {
	v := viper.New()
	v.SetConfigName("app")
	v.SetConfigType("env")
	v.AddConfigPath(".")
	v.AutomaticEnv()

	_ = v.BindEnv("database.url", "DATABASE_URL")
	_ = v.BindEnv("server.port", "SOCIAL_PORT")
	_ = v.BindEnv("outbox.poll_interval_secs", "OUTBOX_POLL_INTERVAL_SECS")
	_ = v.BindEnv("log.file_path", "LOG_FILE_PATH")

	v.SetDefault("database.url", "postgres://questhub:questhub@localhost:5432/questhub")
	v.SetDefault("server.port", "8081")
	v.SetDefault("outbox.poll_interval_secs", 5)
	v.SetDefault("log.file_path", "logs/social.log")

	_ = v.ReadInConfig()

	return Config{
		DBURL:        v.GetString("database.url"),
		Port:         v.GetString("server.port"),
		PollInterval: time.Duration(v.GetInt("outbox.poll_interval_secs")) * time.Second,
		LogFilePath:  v.GetString("log.file_path"),
	}
}
