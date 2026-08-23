package controller

import (
	"net/http"

	"questhub/social/helper"
	"questhub/social/schemas"
	"questhub/social/service"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
)

// CommentController handles quest comment endpoints.
type CommentController struct{ svc service.ICommentService }

func NewCommentController(svc service.ICommentService) *CommentController {
	return &CommentController{svc: svc}
}

// Create godoc
// @Summary      Add a comment to a quest
// @Description  Tạo root comment hoặc reply (tối đa 2 cấp). parentPath rỗng = root comment.
// @Tags         comments
// @Accept       json
// @Produce      json
// @Param        id    path  string                        true  "Quest ID"
// @Param        body  body  schemas.CreateCommentRequest  true  "Comment payload"
// @Success      201   {object}  schemas.Comment
// @Failure      400   {object}  helper.ErrorResponse
// @Router       /api/v1/quests/{id}/comments [post]
func (ctrl *CommentController) Create(c *gin.Context) {
	questID, err := uuid.Parse(c.Param("id"))
	if err != nil {
		helper.BadRequest(c, "parse questId failed", "questId không hợp lệ")
		return
	}

	var req schemas.CreateCommentRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		helper.BadRequest(c, "bind comment failed", "Dữ liệu không hợp lệ")
		return
	}

	comment, err := ctrl.svc.Create(c.Request.Context(), questID, &req)
	if err != nil {
		helper.BadRequest(c, err.Error(), err.Error())
		return
	}
	helper.GinResponse(c, http.StatusCreated, comment)
}

// List godoc
// @Summary      List comments for a quest
// @Description  Trả về tất cả comment theo thứ tự depth-first (ORDER BY path)
// @Tags         comments
// @Param        id  path  string  true  "Quest ID"
// @Success      200  {object}  schemas.ListCommentsResponse
// @Router       /api/v1/quests/{id}/comments [get]
func (ctrl *CommentController) List(c *gin.Context) {
	questID, err := uuid.Parse(c.Param("id"))
	if err != nil {
		helper.BadRequest(c, "parse questId failed", "questId không hợp lệ")
		return
	}

	resp, err := ctrl.svc.ListByQuest(c.Request.Context(), questID)
	if err != nil {
		helper.InternalError(c, err, "list comments failed")
		return
	}
	helper.GinResponse(c, http.StatusOK, resp)
}
