package com.questhub.shared.domain;

import java.util.UUID;

public record AuthenticatedUser(UUID id, String username) {}
