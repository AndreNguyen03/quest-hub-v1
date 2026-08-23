package controller

import (
	"net/http"

	"questhub/social/helper"
	"questhub/social/service"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
)

// FollowController handles follow/unfollow and follower list endpoints.
type FollowController struct{ svc service.IFollowService }

func NewFollowController(svc service.IFollowService) *FollowController {
	return &FollowController{svc: svc}
}

// parseQueryUUID extracts a required UUID from the query string.
func parseQueryUUID(c *gin.Context, key string) (uuid.UUID, bool) {
	raw := c.Query(key)
	id, err := uuid.Parse(raw)
	if err != nil {
		helper.BadRequest(c, "parse "+key+" failed", key+" không hợp lệ")
		return uuid.Nil, false
	}
	return id, true
}

// Follow godoc
// @Summary      Follow a user
// @Tags         follows
// @Param        username   path   string  true  "Target username"
// @Param        followerId query  string  true  "Follower user ID (UUID)"
// @Success      204
// @Failure      400  {object}  helper.ErrorResponse
// @Router       /api/v1/users/{username}/follow [post]
func (ctrl *FollowController) Follow(c *gin.Context) {
	followerID, ok := parseQueryUUID(c, "followerId")
	if !ok {
		return
	}
	followeeID, ok := parseQueryUUID(c, "followeeId")
	if !ok {
		return
	}
	if err := ctrl.svc.Follow(c.Request.Context(), followerID, followeeID); err != nil {
		helper.BadRequest(c, err.Error(), err.Error())
		return
	}
	c.Status(http.StatusNoContent)
}

// Unfollow godoc
// @Summary      Unfollow a user
// @Tags         follows
// @Param        username   path   string  true  "Target username"
// @Param        followerId query  string  true  "Follower user ID (UUID)"
// @Success      204
// @Router       /api/v1/users/{username}/follow [delete]
func (ctrl *FollowController) Unfollow(c *gin.Context) {
	followerID, ok := parseQueryUUID(c, "followerId")
	if !ok {
		return
	}
	followeeID, ok := parseQueryUUID(c, "followeeId")
	if !ok {
		return
	}
	if err := ctrl.svc.Unfollow(c.Request.Context(), followerID, followeeID); err != nil {
		helper.InternalError(c, err, "unfollow failed")
		return
	}
	c.Status(http.StatusNoContent)
}

// ListFollowing godoc
// @Summary      List who a user is following
// @Tags         follows
// @Param        userId  query  string  true  "User ID (UUID)"
// @Param        page    query  int     false "Page"
// @Param        limit   query  int     false "Limit"
// @Success      200  {array}  schemas.Follow
// @Router       /api/v1/users/me/following [get]
func (ctrl *FollowController) ListFollowing(c *gin.Context) {
	userID, ok := parseQueryUUID(c, "userId")
	if !ok {
		return
	}
	page, limit := pageLimit(c)
	items, err := ctrl.svc.ListFollowing(c.Request.Context(), userID, page, limit)
	if err != nil {
		helper.InternalError(c, err, "list following failed")
		return
	}
	helper.GinResponse(c, http.StatusOK, items)
}

// ListFollowers godoc
// @Summary      List a user's followers
// @Tags         follows
// @Param        userId  query  string  true  "User ID (UUID)"
// @Success      200  {array}  schemas.Follow
// @Router       /api/v1/users/{username}/followers [get]
func (ctrl *FollowController) ListFollowers(c *gin.Context) {
	userID, ok := parseQueryUUID(c, "userId")
	if !ok {
		return
	}
	page, limit := pageLimit(c)
	items, err := ctrl.svc.ListFollowers(c.Request.Context(), userID, page, limit)
	if err != nil {
		helper.InternalError(c, err, "list followers failed")
		return
	}
	helper.GinResponse(c, http.StatusOK, items)
}
