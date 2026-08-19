package com.questhub.modules.identity.domain.user;

import com.questhub.shared.domain.DomainValidationException;

public record Username(String value) {
    public Username {
        if (value == null || !value.matches("^[a-z0-9_]+$")) {
            throw new DomainValidationException("User must include lowercase character or number or underscore" + value);
        }
    }
}
