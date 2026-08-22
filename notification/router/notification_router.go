package router

import "github.com/gin-gonic/gin"

// SetupNotificationRouter registers notification endpoints on the given group.
func SetupNotificationRouter(rg *gin.RouterGroup, server *APIServer) {
	n := rg.Group("/notifications")
	n.GET("", server.Ctrls.Notification.List)
	n.PATCH("/:id/read", server.Ctrls.Notification.MarkRead)
	n.PATCH("/read-all", server.Ctrls.Notification.MarkAllRead)
	n.GET("/unread-count", server.Ctrls.Notification.UnreadCount)
}
