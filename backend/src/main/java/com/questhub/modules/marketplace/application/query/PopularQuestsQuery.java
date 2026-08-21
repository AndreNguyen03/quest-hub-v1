package com.questhub.modules.marketplace.application.query;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.questhub.modules.quest.application.dto.QuestDto;
import com.questhub.modules.marketplace.infrastructure.redis.MarketplaceCacheService;
import com.questhub.shared.annotation.UseCase;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class PopularQuestsQuery {

  private static final String CACHE_KEY_PREFIX = "marketplace:popular:quests:";

  private final com.questhub.modules.quest.application.query.GetPopularQuestsQuery delegate;
  private final MarketplaceCacheService cacheService;
  private final ObjectMapper objectMapper;

  @Transactional(
      readOnly = true,
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public List<QuestDto> get(int limit) {
    String key = CACHE_KEY_PREFIX + limit;
    String cached = cacheService.getPopular(key);
    if (cached != null) {
      try {
        return objectMapper.readValue(cached, new TypeReference<List<QuestDto>>() {});
      } catch (Exception e) {
        log.warn("Failed to deserialize cached popular quests", e);
      }
    }

    List<QuestDto> quests = delegate.get(limit);
    try {
      cacheService.cachePopular(key, objectMapper.writeValueAsString(quests));
    } catch (Exception e) {
      log.warn("Failed to serialize popular quests for cache", e);
    }
    return quests;
  }
}
