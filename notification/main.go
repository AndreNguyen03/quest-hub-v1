// Command notification is the QuestHub notification side-app: it consumes the
// Java monolith's transactional outbox and serves the notifications inbox API.
//
//	@title                      Notification Service API
//	@version                    1.0
//	@description                Inbox API + transactional-outbox consumer của QuestHub notification side-app.
//	@BasePath                   /
//	@query.collection.format    multi
package main

import (
	"context"
	"errors"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"notification/controller"
	_ "notification/docs" // Swagger generated docs
	"notification/infra/db"
	"notification/infra/email"
	"notification/infra/push"
	"notification/infra/sse"
	"notification/infra/worker"
	"notification/repository"
	"notification/router"
	"notification/service"
	"notification/util/config"
	"notification/util/logger"
)

// init pins the whole app to UTC timezone.
func init() {
	time.Local = time.UTC
}

func main() {
	cfg := config.Load()
	logger.Setup(cfg.LogFilePath)
	log := logger.Log

	psql, err := db.NewPostgres(cfg)
	if err != nil {
		log.Error().Err(err).Msg("db connect failed")
		os.Exit(1)
	}
	defer psql.Close()

	// Infrastructure
	sseHub := sse.NewHub()
	fcmClient := push.NewFCMClient(cfg.FCMCredentialsPath)
	mailer := email.NewMailer(cfg.SMTPHost, cfg.SMTPPort, cfg.SMTPUsername, cfg.SMTPPassword, cfg.SMTPFrom)

	// Wire layers
	repos := repository.NewRepositoryFactory(psql).CreateRepositories()
	services := service.NewServiceFactory(repos, sseHub, fcmClient, mailer).CreateServices()
	ctrls := controller.NewControllerFactory(services, sseHub).CreateControllers()
	server := router.NewAPIServer(ctrls)

	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stop()

	eventHandler := worker.NewEventHandler(services.Notification, repos.UserEmail)
	go worker.NewOutboxWorker(psql.DB, eventHandler, cfg.PollInterval).Run(ctx)

	srv := &http.Server{Addr: ":" + cfg.Port, Handler: server.SetupRouter()}

	go func() {
		log.Info().Str("port", cfg.Port).Msg("http server listening")
		if err := srv.ListenAndServe(); err != nil && !errors.Is(err, http.ErrServerClosed) {
			log.Error().Err(err).Msg("http server error")
			stop()
		}
	}()

	<-ctx.Done()
	log.Info().Msg("shutting down")

	shutdownCtx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	if err := srv.Shutdown(shutdownCtx); err != nil {
		log.Error().Err(err).Msg("graceful shutdown failed")
	}
}
