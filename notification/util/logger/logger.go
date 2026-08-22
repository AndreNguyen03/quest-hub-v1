// Package logger configures the global zerolog logger with lumberjack
// file rotation and provides a GORM logger adapter.
package logger

import (
	"context"
	"errors"
	"io"
	"os"
	"path/filepath"
	"time"

	"github.com/rs/zerolog"
	"gopkg.in/natefinch/lumberjack.v2"
	"gorm.io/gorm"
	gormlogger "gorm.io/gorm/logger"
)

// Log is the process-wide zerolog logger. Configure with Setup before use.
var Log = zerolog.New(os.Stdout).With().Timestamp().Logger()

// Setup initialises the global logger: pretty console output plus a rotated
// file at logFilePath (10 MB per file, 3 backups, 28 days retention).
func Setup(logFilePath string) {
	if err := os.MkdirAll(filepath.Dir(logFilePath), 0o755); err != nil {
		Log.Warn().Err(err).Msg("cannot create log directory, file logging disabled")
	}

	console := zerolog.ConsoleWriter{Out: os.Stdout, TimeFormat: time.RFC3339}
	file := &lumberjack.Logger{
		Filename:   logFilePath,
		MaxSize:    10,
		MaxBackups: 3,
		MaxAge:     28,
		Compress:   true,
	}
	var writer io.Writer = console
	if dir := filepath.Dir(logFilePath); dir != "" && dir != "." {
		writer = io.MultiWriter(console, file)
	}
	Log = zerolog.New(writer).With().Timestamp().Str("service", "notification").Logger()
}

const (
	slowQueryThreshold = 200 * time.Millisecond
	gormCallerDepth    = 2
)

// NewGormLogger returns a GORM logger.Interface that routes SQL logs to
// the global zerolog logger.
func NewGormLogger() gormlogger.Interface {
	return gormZerolog{level: gormlogger.Warn}
}

type gormZerolog struct {
	level gormlogger.LogLevel
}

// LogMode adjusts the verbosity of GORM SQL logging.
func (l gormZerolog) LogMode(level gormlogger.LogLevel) gormlogger.Interface {
	l.level = level
	return l
}

// Info logs informational GORM messages.
func (l gormZerolog) Info(_ context.Context, msg string, data ...any) {
	Log.Info().Msgf(msg, data...)
}

// Warn logs warning GORM messages.
func (l gormZerolog) Warn(_ context.Context, msg string, data ...any) {
	Log.Warn().Msgf(msg, data...)
}

// Error logs error GORM messages.
func (l gormZerolog) Error(_ context.Context, msg string, data ...any) {
	Log.Error().Msgf(msg, data...)
}

// Trace logs executed SQL — errors above Warn level, slow queries as warnings.
func (l gormZerolog) Trace(_ context.Context, begin time.Time, fc func() (string, int64), err error) {
	if l.level <= gormlogger.Silent {
		return
	}
	elapsed := time.Since(begin)
	sql, rows := fc()

	switch {
	case err != nil && l.level >= gormlogger.Error && !errors.Is(err, gorm.ErrRecordNotFound):
		Log.Error().Err(err).Str("sql", sql).Int64("rows", rows).Dur("elapsed", elapsed).Msg("gorm query failed")
	case elapsed > slowQueryThreshold && l.level >= gormlogger.Warn:
		Log.Warn().Str("sql", sql).Int64("rows", rows).Dur("elapsed", elapsed).Msg("slow query")
	default:
		Log.Debug().Str("sql", sql).Int64("rows", rows).Dur("elapsed", elapsed).Msg("query")
	}
}

var _ gormlogger.Interface = gormZerolog{}
