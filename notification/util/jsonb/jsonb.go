// Package jsonb provides a GORM-compatible JSONB payload type.
package jsonb

import (
	"database/sql/driver"
	"encoding/json"
	"fmt"
)

// JSONB is a generic map persisted as PostgreSQL JSONB column.
type JSONB map[string]any

// Value implements driver.Valuer — marshals to JSON bytes for storage.
func (j JSONB) Value() (driver.Value, error) {
	if j == nil {
		return []byte("{}"), nil
	}
	bytes, err := json.Marshal(j)
	if err != nil {
		return nil, fmt.Errorf("marshal jsonb: %w", err)
	}
	return bytes, nil
}

// Scan implements sql.Scanner — unmarshals stored JSON into the map.
func (j *JSONB) Scan(value any) error {
	var bytes []byte
	switch v := value.(type) {
	case []byte:
		bytes = v
	case string:
		bytes = []byte(v)
	default:
		return fmt.Errorf("unsupported jsonb type %T", value)
	}
	result := map[string]any{}
	if err := json.Unmarshal(bytes, &result); err != nil {
		return fmt.Errorf("unmarshal jsonb: %w", err)
	}
	*j = result
	return nil
}

// GormDataType returns the GORM data type name used in migrations.
func (JSONB) GormDataType() string {
	return "jsonb"
}
