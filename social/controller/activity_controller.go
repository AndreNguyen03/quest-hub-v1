package controller

import (
	"net/http"

	"social/helper"
	"social/service"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
)

// ActivityController handles feed and user-activity endpoints.
type ActivityController struct{ svc service.IActivityService }

func NewActivityController(svc service.IActivityService) *ActivityController {
	return &ActivityController{svc: svc}
}

// Feed godoc
// @Summary      Get activity feed
// @Description  Trả về activities của các user mà followerId đang follow, mới nhất trước
// @Tags         activities
// @Param        followerId  query  string  true   "Viewer user ID (UUID)"
// @Param        page        query  int     false  "Page"
// @Param        limit       query  int     false  "Limit"
// @Success      200  {object}  schemas.FeedResponse
// @Router       /api/v1/feed [get]
func (ctrl *ActivityController) Feed(c *gin.Context) {
	followerID, err := uuid.Parse(c.Query("followerId"))
	if err != nil {
		helper.BadRequest(c, "parse followerId failed", "followerId không hợp lệ")
		return
	}
	page, limit := pageLimit(c)
	resp, err := ctrl.svc.Feed(c.Request.Context(), followerID, page, limit)
	if err != nil {
		helper.InternalError(c, err, "feed failed")
		return
	}
	helper.GinResponse(c, http.StatusOK, resp)
}

// ListByUser godoc
// @Summary      Get a user's public activities
// @Tags         activities
// @Param        userId  query  string  true  "User ID (UUID)"
// @Success      200  {object}  schemas.FeedResponse
// @Router       /api/v1/users/{username}/activities [get]
func (ctrl *ActivityController) ListByUser(c *gin.Context) {
	userID, err := uuid.Parse(c.Query("userId"))
	if err != nil {
		helper.BadRequest(c, "parse userId failed", "userId không hợp lệ")
		return
	}
	page, limit := pageLimit(c)
	resp, err := ctrl.svc.ListByUser(c.Request.Context(), userID, page, limit)
	if err != nil {
		helper.InternalError(c, err, "list user activities failed")
		return
	}
	helper.GinResponse(c, http.StatusOK, resp)
}
