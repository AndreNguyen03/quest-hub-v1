package tests

import (
	"context"
	"sync"

	"social/schemas"
	"social/util/jsonb"

	"github.com/google/uuid"
)

// ---- fakeActivityRepo ----

type fakeActivityRepo struct {
	mu      sync.Mutex
	created []*schemas.Activity
}

func (r *fakeActivityRepo) Create(_ context.Context, a *schemas.Activity) error {
	r.mu.Lock()
	r.created = append(r.created, a)
	r.mu.Unlock()
	return nil
}
func (r *fakeActivityRepo) Feed(_ context.Context, _ uuid.UUID, limit, _ int) ([]schemas.Activity, error) {
	return make([]schemas.Activity, 0), nil
}
func (r *fakeActivityRepo) ListByUser(_ context.Context, _ uuid.UUID, limit, _ int) ([]schemas.Activity, error) {
	return make([]schemas.Activity, 0), nil
}

// ---- fakeFollowRepo ----

type fakeFollowRepo struct {
	mu         sync.Mutex
	follows    map[string]bool // "follower:followee" → true
	followings []schemas.Follow
	followers  []schemas.Follow
}

func newFakeFollowRepo() *fakeFollowRepo {
	return &fakeFollowRepo{follows: make(map[string]bool)}
}

func (r *fakeFollowRepo) key(a, b uuid.UUID) string { return a.String() + ":" + b.String() }

func (r *fakeFollowRepo) Follow(_ context.Context, followerID, followeeID uuid.UUID) error {
	r.mu.Lock()
	r.follows[r.key(followerID, followeeID)] = true
	r.mu.Unlock()
	return nil
}
func (r *fakeFollowRepo) Unfollow(_ context.Context, followerID, followeeID uuid.UUID) error {
	r.mu.Lock()
	delete(r.follows, r.key(followerID, followeeID))
	r.mu.Unlock()
	return nil
}
func (r *fakeFollowRepo) IsFollowing(_ context.Context, followerID, followeeID uuid.UUID) (bool, error) {
	r.mu.Lock()
	defer r.mu.Unlock()
	return r.follows[r.key(followerID, followeeID)], nil
}
func (r *fakeFollowRepo) ListFollowing(_ context.Context, _ uuid.UUID, _, _ int) ([]schemas.Follow, error) {
	return r.followings, nil
}
func (r *fakeFollowRepo) ListFollowers(_ context.Context, _ uuid.UUID, _, _ int) ([]schemas.Follow, error) {
	return r.followers, nil
}

// ---- fakeCommentRepo ----

type fakeCommentRepo struct {
	mu       sync.Mutex
	created  []*schemas.Comment
	seqVal   int64
	byPath   map[string]*schemas.Comment
	findErr  error
}

func newFakeCommentRepo() *fakeCommentRepo {
	return &fakeCommentRepo{byPath: make(map[string]*schemas.Comment)}
}

func (r *fakeCommentRepo) Create(_ context.Context, c *schemas.Comment) error {
	r.mu.Lock()
	r.created = append(r.created, c)
	r.byPath[c.Path] = c
	r.mu.Unlock()
	return nil
}
func (r *fakeCommentRepo) NextPathSeq(_ context.Context) (int64, error) {
	r.mu.Lock()
	defer r.mu.Unlock()
	r.seqVal++
	return r.seqVal, nil
}
func (r *fakeCommentRepo) ListByQuest(_ context.Context, _ uuid.UUID) ([]schemas.Comment, error) {
	r.mu.Lock()
	defer r.mu.Unlock()
	var out []schemas.Comment
	for _, c := range r.created {
		out = append(out, *c)
	}
	return out, nil
}
func (r *fakeCommentRepo) FindByPath(_ context.Context, path string) (*schemas.Comment, error) {
	if r.findErr != nil {
		return nil, r.findErr
	}
	r.mu.Lock()
	defer r.mu.Unlock()
	c, ok := r.byPath[path]
	if !ok {
		return nil, nil
	}
	return c, nil
}

// ---- fakeOutboxPublisher ----

type fakeOutbox struct {
	mu       sync.Mutex
	published []struct {
		EventType string
		Payload   map[string]any
	}
}

func (o *fakeOutbox) Publish(_ context.Context, eventType string, payload map[string]any) error {
	o.mu.Lock()
	o.published = append(o.published, struct {
		EventType string
		Payload   map[string]any
	}{eventType, payload})
	o.mu.Unlock()
	return nil
}

// ---- fakeEventHandlerActivitySvc ----

type fakeActivitySvc struct {
	mu      sync.Mutex
	created []struct {
		UserID  uuid.UUID
		Type    schemas.ActivityType
		Payload jsonb.JSONB
	}
}

func (s *fakeActivitySvc) CreateActivity(_ context.Context, userID uuid.UUID, actType schemas.ActivityType, payload jsonb.JSONB) error {
	s.mu.Lock()
	s.created = append(s.created, struct {
		UserID  uuid.UUID
		Type    schemas.ActivityType
		Payload jsonb.JSONB
	}{userID, actType, payload})
	s.mu.Unlock()
	return nil
}
func (s *fakeActivitySvc) Feed(_ context.Context, _ uuid.UUID, _, _ int) (*schemas.FeedResponse, error) {
	return &schemas.FeedResponse{}, nil
}
func (s *fakeActivitySvc) ListByUser(_ context.Context, _ uuid.UUID, _, _ int) (*schemas.FeedResponse, error) {
	return &schemas.FeedResponse{}, nil
}
