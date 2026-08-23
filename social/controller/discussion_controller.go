package controller

import (
	"net/http"

	"social/helper"
	"social/schemas"
	"social/service"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
)

// DiscussionController handles discussion thread endpoints.
type DiscussionController struct{ svc service.IDiscussionService }

func NewDiscussionController(svc service.IDiscussionService) *DiscussionController {
	return &DiscussionController{svc: svc}
}

// Create godoc
// @Summary      Create a discussion on a quest
// @Tags         discussions
// @Accept       json
// @Produce      json
// @Param        id    path  string                          true  "Quest ID"
// @Param        body  body  schemas.CreateDiscussionRequest true  "Discussion payload"
// @Success      201   {object}  schemas.Discussion
// @Failure      400   {object}  helper.ErrorResponse
// @Router       /api/v1/quests/{id}/discussions [post]
func (ctrl *DiscussionController) Create(c *gin.Context) {
	questID, err := uuid.Parse(c.Param("id"))
	if err != nil {
		helper.BadRequest(c, "parse questId failed", "questId không hợp lệ")
		return
	}

	var req schemas.CreateDiscussionRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		helper.BadRequest(c, "bind discussion failed", "Dữ liệu không hợp lệ")
		return
	}

	d, err := ctrl.svc.Create(c.Request.Context(), questID, &req)
	if err != nil {
		helper.BadRequest(c, err.Error(), err.Error())
		return
	}
	helper.GinResponse(c, http.StatusCreated, d)
}

// ListByQuest godoc
// @Summary      List discussions for a quest
// @Tags         discussions
// @Param        id  path  string  true  "Quest ID"
// @Success      200  {object}  schemas.ListDiscussionsResponse
// @Router       /api/v1/quests/{id}/discussions [get]
func (ctrl *DiscussionController) ListByQuest(c *gin.Context) {
	questID, err := uuid.Parse(c.Param("id"))
	if err != nil {
		helper.BadRequest(c, "parse questId failed", "questId không hợp lệ")
		return
	}

	resp, err := ctrl.svc.ListByQuest(c.Request.Context(), questID)
	if err != nil {
		helper.InternalError(c, err, "list discussions failed")
		return
	}
	helper.GinResponse(c, http.StatusOK, resp)
}

// Reply godoc
// @Summary      Reply to a discussion
// @Tags         discussions
// @Accept       json
// @Produce      json
// @Param        id    path  string                        true  "Quest ID"
// @Param        body  body  schemas.ReplyDiscussionRequest true  "Reply payload (parentPath required)"
// @Success      201   {object}  schemas.Discussion
// @Router       /api/v1/discussions/{id}/comments [post]
func (ctrl *DiscussionController) Reply(c *gin.Context) {
	questID, err := uuid.Parse(c.Param("id"))
	if err != nil {
		helper.BadRequest(c, "parse questId failed", "questId không hợp lệ")
		return
	}

	var req schemas.ReplyDiscussionRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		helper.BadRequest(c, "bind reply failed", "Dữ liệu không hợp lệ")
		return
	}

	d, err := ctrl.svc.Reply(c.Request.Context(), questID, &req)
	if err != nil {
		helper.BadRequest(c, err.Error(), err.Error())
		return
	}
	helper.GinResponse(c, http.StatusCreated, d)
}

// ListReplies godoc
// @Summary      List replies under a discussion
// @Tags         discussions
// @Param        id         path   string  true  "Quest ID"
// @Param        rootPath   query  string  true  "Root discussion path"
// @Success      200  {object}  schemas.ListDiscussionsResponse
// @Router       /api/v1/discussions/{id}/comments [get]
func (ctrl *DiscussionController) ListReplies(c *gin.Context) {
	rootPath := c.Query("rootPath")
	if rootPath == "" {
		helper.BadRequest(c, "rootPath required", "rootPath không được để trống")
		return
	}
	resp, err := ctrl.svc.ListReplies(c.Request.Context(), rootPath)
	if err != nil {
		helper.InternalError(c, err, "list replies failed")
		return
	}
	helper.GinResponse(c, http.StatusOK, resp)
}
