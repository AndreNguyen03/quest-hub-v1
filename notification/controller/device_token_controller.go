package controller

import (
	"net/http"

	"notification/helper"
	"notification/schemas"
	"notification/service"

	"github.com/gin-gonic/gin"
)

// DeviceTokenController handles FCM device token registration.
type DeviceTokenController struct {
	service service.IDeviceTokenService
}

// NewDeviceTokenController returns a controller backed by the given service.
func NewDeviceTokenController(svc service.IDeviceTokenService) *DeviceTokenController {
	return &DeviceTokenController{service: svc}
}

// Register godoc
// @Summary      Register device token
// @Description  Đăng ký FCM device token để nhận push notification
// @Tags         device-tokens
// @Accept       json
// @Produce      json
// @Param        body  body  schemas.RegisterDeviceTokenRequest  true  "Token payload"
// @Success      204   "No Content"
// @Failure      400   {object}  helper.ErrorResponse
// @Failure      500   {object}  helper.ErrorResponse
// @Router       /api/v1/device-tokens [post]
func (ctrl *DeviceTokenController) Register(c *gin.Context) {
	var req schemas.RegisterDeviceTokenRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		helper.BadRequest(c, "bind register token failed", "Dữ liệu không hợp lệ")
		return
	}
	if err := ctrl.service.Register(c.Request.Context(), &req); err != nil {
		helper.InternalError(c, err, "register device token failed")
		return
	}
	c.Status(http.StatusNoContent)
}

// Deregister godoc
// @Summary      Deregister device token
// @Description  Xoá FCM device token (đăng xuất hoặc uninstall app)
// @Tags         device-tokens
// @Accept       json
// @Produce      json
// @Param        body  body  schemas.DeregisterDeviceTokenRequest  true  "Token payload"
// @Success      204   "No Content"
// @Failure      400   {object}  helper.ErrorResponse
// @Failure      500   {object}  helper.ErrorResponse
// @Router       /api/v1/device-tokens [delete]
func (ctrl *DeviceTokenController) Deregister(c *gin.Context) {
	var req schemas.DeregisterDeviceTokenRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		helper.BadRequest(c, "bind deregister token failed", "Dữ liệu không hợp lệ")
		return
	}
	if err := ctrl.service.Deregister(c.Request.Context(), &req); err != nil {
		helper.InternalError(c, err, "deregister device token failed")
		return
	}
	c.Status(http.StatusNoContent)
}
