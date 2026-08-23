package router

import "github.com/gin-gonic/gin"

// SetupDeviceTokenRouter registers device token endpoints on the given group.
func SetupDeviceTokenRouter(rg *gin.RouterGroup, server *APIServer) {
	dt := rg.Group("/device-tokens")
	dt.POST("", server.Ctrls.DeviceToken.Register)
	dt.DELETE("", server.Ctrls.DeviceToken.Deregister)
}
