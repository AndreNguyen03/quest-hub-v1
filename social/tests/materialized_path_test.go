package tests

import (
	"fmt"
	"testing"

	"social/schemas"

	"github.com/stretchr/testify/assert"
)

// TestBuildPath_RootComment — root comment có path đúng 10 ký tự
func TestBuildPath_RootComment(t *testing.T) {
	path := schemas.BuildPath("", 1)
	assert.Equal(t, "0000000001", path)
	assert.Equal(t, 10, len(path))
}

// TestBuildPath_ReplyComment — reply appends 10 ký tự vào parent path
func TestBuildPath_ReplyComment(t *testing.T) {
	parentPath := schemas.BuildPath("", 1)   // "0000000001"
	childPath := schemas.BuildPath(parentPath, 2) // "00000000010000000002"
	assert.Equal(t, "00000000010000000002", childPath)
	assert.Equal(t, 20, len(childPath))
}

// TestBuildPath_SecondLevelReply — path 3 cấp = 30 ký tự
func TestBuildPath_SecondLevelReply(t *testing.T) {
	l1 := schemas.BuildPath("", 1)
	l2 := schemas.BuildPath(l1, 2)
	l3 := schemas.BuildPath(l2, 4)
	assert.Equal(t, 30, len(l3))
	assert.Equal(t, "000000000100000000020000000004", l3)
}

// TestBuildPath_ZeroPadding — số nhỏ được pad đủ 10 chữ số
func TestBuildPath_ZeroPadding(t *testing.T) {
	cases := []struct {
		seq  int64
		want string
	}{
		{1, "0000000001"},
		{42, "0000000042"},
		{9999999999, "9999999999"},
	}
	for _, tc := range cases {
		assert.Equal(t, tc.want, schemas.BuildPath("", tc.seq), "seq=%d", tc.seq)
	}
}

// TestComment_Depth — Depth() trả đúng level
func TestComment_Depth(t *testing.T) {
	cases := []struct {
		path  string
		depth int
	}{
		{"0000000001", 0},                         // root
		{"00000000010000000002", 1},                // first reply
		{"000000000100000000020000000003", 2},       // second reply
	}
	for _, tc := range cases {
		c := schemas.Comment{Path: tc.path}
		assert.Equal(t, tc.depth, c.Depth(), "path=%s", tc.path)
	}
}

// TestComment_ParentPath — ParentPath() suy ra đúng parent
func TestComment_ParentPath(t *testing.T) {
	root := schemas.Comment{Path: "0000000001"}
	assert.Equal(t, "", root.ParentPath(), "root has no parent")

	child := schemas.Comment{Path: "00000000010000000002"}
	assert.Equal(t, "0000000001", child.ParentPath())

	grandChild := schemas.Comment{Path: "000000000100000000020000000004"}
	assert.Equal(t, "00000000010000000002", grandChild.ParentPath())
}

// TestLexicographicOrder — ORDER BY path gives correct depth-first traversal
//
// Tree:
//
//	1
//	├── 2
//	│   ├── 4
//	│   └── 5
//	└── 3
//	    └── 6
//
// Expected ORDER BY path: 1, 2, 4, 5, 3, 6
func TestLexicographicOrder(t *testing.T) {
	paths := []string{
		schemas.BuildPath("", 1),                        // 1
		schemas.BuildPath(schemas.BuildPath("", 1), 2),  // 1→2
		schemas.BuildPath(schemas.BuildPath("", 1), 3),  // 1→3
		schemas.BuildPath(schemas.BuildPath(schemas.BuildPath("", 1), 2), 4), // 1→2→4
		schemas.BuildPath(schemas.BuildPath(schemas.BuildPath("", 1), 2), 5), // 1→2→5
		schemas.BuildPath(schemas.BuildPath(schemas.BuildPath("", 1), 3), 6), // 1→3→6
	}

	// Sort manually the same way postgres ORDER BY path would.
	sorted := make([]string, len(paths))
	copy(sorted, paths)
	for i := 0; i < len(sorted)-1; i++ {
		for j := i + 1; j < len(sorted); j++ {
			if sorted[i] > sorted[j] {
				sorted[i], sorted[j] = sorted[j], sorted[i]
			}
		}
	}

	expected := []string{
		paths[0], // 1
		paths[1], // 1→2
		paths[3], // 1→2→4
		paths[4], // 1→2→5
		paths[2], // 1→3
		paths[5], // 1→3→6
	}

	assert.Equal(t, expected, sorted, fmt.Sprintf("paths: %v", paths))
}
