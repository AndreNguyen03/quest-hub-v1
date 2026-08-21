package com.questhub.modules.marketplace.application.event;

import com.questhub.modules.marketplace.infrastructure.redis.MarketplaceCacheService;
import com.questhub.shared.outbox.OutboxEventDispatched;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component("marketplaceQuestForkedEventHandler")
public class QuestForkedEventHandler {

  private static final String EVENT_TYPE = "quest.forked";

  private final MarketplaceCacheService cacheService;

  public QuestForkedEventHandler(MarketplaceCacheService cacheService) {
    this.cacheService = cacheService;
  }

  @EventListener
  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public void handle(OutboxEventDispatched event) {
    if (!EVENT_TYPE.equals(event.eventType())) {
      return;
    }

    String questIdStr = (String) event.payload().get("questId");
    if (questIdStr == null) {
      log.info("Quest forked event without questId skipped eventId={}", event.eventId());
      return;
    }

    UUID questId = UUID.fromString(questIdStr);
    cacheService.invalidateQuestCaches(questId);
    log.info("Quest caches invalidated questId={} eventId={}", questId, event.eventId());
  }
}
