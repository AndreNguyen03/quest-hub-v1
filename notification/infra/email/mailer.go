// Package email provides an optional SMTP mailer.
// If SMTPHost is empty all Send calls are no-ops.
package email

import (
	"crypto/tls"
	"fmt"

	"gopkg.in/gomail.v2"

	"notification/util/logger"
)

// Mailer sends transactional emails via SMTP.
type Mailer struct {
	dialer *gomail.Dialer // nil when not configured
	from   string
}

// NewMailer returns a Mailer configured from the given parameters.
// Returns a no-op Mailer when host is empty.
func NewMailer(host string, port int, username, password, from string) *Mailer {
	if host == "" {
		logger.Log.Info().Msg("email disabled (SMTP_HOST not set)")
		return &Mailer{}
	}

	d := gomail.NewDialer(host, port, username, password)
	d.TLSConfig = &tls.Config{ServerName: host}

	logger.Log.Info().Str("host", host).Int("port", port).Msg("SMTP mailer initialized")
	return &Mailer{dialer: d, from: from}
}

// Send delivers a plain-text + HTML email. Silently returns nil when disabled.
func (m *Mailer) Send(to, subject, htmlBody string) error {
	if m.dialer == nil {
		return nil
	}

	msg := gomail.NewMessage()
	msg.SetHeader("From", m.from)
	msg.SetHeader("To", to)
	msg.SetHeader("Subject", subject)
	msg.SetBody("text/plain", stripHTML(htmlBody))
	msg.AddAlternative("text/html", htmlBody)

	if err := m.dialer.DialAndSend(msg); err != nil {
		return fmt.Errorf("smtp send: %w", err)
	}
	return nil
}

// stripHTML is a minimal plaintext fallback — just remove tags.
func stripHTML(s string) string {
	out := make([]byte, 0, len(s))
	inTag := false
	for i := 0; i < len(s); i++ {
		switch s[i] {
		case '<':
			inTag = true
		case '>':
			inTag = false
		default:
			if !inTag {
				out = append(out, s[i])
			}
		}
	}
	return string(out)
}
