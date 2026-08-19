package com.questhub.modules.identity.application.port;

import java.time.Duration;
import java.util.UUID;

public interface RefreshTokenStore {

  void store(UUID userId, String token, Duration ttl);

  boolean isValid(String token);

  void delete(String token);
}