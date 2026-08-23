// Package jsonb provides a GORM-compatible JSONB payload type.
package jsonb

import (
	"database/sql/driver"
	"encoding/json"
	"fmt"
)

// JSONB is a generic map persisted as PostgreSQL JSONB column.
type JSONB map[string]any

func (j JSONB) Value() (driver.Value, error) {
	if j == nil {
		return []byte("{}"), nil
	}
	b, err := json.Marshal(j)
	if err != nil {
		return nil, fmt.Errorf("marshal jsonb: %w", err)
	}
	return b, nil
}

func (j *JSONB) Scan(value any) error {
	var b []byte
	switch v := value.(type) {
	case []byte:
		b = v
	case string:
		b = []byte(v)
	default:
		return fmt.Errorf("unsupported jsonb type %T", value)
	}
	result := map[string]any{}
	if err := json.Unmarshal(b, &result); err != nil {
		return fmt.Errorf("unmarshal jsonb: %w", err)
	}
	*j = result
	return nil
}

func (JSONB) GormDataType() string { return "jsonb" }
