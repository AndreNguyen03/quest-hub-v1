// Package helper provides standardized HTTP response helpers.
package helper

import (
	"net/http"

	"questhub/social/util/logger"

	"github.com/gin-gonic/gin"
)

// ErrorResponse is the standardized error body.
type ErrorResponse struct {
	Error   bool   `json:"error" example:"true"`
	Message string `json:"message" example:"Lỗi hệ thống, vui lòng thử lại sau"`
}

// GinResponse writes JSON with the given status code.
func GinResponse(c *gin.Context, statusCode int, response any) { c.JSON(statusCode, response) }

// ErrorResponseWithMessage logs the error then responds with the standardized error body.
func ErrorResponseWithMessage(c *gin.Context, statusCode int, err error, devMsg, userMsg string) {
	entry := logger.Log.Error()
	if err != nil {
		entry = entry.Err(err)
	}
	entry.Msg(devMsg)
	GinResponse(c, statusCode, ErrorResponse{Error: true, Message: userMsg})
}

// BadRequest responds with 400.
func BadRequest(c *gin.Context, devMsg, userMsg string) {
	ErrorResponseWithMessage(c, http.StatusBadRequest, nil, devMsg, userMsg)
}

// NotFound responds with 404.
func NotFound(c *gin.Context, devMsg, userMsg string) {
	ErrorResponseWithMessage(c, http.StatusNotFound, nil, devMsg, userMsg)
}

// Conflict responds with 409.
func Conflict(c *gin.Context, devMsg, userMsg string) {
	ErrorResponseWithMessage(c, http.StatusConflict, nil, devMsg, userMsg)
}

// InternalError responds with 500.
func InternalError(c *gin.Context, err error, devMsg string) {
	ErrorResponseWithMessage(c, http.StatusInternalServerError, err, devMsg, "Lỗi hệ thống, vui lòng thử lại sau")
}
