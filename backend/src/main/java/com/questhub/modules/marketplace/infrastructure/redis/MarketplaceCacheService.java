package com.questhub.modules.marketplace.infrastructure.redis;

import java.time.Duration;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class MarketplaceCacheService {

  private static final String PREFIX_TRENDING = "marketplace:trending:";
  private static final String PREFIX_POPULAR = "marketplace:popular:";
  private static final Duration TRENDING_TTL = Duration.ofMinutes(15);
  private static final Duration POPULAR_TTL = Duration.ofHours(1);

  private final StringRedisTemplate redis;

  public MarketplaceCacheService(StringRedisTemplate redis) {
    this.redis = redis;
  }

  public void cacheTrending(String key, String value) {
    redis.opsForValue().set(PREFIX_TRENDING + key, value, TRENDING_TTL);
  }

  public String getTrending(String key) {
    return redis.opsForValue().get(PREFIX_TRENDING + key);
  }

  public void invalidateTrending(String key) {
    redis.delete(PREFIX_TRENDING + key);
  }

  public void cachePopular(String key, String value) {
    redis.opsForValue().set(PREFIX_POPULAR + key, value, POPULAR_TTL);
  }

  public String getPopular(String key) {
    return redis.opsForValue().get(PREFIX_POPULAR + key);
  }

  public void invalidatePopular(String key) {
    redis.delete(PREFIX_POPULAR + key);
  }

  public void invalidateAllTrending() {
    redis.keys(PREFIX_TRENDING + "*").forEach(redis::delete);
  }

  public void invalidateAllPopular() {
    redis.keys(PREFIX_POPULAR + "*").forEach(redis::delete);
  }

  public void invalidateQuestCaches(UUID questId) {
    redis.delete(PREFIX_TRENDING + questId);
    redis.delete(PREFIX_POPULAR + questId);
  }
}
