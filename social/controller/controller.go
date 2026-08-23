// Package controller contains HTTP handlers for the social service.
package controller

import (
	"strconv"

	"social/service"

	"github.com/gin-gonic/gin"
)

// ControllerContainer aggregates every controller owned by the social service.
type ControllerContainer struct {
	Follow     *FollowController
	Activity   *ActivityController
	Comment    *CommentController
	Discussion *DiscussionController
}

// ControllerFactory injects services into controllers.
type ControllerFactory struct{ services *service.ServiceContainer }

func NewControllerFactory(services *service.ServiceContainer) *ControllerFactory {
	return &ControllerFactory{services: services}
}

// CreateControllers instantiates and wires every controller.
func (f *ControllerFactory) CreateControllers() *ControllerContainer {
	return &ControllerContainer{
		Follow:     NewFollowController(f.services.Follow),
		Activity:   NewActivityController(f.services.Activity),
		Comment:    NewCommentController(f.services.Comment),
		Discussion: NewDiscussionController(f.services.Discussion),
	}
}

// pageLimit extracts page and limit from query params with safe defaults.
func pageLimit(c *gin.Context) (int, int) {
	page, _ := strconv.Atoi(c.DefaultQuery("page", "1"))
	limit, _ := strconv.Atoi(c.DefaultQuery("limit", "20"))
	return page, limit
}
