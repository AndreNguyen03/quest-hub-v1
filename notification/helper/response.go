// Package helper provides shared HTTP response helpers following the
// ShareWay conventions: every response goes through GinResponse, errors
// use a standardized {"error": bool, "message": string} body with an
// English dev message (logged) and a Vietnamese user-facing message.
package helper

import (
	"net/http"

	"questhub/notification/util/logger"

	"github.com/gin-gonic/gin"
)

// ErrorResponse is the standardized error body, exposed for Swagger docs.
type ErrorResponse struct {
	Error   bool   `json:"error" example:"true"`
	Message string `json:"message" example:"Lỗi hệ thống, vui lòng thử lại sau"`
}

// GinResponse writes the payload as JSON with the given status code.
func GinResponse(c *gin.Context, statusCode int, response any) {
	c.JSON(statusCode, response)
}

// ErrorResponseWithMessage logs the error with the English dev message and
// responds with the standardized error body carrying the Vietnamese message.
func ErrorResponseWithMessage(c *gin.Context, statusCode int, err error, devMsg, userMsg string) {
	entry := logger.Log.Error()
	if err != nil {
		entry = entry.Err(err)
	}
	entry.Msg(devMsg)
	GinResponse(c, statusCode, ErrorResponse{Error: true, Message: userMsg})
}

// BadRequest responds with 400 and the standardized error body.
func BadRequest(c *gin.Context, devMsg, userMsg string) {
	ErrorResponseWithMessage(c, http.StatusBadRequest, nil, devMsg, userMsg)
}

// InternalError responds with 500 and the standardized error body.
func InternalError(c *gin.Context, err error, devMsg string) {
	ErrorResponseWithMessage(c, http.StatusInternalServerError, err, devMsg, "Lỗi hệ thống, vui lòng thử lại sau")
}
