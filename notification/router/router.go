// Package router registers all routes and wires controllers into Gin.
package router

import (
	"net/http"

	"questhub/notification/controller"
	"questhub/notification/helper"

	"github.com/gin-gonic/gin"
	swaggerFiles "github.com/swaggo/files"
	ginSwagger "github.com/swaggo/gin-swagger"

	"github.com/gin-contrib/cors"
)

// APIServer holds the controller container and builds the Gin engine.
type APIServer struct {
	Ctrls *controller.ControllerContainer
}

// NewAPIServer returns a server wired to the given controller container.
func NewAPIServer(ctrls *controller.ControllerContainer) *APIServer {
	return &APIServer{Ctrls: ctrls}
}

// SetupRouter builds the Gin engine and registers every route group:
// CORS middleware, health check, Swagger UI and the /api/v1 group.
func (s *APIServer) SetupRouter() *gin.Engine {
	r := gin.Default()
	r.Use(cors.New(cors.Config{
		AllowAllOrigins: true,
		AllowMethods:    []string{"GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"},
		AllowHeaders:    []string{"Origin", "Content-Type", "Authorization"},
	}))

	r.GET("/health_check", func(c *gin.Context) {
		helper.GinResponse(c, http.StatusOK, gin.H{"error": false, "message": "ok"})
	})
	r.GET("/swagger/*any", ginSwagger.WrapHandler(swaggerFiles.Handler))

	api := r.Group("/api/v1")
	SetupNotificationRouter(api, s)
	SetupDeviceTokenRouter(api, s)

	return r
}
