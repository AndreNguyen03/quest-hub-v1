package tests

import (
	"context"
	"encoding/json"
	"testing"

	"social/infra/worker"
	"social/schemas"

	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func mustJSON(v any) json.RawMessage {
	b, _ := json.Marshal(v)
	return b
}

func handleEvent(svc *fakeActivitySvc, eventType string, payload any) error {
	h := worker.NewEventHandler(svc)
	return h.Handle(context.Background(), worker.OutboxEvent{
		ID:        uuid.New().String(),
		EventType: eventType,
		Payload:   mustJSON(payload),
	})
}

func TestHandleQuestPublished_CreatesActivity(t *testing.T) {
	svc := &fakeActivitySvc{}
	userID := uuid.New()

	require.NoError(t, handleEvent(svc, "quest.published", map[string]any{
		"userId": userID.String(), "questId": uuid.New().String(), "questTitle": "Go Zero to Hero",
	}))

	svc.mu.Lock()
	defer svc.mu.Unlock()
	require.Len(t, svc.created, 1)
	assert.Equal(t, schemas.ActivityQuestPublished, svc.created[0].Type)
	assert.Equal(t, userID, svc.created[0].UserID)
}

func TestHandleQuestForked_CreatesActivity(t *testing.T) {
	svc := &fakeActivitySvc{}
	userID := uuid.New()

	require.NoError(t, handleEvent(svc, "quest.forked", map[string]any{
		"userId": userID.String(), "questId": uuid.New().String(), "questTitle": "Go",
	}))

	svc.mu.Lock()
	defer svc.mu.Unlock()
	assert.Equal(t, schemas.ActivityQuestForked, svc.created[0].Type)
}

func TestHandleQuestCompleted_CreatesActivity(t *testing.T) {
	svc := &fakeActivitySvc{}
	userID := uuid.New()

	require.NoError(t, handleEvent(svc, "quest.completed", map[string]any{
		"userId": userID.String(), "questId": uuid.New().String(), "questTitle": "Go",
	}))

	svc.mu.Lock()
	defer svc.mu.Unlock()
	assert.Equal(t, schemas.ActivityQuestCompleted, svc.created[0].Type)
}

func TestHandleTaskCompleted_NonFinal_CreatesActivity(t *testing.T) {
	svc := &fakeActivitySvc{}
	userID := uuid.New()

	require.NoError(t, handleEvent(svc, "task.completed", map[string]any{
		"userId": userID.String(), "questId": uuid.New().String(),
		"taskTitle": "Read chapter 1", "isQuestCompleted": false,
	}))

	svc.mu.Lock()
	defer svc.mu.Unlock()
	require.Len(t, svc.created, 1)
	assert.Equal(t, schemas.ActivityTaskCompleted, svc.created[0].Type)
}

func TestHandleTaskCompleted_Final_Skips(t *testing.T) {
	// When isQuestCompleted=true, quest.completed event carries the feed item
	svc := &fakeActivitySvc{}

	require.NoError(t, handleEvent(svc, "task.completed", map[string]any{
		"userId": uuid.New().String(), "questId": uuid.New().String(),
		"taskTitle": "Last task", "isQuestCompleted": true,
	}))

	svc.mu.Lock()
	defer svc.mu.Unlock()
	assert.Empty(t, svc.created, "should not create duplicate task activity when quest also completes")
}

func TestHandleAchievementUnlocked_CreatesActivity(t *testing.T) {
	svc := &fakeActivitySvc{}
	userID := uuid.New()

	require.NoError(t, handleEvent(svc, "achievement.unlocked", map[string]any{
		"userId": userID.String(), "achievementId": uuid.New().String(), "title": "Five Quests",
	}))

	svc.mu.Lock()
	defer svc.mu.Unlock()
	assert.Equal(t, schemas.ActivityAchievementUnlocked, svc.created[0].Type)
	assert.Equal(t, "Five Quests", svc.created[0].Payload["title"])
}

func TestHandleUnknownEvent_ReturnsError(t *testing.T) {
	svc := &fakeActivitySvc{}
	err := handleEvent(svc, "some.unknown.event", map[string]any{})
	require.Error(t, err)
	assert.Contains(t, err.Error(), "unknown event type")
}
