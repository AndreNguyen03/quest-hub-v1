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

	"questhub/notification/controller"
	_ "questhub/notification/docs" // Swagger generated docs
	"questhub/notification/infra/db"
	"questhub/notification/infra/worker"
	"questhub/notification/repository"
	"questhub/notification/router"
	"questhub/notification/service"
	"questhub/notification/util/config"
	"questhub/notification/util/logger"
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

	repos := repository.NewRepositoryFactory(psql).CreateRepositories()
	services := service.NewServiceFactory(repos).CreateServices()
	ctrls := controller.NewControllerFactory(services).CreateControllers()
	server := router.NewAPIServer(ctrls)

	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stop()

	eventHandler := worker.NewEventHandler(repos.Notification)
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
