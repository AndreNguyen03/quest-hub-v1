package controller

import (
	"encoding/json"
	"fmt"
	"net/http"

	"notification/helper"
	"notification/infra/sse"
	"notification/schemas"
	"notification/service"

	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"
	"github.com/google/uuid"
)

// NotificationController handles HTTP requests for the notifications module.
type NotificationController struct {
	service  service.INotificationService
	hub      *sse.Hub
	validate *validator.Validate
}

// NewNotificationController returns a controller backed by the given service and SSE hub.
func NewNotificationController(svc service.INotificationService, hub *sse.Hub) *NotificationController {
	return &NotificationController{service: svc, hub: hub, validate: validator.New()}
}

// List godoc
// @Summary      Get notification inbox
// @Description  Trả về danh sách thông báo của user, mới nhất trước (page >= 1, limit <= 100)
// @Tags         notifications
// @Accept       json
// @Produce      json
// @Param        userId  query  string  true   "User ID (UUID)"
// @Param        page    query  int     false  "Page number"  minimum(1)  default(1)
// @Param        limit   query  int     false  "Page size"    maximum(100) default(20)
// @Success      200  {object}  schemas.ListNotificationsResponse
// @Failure      400  {object}  helper.ErrorResponse
// @Failure      500  {object}  helper.ErrorResponse
// @Router       /api/v1/notifications [get]
func (ctrl *NotificationController) List(c *gin.Context) {
	var req schemas.ListNotificationsRequest
	if err := c.ShouldBindQuery(&req); err != nil {
		helper.BadRequest(c, "bind list notifications query failed", "Tham số truy vấn không hợp lệ")
		return
	}
	if err := ctrl.validate.Struct(req); err != nil {
		helper.BadRequest(c, "validate list notifications query failed", "Tham số truy vấn không hợp lệ")
		return
	}

	userID, err := uuid.Parse(req.UserID)
	if err != nil {
		helper.BadRequest(c, "parse userId failed", "userId không hợp lệ")
		return
	}

	resp, err := ctrl.service.ListByUser(c.Request.Context(), userID, req.Page, req.Limit)
	if err != nil {
		helper.InternalError(c, err, "list notifications failed")
		return
	}
	helper.GinResponse(c, http.StatusOK, resp)
}

// MarkRead godoc
// @Summary      Mark one notification as read
// @Description  Đánh dấu một thông báo là đã đọc
// @Tags         notifications
// @Accept       json
// @Produce      json
// @Param        id  path  string  true  "Notification ID (UUID)"
// @Success      204  "No Content"
// @Failure      400  {object}  helper.ErrorResponse
// @Failure      500  {object}  helper.ErrorResponse
// @Router       /api/v1/notifications/{id}/read [patch]
func (ctrl *NotificationController) MarkRead(c *gin.Context) {
	id, err := uuid.Parse(c.Param("id"))
	if err != nil {
		helper.BadRequest(c, "parse notification id failed", "id không hợp lệ")
		return
	}

	if err := ctrl.service.MarkRead(c.Request.Context(), id); err != nil {
		helper.InternalError(c, err, "mark notification read failed")
		return
	}
	c.Status(http.StatusNoContent)
}

// MarkAllRead godoc
// @Summary      Mark all notifications as read
// @Description  Đánh dấu tất cả thông báo chưa đọc của user là đã đọc
// @Tags         notifications
// @Accept       json
// @Produce      json
// @Param        userId  query  string  true  "User ID (UUID)"
// @Success      204  "No Content"
// @Failure      400  {object}  helper.ErrorResponse
// @Failure      500  {object}  helper.ErrorResponse
// @Router       /api/v1/notifications/read-all [patch]
func (ctrl *NotificationController) MarkAllRead(c *gin.Context) {
	var req schemas.MarkAllReadRequest
	if err := c.ShouldBindQuery(&req); err != nil {
		helper.BadRequest(c, "bind markAllRead query failed", "Tham số truy vấn không hợp lệ")
		return
	}
	if err := ctrl.validate.Struct(req); err != nil {
		helper.BadRequest(c, "validate markAllRead query failed", "Tham số truy vấn không hợp lệ")
		return
	}

	userID, _ := uuid.Parse(req.UserID)
	if err := ctrl.service.MarkAllRead(c.Request.Context(), userID); err != nil {
		helper.InternalError(c, err, "mark all notifications read failed")
		return
	}
	c.Status(http.StatusNoContent)
}

// UnreadCount godoc
// @Summary      Count unread notifications
// @Description  Số thông báo chưa đọc của user (badge count)
// @Tags         notifications
// @Accept       json
// @Produce      json
// @Param        userId  query  string  true  "User ID (UUID)"
// @Success      200  {object}  schemas.UnreadCountResponse
// @Failure      400  {object}  helper.ErrorResponse
// @Failure      500  {object}  helper.ErrorResponse
// @Router       /api/v1/notifications/unread-count [get]
func (ctrl *NotificationController) UnreadCount(c *gin.Context) {
	var req schemas.UnreadCountRequest
	if err := c.ShouldBindQuery(&req); err != nil {
		helper.BadRequest(c, "bind unreadCount query failed", "Tham số truy vấn không hợp lệ")
		return
	}
	if err := ctrl.validate.Struct(req); err != nil {
		helper.BadRequest(c, "validate unreadCount query failed", "Tham số truy vấn không hợp lệ")
		return
	}

	userID, _ := uuid.Parse(req.UserID)
	resp, err := ctrl.service.UnreadCount(c.Request.Context(), userID)
	if err != nil {
		helper.InternalError(c, err, "count unread notifications failed")
		return
	}
	helper.GinResponse(c, http.StatusOK, resp)
}

// Stream godoc
// @Summary      SSE notification stream
// @Description  Kết nối Server-Sent Events để nhận notification real-time. Giữ connection mở; mỗi event là một JSON notification.
// @Tags         notifications
// @Produce      text/event-stream
// @Param        userId  query  string  true  "User ID (UUID)"
// @Success      200
// @Failure      400  {object}  helper.ErrorResponse
// @Router       /api/v1/notifications/stream [get]
func (ctrl *NotificationController) Stream(c *gin.Context) {
	userIDStr := c.Query("userId")
	userID, err := uuid.Parse(userIDStr)
	if err != nil {
		helper.BadRequest(c, "parse userId failed", "userId không hợp lệ")
		return
	}

	ch := ctrl.hub.Subscribe(userID)
	defer ctrl.hub.Unsubscribe(userID, ch)

	c.Writer.Header().Set("Content-Type", "text/event-stream")
	c.Writer.Header().Set("Cache-Control", "no-cache")
	c.Writer.Header().Set("Connection", "keep-alive")
	c.Writer.Header().Set("X-Accel-Buffering", "no")
	c.Writer.WriteHeader(http.StatusOK)

	// Send an initial ping so the client knows the connection is live.
	fmt.Fprintf(c.Writer, "event: ping\ndata: {}\n\n")
	c.Writer.Flush()

	for {
		select {
		case <-c.Request.Context().Done():
			return
		case n, ok := <-ch:
			if !ok {
				return
			}
			data, _ := json.Marshal(n)
			fmt.Fprintf(c.Writer, "event: notification\ndata: %s\n\n", data)
			c.Writer.Flush()
		}
	}
}

// Broadcast godoc
// @Summary      Admin broadcast notification
// @Description  Gửi thông báo hệ thống tới danh sách userIds được chỉ định
// @Tags         notifications
// @Accept       json
// @Produce      json
// @Param        body  body  schemas.BroadcastRequest  true  "Broadcast payload"
// @Success      204   "No Content"
// @Failure      400   {object}  helper.ErrorResponse
// @Failure      500   {object}  helper.ErrorResponse
// @Router       /api/v1/notifications/broadcast [post]
func (ctrl *NotificationController) Broadcast(c *gin.Context) {
	var req schemas.BroadcastRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		helper.BadRequest(c, "bind broadcast failed", "Dữ liệu không hợp lệ")
		return
	}
	if len(req.UserIDs) == 0 {
		helper.BadRequest(c, "userIds empty", "userIds không được để trống")
		return
	}
	if err := ctrl.service.Broadcast(c.Request.Context(), &req); err != nil {
		helper.InternalError(c, err, "broadcast failed")
		return
	}
	c.Status(http.StatusNoContent)
}
