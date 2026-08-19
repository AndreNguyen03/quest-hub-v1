package com.questhub.modules.identity.infrastructure.redis;

import com.questhub.modules.identity.application.port.RefreshTokenStore;
import java.time.Duration;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisRefreshTokenStore implements RefreshTokenStore {

  private static final String PREFIX = "auth:refresh:";

  private final StringRedisTemplate redis;

  public RedisRefreshTokenStore(StringRedisTemplate redis) {
    this.redis = redis;
  }

  @Override
  public void store(UUID userId, String token, Duration ttl) {
    redis.opsForValue().set(PREFIX + token, userId.toString(), ttl);
  }

  @Override
  public boolean isValid(String token) {
    return Boolean.TRUE.equals(redis.hasKey(PREFIX + token));
  }

  @Override
  public void delete(String token) {
    redis.delete(PREFIX + token);
  }
}
