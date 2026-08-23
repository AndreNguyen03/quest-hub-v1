// Package router registers all routes and wires controllers into Gin.
package router

import (
	"net/http"

	"questhub/social/controller"
	"questhub/social/helper"

	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
)

// APIServer holds the controller container and builds the Gin engine.
type APIServer struct{ Ctrls *controller.ControllerContainer }

func NewAPIServer(ctrls *controller.ControllerContainer) *APIServer { return &APIServer{Ctrls: ctrls} }

// SetupRouter builds the Gin engine and registers every route group.
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

	api := r.Group("/api/v1")

	// Feed
	api.GET("/feed", s.Ctrls.Activity.Feed)

	// User activities & follows
	users := api.Group("/users")
	users.GET("/:username/activities", s.Ctrls.Activity.ListByUser)
	users.POST("/:username/follow", s.Ctrls.Follow.Follow)
	users.DELETE("/:username/follow", s.Ctrls.Follow.Unfollow)
	users.GET("/me/following", s.Ctrls.Follow.ListFollowing)
	users.GET("/:username/followers", s.Ctrls.Follow.ListFollowers)

	// Quest comments & discussions
	quests := api.Group("/quests")
	quests.GET("/:id/comments", s.Ctrls.Comment.List)
	quests.POST("/:id/comments", s.Ctrls.Comment.Create)
	quests.GET("/:id/discussions", s.Ctrls.Discussion.ListByQuest)
	quests.POST("/:id/discussions", s.Ctrls.Discussion.Create)

	// Discussion thread replies
	discussions := api.Group("/discussions")
	discussions.GET("/:id/comments", s.Ctrls.Discussion.ListReplies)
	discussions.POST("/:id/comments", s.Ctrls.Discussion.Reply)

	return r
}
