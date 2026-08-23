// Package sse implements a Server-Sent Events hub for real-time notification delivery.
package sse

import (
	"sync"

	"notification/schemas"

	"github.com/google/uuid"
)

// Hub manages per-user SSE channels. Multiple tabs/connections per user are supported.
type Hub struct {
	mu      sync.RWMutex
	clients map[uuid.UUID][]chan schemas.Notification
}

// NewHub returns an empty Hub.
func NewHub() *Hub {
	return &Hub{clients: make(map[uuid.UUID][]chan schemas.Notification)}
}

// Subscribe registers a new channel for the given user and returns it.
// The channel is buffered so a slow consumer doesn't block Notify.
func (h *Hub) Subscribe(userID uuid.UUID) chan schemas.Notification {
	ch := make(chan schemas.Notification, 16)
	h.mu.Lock()
	h.clients[userID] = append(h.clients[userID], ch)
	h.mu.Unlock()
	return ch
}

// Unsubscribe removes the channel for the given user and closes it.
func (h *Hub) Unsubscribe(userID uuid.UUID, ch chan schemas.Notification) {
	h.mu.Lock()
	defer h.mu.Unlock()
	channels := h.clients[userID]
	for i, c := range channels {
		if c == ch {
			h.clients[userID] = append(channels[:i], channels[i+1:]...)
			close(ch)
			break
		}
	}
	if len(h.clients[userID]) == 0 {
		delete(h.clients, userID)
	}
}

// Push sends n to every active channel of the user. Drops the event if a
// channel buffer is full (non-blocking) to avoid stalling the caller.
func (h *Hub) Push(userID uuid.UUID, n schemas.Notification) {
	h.mu.RLock()
	defer h.mu.RUnlock()
	for _, ch := range h.clients[userID] {
		select {
		case ch <- n:
		default:
		}
	}
}
