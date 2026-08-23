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

	// FCM — leave empty to disable push notifications.
	FCMCredentialsPath string // path to Firebase service account JSON

	// SMTP — leave host empty to disable email notifications.
	SMTPHost     string
	SMTPPort     int
	SMTPUsername string
	SMTPPassword string
	SMTPFrom     string // "QuestHub <noreply@questhub.io>"
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
	_ = v.BindEnv("fcm.credentials_path", "FCM_CREDENTIALS_PATH")
	_ = v.BindEnv("smtp.host", "SMTP_HOST")
	_ = v.BindEnv("smtp.port", "SMTP_PORT")
	_ = v.BindEnv("smtp.username", "SMTP_USERNAME")
	_ = v.BindEnv("smtp.password", "SMTP_PASSWORD")
	_ = v.BindEnv("smtp.from", "SMTP_FROM")

	v.SetDefault("database.url", "postgres://questhub:questhub@localhost:5432/questhub")
	v.SetDefault("server.port", "8082")
	v.SetDefault("outbox.poll_interval_secs", 5)
	v.SetDefault("log.file_path", "logs/notification.log")
	v.SetDefault("smtp.port", 587)
	v.SetDefault("smtp.from", "QuestHub <noreply@questhub.io>")

	// Missing app.env is fine — defaults + env vars still work.
	_ = v.ReadInConfig()

	return Config{
		DBURL:              v.GetString("database.url"),
		Port:               v.GetString("server.port"),
		PollInterval:       time.Duration(v.GetInt("outbox.poll_interval_secs")) * time.Second,
		LogFilePath:        v.GetString("log.file_path"),
		FCMCredentialsPath: v.GetString("fcm.credentials_path"),
		SMTPHost:           v.GetString("smtp.host"),
		SMTPPort:           v.GetInt("smtp.port"),
		SMTPUsername:       v.GetString("smtp.username"),
		SMTPPassword:       v.GetString("smtp.password"),
		SMTPFrom:           v.GetString("smtp.from"),
	}
}
