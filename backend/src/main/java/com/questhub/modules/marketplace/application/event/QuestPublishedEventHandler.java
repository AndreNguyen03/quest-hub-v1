package com.questhub.modules.marketplace.application.event;

import com.questhub.modules.marketplace.infrastructure.elasticsearch.QuestDocument;
import com.questhub.modules.marketplace.infrastructure.elasticsearch.QuestIndexer;
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
@Component("marketplaceQuestPublishedEventHandler")
public class QuestPublishedEventHandler {

  private static final String EVENT_TYPE = "quest.published";

  private final QuestIndexer questIndexer;

  public QuestPublishedEventHandler(QuestIndexer questIndexer) {
    this.questIndexer = questIndexer;
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
    String domainIdStr = (String) event.payload().get("skillDomainId");
    if (questIdStr == null) {
      log.info("Quest published event without questId skipped eventId={}", event.eventId());
      return;
    }

    UUID questId = UUID.fromString(questIdStr);
    UUID domainId = domainIdStr != null ? UUID.fromString(domainIdStr) : null;

    QuestDocument doc =
        new QuestDocument(
            questId,
            (String) event.payload().get("title"),
            (String) event.payload().get("description"),
            (String) event.payload().get("difficulty"),
            event.payload().get("learningPathId") != null ? UUID.fromString((String) event.payload().get("learningPathId")) : null,
            domainId,
            ((Number) event.payload().getOrDefault("taskCount", 0)).intValue(),
            null,
            0,
            event.payload().get("publishedAt") != null ? java.time.Instant.parse((String) event.payload().get("publishedAt")) : null);
    questIndexer.index(doc);
    log.info("Quest indexed from event questId={} eventId={}", questId, event.eventId());
  }
}
