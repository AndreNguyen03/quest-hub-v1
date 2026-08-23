// Command social is the QuestHub social side-app: it manages follows,
// activity feed, comments and discussions.
package main

import (
	"context"
	"errors"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"questhub/social/controller"
	"questhub/social/infra/db"
	"questhub/social/infra/worker"
	"questhub/social/repository"
	"questhub/social/router"
	"questhub/social/service"
	"questhub/social/util/config"
	"questhub/social/util/logger"
)

func init() { time.Local = time.UTC }

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
	outboxPub := worker.NewOutboxPublisher(psql.DB)

	// Wire layers
	repos := repository.NewRepositoryFactory(psql).CreateRepositories()
	services := service.NewServiceFactory(repos, outboxPub).CreateServices()
	ctrls := controller.NewControllerFactory(services).CreateControllers()
	server := router.NewAPIServer(ctrls)

	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stop()

	eventHandler := worker.NewEventHandler(services.Activity)
	go worker.NewOutboxWorker(psql.DB, eventHandler, cfg.PollInterval).Run(ctx)

	srv := &http.Server{Addr: ":" + cfg.Port, Handler: server.SetupRouter()}

	go func() {
		log.Info().Str("port", cfg.Port).Msg("social service listening")
		if err := srv.ListenAndServe(); err != nil && !errors.Is(err, http.ErrServerClosed) {
			log.Error().Err(err).Msg("http server error")
			stop()
		}
	}()

	<-ctx.Done()
	log.Info().Msg("shutting down social service")

	shutdownCtx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	if err := srv.Shutdown(shutdownCtx); err != nil {
		log.Error().Err(err).Msg("graceful shutdown failed")
	}
}
