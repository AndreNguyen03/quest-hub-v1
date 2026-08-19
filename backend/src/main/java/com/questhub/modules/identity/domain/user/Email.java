package com.questhub.modules.identity.domain.user;

import com.questhub.shared.domain.DomainValidationException;

public record Email(String value) {
    public Email {
        if (value == null || !value.matches("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$")) {
            throw new DomainValidationException("Email not valid: " + value);
        }
    }
}
