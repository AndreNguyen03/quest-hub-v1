// Package logger configures the global zerolog logger with lumberjack rotation.
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

// Log is the process-wide zerolog logger.
var Log = zerolog.New(os.Stdout).With().Timestamp().Logger()

// Setup initialises the global logger with console + rotating file output.
func Setup(logFilePath string) {
	if err := os.MkdirAll(filepath.Dir(logFilePath), 0o755); err != nil {
		Log.Warn().Err(err).Msg("cannot create log directory, file logging disabled")
	}
	console := zerolog.ConsoleWriter{Out: os.Stdout, TimeFormat: time.RFC3339}
	file := &lumberjack.Logger{Filename: logFilePath, MaxSize: 10, MaxBackups: 3, MaxAge: 28, Compress: true}
	writer := io.MultiWriter(console, file)
	Log = zerolog.New(writer).With().Timestamp().Str("service", "social").Logger()
}

const slowQueryThreshold = 200 * time.Millisecond

// NewGormLogger returns a GORM logger that routes to zerolog.
func NewGormLogger() gormlogger.Interface { return gormZerolog{level: gormlogger.Warn} }

type gormZerolog struct{ level gormlogger.LogLevel }

func (l gormZerolog) LogMode(level gormlogger.LogLevel) gormlogger.Interface {
	l.level = level
	return l
}
func (l gormZerolog) Info(_ context.Context, msg string, data ...any)  { Log.Info().Msgf(msg, data...) }
func (l gormZerolog) Warn(_ context.Context, msg string, data ...any)  { Log.Warn().Msgf(msg, data...) }
func (l gormZerolog) Error(_ context.Context, msg string, data ...any) { Log.Error().Msgf(msg, data...) }

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
