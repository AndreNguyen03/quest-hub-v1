package tests

// mocks_test.go chứa các fake implementations dùng chung trong toàn bộ tests/ package.

import (
	"context"
	"sync"

	"questhub/notification/schemas"

	"github.com/google/uuid"
)

// ---- fakeNotifRepo ----

type fakeNotifRepo struct {
	mu        sync.Mutex
	created   []*schemas.Notification
	createErr error
	countVal  int
}

func (r *fakeNotifRepo) Create(_ context.Context, n *schemas.Notification) error {
	if r.createErr != nil {
		return r.createErr
	}
	r.mu.Lock()
	r.created = append(r.created, n)
	r.mu.Unlock()
	return nil
}
func (r *fakeNotifRepo) ListByUser(_ context.Context, _ uuid.UUID, _, _ int) ([]schemas.Notification, error) {
	return []schemas.Notification{}, nil
}
func (r *fakeNotifRepo) MarkRead(_ context.Context, _ uuid.UUID) error    { return nil }
func (r *fakeNotifRepo) MarkAllRead(_ context.Context, _ uuid.UUID) error { return nil }
func (r *fakeNotifRepo) CountUnread(_ context.Context, _ uuid.UUID) (int, error) {
	return r.countVal, nil
}

// ---- fakeTokenRepo ----

type fakeTokenRepo struct {
	mu       sync.Mutex
	upserted []*schemas.DeviceToken
	deleted  []struct {
		userID uuid.UUID
		token  string
	}
}

func (r *fakeTokenRepo) Upsert(_ context.Context, dt *schemas.DeviceToken) error {
	r.mu.Lock()
	r.upserted = append(r.upserted, dt)
	r.mu.Unlock()
	return nil
}
func (r *fakeTokenRepo) ListByUser(_ context.Context, _ uuid.UUID) ([]schemas.DeviceToken, error) {
	return nil, nil
}
func (r *fakeTokenRepo) Delete(_ context.Context, userID uuid.UUID, token string) error {
	r.mu.Lock()
	r.deleted = append(r.deleted, struct {
		userID uuid.UUID
		token  string
	}{userID, token})
	r.mu.Unlock()
	return nil
}

// ---- fakeEmailRepo ----

type fakeEmailRepo struct {
	mu     sync.Mutex
	stored map[uuid.UUID]string
}

func newFakeEmailRepo() *fakeEmailRepo {
	return &fakeEmailRepo{stored: make(map[uuid.UUID]string)}
}

func (r *fakeEmailRepo) Upsert(_ context.Context, id uuid.UUID, email string) error {
	r.mu.Lock()
	r.stored[id] = email
	r.mu.Unlock()
	return nil
}
func (r *fakeEmailRepo) FindByUser(_ context.Context, id uuid.UUID) (string, error) {
	r.mu.Lock()
	defer r.mu.Unlock()
	return r.stored[id], nil
}

// ---- mockSvc (INotificationService) — dùng trong event_handler tests ----

type mockSvc struct {
	mu       sync.Mutex
	notified []*schemas.Notification
}

func (m *mockSvc) Notify(_ context.Context, n *schemas.Notification) error {
	m.mu.Lock()
	m.notified = append(m.notified, n)
	m.mu.Unlock()
	return nil
}
func (m *mockSvc) Broadcast(_ context.Context, _ *schemas.BroadcastRequest) error { return nil }
func (m *mockSvc) ListByUser(_ context.Context, _ uuid.UUID, _, _ int) (*schemas.ListNotificationsResponse, error) {
	return &schemas.ListNotificationsResponse{}, nil
}
func (m *mockSvc) MarkRead(_ context.Context, _ uuid.UUID) error    { return nil }
func (m *mockSvc) MarkAllRead(_ context.Context, _ uuid.UUID) error { return nil }
func (m *mockSvc) UnreadCount(_ context.Context, _ uuid.UUID) (*schemas.UnreadCountResponse, error) {
	return &schemas.UnreadCountResponse{}, nil
}
