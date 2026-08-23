// Package push wraps Firebase Cloud Messaging for optional push delivery.
// If FCMCredentialsPath is empty the client is a no-op.
package push

import (
	"context"

	firebase "firebase.google.com/go/v4"
	"firebase.google.com/go/v4/messaging"
	"google.golang.org/api/option"

	"questhub/notification/util/logger"
)

// FCMClient sends push notifications via Firebase Cloud Messaging.
// A nil internal app means FCM is disabled — all Send calls are no-ops.
type FCMClient struct {
	msg *messaging.Client // nil when not configured
}

// NewFCMClient initialises the Firebase Admin SDK from credentialsPath.
// Returns a no-op client when credentialsPath is empty.
func NewFCMClient(credentialsPath string) *FCMClient {
	if credentialsPath == "" {
		logger.Log.Info().Msg("FCM disabled (FCM_CREDENTIALS_PATH not set)")
		return &FCMClient{}
	}

	app, err := firebase.NewApp(context.Background(), nil,
		option.WithCredentialsFile(credentialsPath),
	)
	if err != nil {
		logger.Log.Error().Err(err).Msg("FCM init failed — push disabled")
		return &FCMClient{}
	}

	msgClient, err := app.Messaging(context.Background())
	if err != nil {
		logger.Log.Error().Err(err).Msg("FCM messaging client failed — push disabled")
		return &FCMClient{}
	}

	logger.Log.Info().Str("creds", credentialsPath).Msg("FCM initialized")
	return &FCMClient{msg: msgClient}
}

// Send delivers a push notification to a single device token.
// Silently returns nil when FCM is disabled.
func (c *FCMClient) Send(ctx context.Context, deviceToken, title, body string) error {
	if c.msg == nil {
		return nil
	}
	_, err := c.msg.Send(ctx, &messaging.Message{
		Token: deviceToken,
		Notification: &messaging.Notification{
			Title: title,
			Body:  body,
		},
	})
	return err
}
