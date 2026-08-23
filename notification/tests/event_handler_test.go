package tests

import (
	"context"
	"encoding/json"
	"testing"

	"notification/infra/worker"
	"notification/schemas"

	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

// ---- helpers ----

func mustJSON(v any) json.RawMessage {
	b, _ := json.Marshal(v)
	return b
}

func handle(t *testing.T, svc *mockSvc, eventType string, payload any) error {
	t.Helper()
	emailRepo := newFakeEmailRepo()
	h := worker.NewEventHandler(svc, emailRepo)
	return h.Handle(context.Background(), worker.OutboxEvent{
		ID:        uuid.New().String(),
		EventType: eventType,
		Payload:   mustJSON(payload),
	})
}

func firstNotif(t *testing.T, svc *mockSvc) *schemas.Notification {
	t.Helper()
	svc.mu.Lock()
	defer svc.mu.Unlock()
	require.NotEmpty(t, svc.notified, "expected at least one notification to be created")
	return svc.notified[0]
}

// ---- task.completed ----

func TestHandleTaskCompleted_CreatesTaskNotification(t *testing.T) {
	svc := &mockSvc{}
	userID := uuid.New()

	require.NoError(t, handle(t, svc, "task.completed", map[string]any{
		"userId":           userID.String(),
		"taskTitle":        "Write tests",
		"questTitle":       "Go mastery",
		"questId":          uuid.New().String(),
		"isQuestCompleted": false,
	}))

	n := firstNotif(t, svc)
	assert.Equal(t, schemas.TypeTaskCompleted, n.Type)
	assert.Equal(t, userID, n.UserID)
	assert.Contains(t, n.Title, "Write tests")
}

func TestHandleTaskCompleted_AlsoCreatesQuestNotifWhenFlagSet(t *testing.T) {
	svc := &mockSvc{}
	userID := uuid.New()

	require.NoError(t, handle(t, svc, "task.completed", map[string]any{
		"userId":           userID.String(),
		"taskTitle":        "Last task",
		"questTitle":       "Quest finished",
		"questId":          uuid.New().String(),
		"isQuestCompleted": true,
	}))

	svc.mu.Lock()
	defer svc.mu.Unlock()
	require.Len(t, svc.notified, 2, "expect TASK_COMPLETED + QUEST_COMPLETED")

	types := []schemas.NotificationType{svc.notified[0].Type, svc.notified[1].Type}
	assert.Contains(t, types, schemas.TypeTaskCompleted)
	assert.Contains(t, types, schemas.TypeQuestCompleted)
}

// ---- quest.completed ----

func TestHandleQuestCompleted_CreatesQuestNotification(t *testing.T) {
	svc := &mockSvc{}
	userID := uuid.New()

	require.NoError(t, handle(t, svc, "quest.completed", map[string]any{
		"userId":     userID.String(),
		"questTitle": "Go mastery",
		"questId":    uuid.New().String(),
	}))

	n := firstNotif(t, svc)
	assert.Equal(t, schemas.TypeQuestCompleted, n.Type)
	assert.Equal(t, userID, n.UserID)
	assert.Contains(t, n.Title, "Go mastery")
}

// ---- achievement.unlocked ----

func TestHandleAchievementUnlocked_CreatesAchievementNotification(t *testing.T) {
	svc := &mockSvc{}
	userID := uuid.New()

	require.NoError(t, handle(t, svc, "achievement.unlocked", map[string]any{
		"userId":        userID.String(),
		"title":         "First Quest",
		"achievementId": uuid.New().String(),
	}))

	n := firstNotif(t, svc)
	assert.Equal(t, schemas.TypeAchievement, n.Type)
	assert.Contains(t, n.Title, "First Quest")
}

// ---- comment.created ----

func TestHandleCommentCreated_CreatesCommentNotification(t *testing.T) {
	svc := &mockSvc{}
	recipientID := uuid.New()

	require.NoError(t, handle(t, svc, "comment.created", map[string]any{
		"recipientUserId": recipientID.String(),
		"authorUsername":  "alice",
		"questId":         uuid.New().String(),
		"commentId":       uuid.New().String(),
	}))

	n := firstNotif(t, svc)
	assert.Equal(t, schemas.TypeComment, n.Type)
	assert.Equal(t, recipientID, n.UserID)
	assert.Contains(t, n.Title, "alice")
}

func TestHandleCommentCreated_MissingRecipient_Skips(t *testing.T) {
	svc := &mockSvc{}

	require.NoError(t, handle(t, svc, "comment.created", map[string]any{
		"recipientUserId": "",
		"authorUsername":  "bob",
		"questId":         uuid.New().String(),
	}))

	svc.mu.Lock()
	defer svc.mu.Unlock()
	assert.Empty(t, svc.notified, "should skip when recipientUserId is missing")
}

// ---- discussion.created ----

func TestHandleDiscussionCreated_CreatesCommentNotification(t *testing.T) {
	svc := &mockSvc{}
	recipientID := uuid.New()

	require.NoError(t, handle(t, svc, "discussion.created", map[string]any{
		"recipientUserId": recipientID.String(),
		"authorUsername":  "charlie",
		"questId":         uuid.New().String(),
		"discussionId":    uuid.New().String(),
		"title":           "How do I start?",
	}))

	n := firstNotif(t, svc)
	assert.Equal(t, schemas.TypeComment, n.Type)
	assert.Equal(t, recipientID, n.UserID)
	assert.Contains(t, n.Title, "charlie")
}

func TestHandleDiscussionCreated_MissingRecipient_Skips(t *testing.T) {
	svc := &mockSvc{}

	require.NoError(t, handle(t, svc, "discussion.created", map[string]any{
		"recipientUserId": "",
		"authorUsername":  "dave",
	}))

	svc.mu.Lock()
	defer svc.mu.Unlock()
	assert.Empty(t, svc.notified)
}

// ---- user.followed ----

func TestHandleUserFollowed_CreatesFollowedNotification(t *testing.T) {
	svc := &mockSvc{}
	followedID := uuid.New()

	require.NoError(t, handle(t, svc, "user.followed", map[string]any{
		"followerUsername": "eve",
		"followedUserId":   followedID.String(),
	}))

	n := firstNotif(t, svc)
	assert.Equal(t, schemas.TypeFollowed, n.Type)
	assert.Equal(t, followedID, n.UserID)
	assert.Contains(t, n.Title, "eve")
}

// ---- submission.graded ----

func TestHandleSubmissionGraded_Pass(t *testing.T) {
	svc := &mockSvc{}
	userID := uuid.New()

	require.NoError(t, handle(t, svc, "submission.graded", map[string]any{
		"userId":  userID.String(),
		"questId": uuid.New().String(),
		"status":  "PASS",
		"score":   95.0,
	}))

	n := firstNotif(t, svc)
	assert.Equal(t, schemas.TypeReview, n.Type)
	assert.Contains(t, n.Title, "passed")
	assert.Contains(t, n.Title, "95")
}

func TestHandleSubmissionGraded_Fail(t *testing.T) {
	svc := &mockSvc{}
	userID := uuid.New()

	require.NoError(t, handle(t, svc, "submission.graded", map[string]any{
		"userId":  userID.String(),
		"questId": uuid.New().String(),
		"status":  "FAIL",
		"score":   40.0,
	}))

	n := firstNotif(t, svc)
	assert.Equal(t, schemas.TypeReview, n.Type)
	assert.Contains(t, n.Title, "revision")
}

// ---- user.registered ----

func TestHandleUserRegistered_StoresEmail(t *testing.T) {
	svc := &mockSvc{}
	emailRepo := newFakeEmailRepo()
	userID := uuid.New()

	h := worker.NewEventHandler(svc, emailRepo)
	err := h.Handle(context.Background(), worker.OutboxEvent{
		ID:        uuid.New().String(),
		EventType: "user.registered",
		Payload: mustJSON(map[string]any{
			"userId": userID.String(),
			"email":  "user@example.com",
		}),
	})
	require.NoError(t, err)

	emailRepo.mu.Lock()
	defer emailRepo.mu.Unlock()
	assert.Equal(t, "user@example.com", emailRepo.stored[userID])
}

func TestHandleUserRegistered_EmptyEmail_Skips(t *testing.T) {
	svc := &mockSvc{}
	emailRepo := newFakeEmailRepo()
	userID := uuid.New()

	h := worker.NewEventHandler(svc, emailRepo)
	err := h.Handle(context.Background(), worker.OutboxEvent{
		ID:        uuid.New().String(),
		EventType: "user.registered",
		Payload: mustJSON(map[string]any{
			"userId": userID.String(),
			"email":  "",
		}),
	})
	require.NoError(t, err)

	emailRepo.mu.Lock()
	defer emailRepo.mu.Unlock()
	assert.Empty(t, emailRepo.stored)
}

// ---- unknown event ----

func TestHandle_UnknownEventType_ReturnsError(t *testing.T) {
	svc := &mockSvc{}
	err := handle(t, svc, "some.unknown.event", map[string]any{})
	require.Error(t, err)
	assert.Contains(t, err.Error(), "unknown event type")
}
