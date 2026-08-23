package tests

import (
	"testing"
	"time"

	"notification/infra/sse"
	"notification/schemas"

	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func newNotif(userID uuid.UUID, title string) schemas.Notification {
	return schemas.Notification{ID: uuid.New(), UserID: userID, Type: schemas.TypeAdmin, Title: title}
}

func TestHub_SubscriberReceivesPushedNotification(t *testing.T) {
	hub := sse.NewHub()
	userID := uuid.New()

	ch := hub.Subscribe(userID)
	defer hub.Unsubscribe(userID, ch)

	n := newNotif(userID, "hello")
	hub.Push(userID, n)

	select {
	case got := <-ch:
		assert.Equal(t, n.ID, got.ID)
		assert.Equal(t, "hello", got.Title)
	case <-time.After(100 * time.Millisecond):
		t.Fatal("timeout: expected notification on channel")
	}
}

func TestHub_AllSubscribersReceiveOnPush(t *testing.T) {
	hub := sse.NewHub()
	userID := uuid.New()

	ch1 := hub.Subscribe(userID)
	ch2 := hub.Subscribe(userID)
	defer hub.Unsubscribe(userID, ch1)
	defer hub.Unsubscribe(userID, ch2)

	n := newNotif(userID, "broadcast")
	hub.Push(userID, n)

	for i, ch := range []chan schemas.Notification{ch1, ch2} {
		select {
		case got := <-ch:
			assert.Equal(t, n.ID, got.ID, "subscriber %d", i)
		case <-time.After(100 * time.Millisecond):
			t.Fatalf("subscriber %d: timeout", i)
		}
	}
}

func TestHub_OtherUserReceivesNothing(t *testing.T) {
	hub := sse.NewHub()
	userA, userB := uuid.New(), uuid.New()

	chA := hub.Subscribe(userA)
	defer hub.Unsubscribe(userA, chA)

	hub.Push(userB, newNotif(userB, "for B only"))

	select {
	case got := <-chA:
		t.Fatalf("userA should not receive notification for userB, got: %v", got)
	case <-time.After(50 * time.Millisecond):
		// correct: nothing delivered
	}
}

func TestHub_UnsubscribeClosesChannel(t *testing.T) {
	hub := sse.NewHub()
	userID := uuid.New()
	ch := hub.Subscribe(userID)

	hub.Unsubscribe(userID, ch)

	_, open := <-ch
	require.False(t, open, "channel should be closed after Unsubscribe")
}

func TestHub_PushAfterUnsubscribeDoesNotPanic(t *testing.T) {
	hub := sse.NewHub()
	userID := uuid.New()
	ch := hub.Subscribe(userID)
	hub.Unsubscribe(userID, ch)

	assert.NotPanics(t, func() {
		hub.Push(userID, newNotif(userID, "after unsub"))
	})
}

func TestHub_FullBufferDoesNotBlock(t *testing.T) {
	hub := sse.NewHub()
	userID := uuid.New()
	ch := hub.Subscribe(userID)
	defer hub.Unsubscribe(userID, ch)

	done := make(chan struct{})
	go func() {
		for i := 0; i < 25; i++ { // buffer = 16, push 25 must not deadlock
			hub.Push(userID, newNotif(userID, "flood"))
		}
		close(done)
	}()

	select {
	case <-done:
	case <-time.After(500 * time.Millisecond):
		t.Fatal("Push blocked on a full channel")
	}
}
