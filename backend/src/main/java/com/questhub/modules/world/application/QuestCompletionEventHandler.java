package com.questhub.modules.world.application;

import com.questhub.modules.world.domain.world.World;
import com.questhub.modules.world.domain.world.WorldRepository;
import com.questhub.shared.outbox.OutboxEventDispatched;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class QuestCompletionEventHandler {

  private static final Logger log = LoggerFactory.getLogger(QuestCompletionEventHandler.class);
  private static final String QUEST_COMPLETED = "quest.completed";
  private static final String QUEST_REOPENED = "quest.reopened";

  private final WorldRepository worldRepository;
  private final AchievementUnlockService achievementUnlockService;

  public QuestCompletionEventHandler(
      WorldRepository worldRepository,
      AchievementUnlockService achievementUnlockService) {
    this.worldRepository = worldRepository;
    this.achievementUnlockService = achievementUnlockService;
  }

  @EventListener
  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public void handle(OutboxEventDispatched event) {
    if (!QUEST_COMPLETED.equals(event.eventType()) && !QUEST_REOPENED.equals(event.eventType())) {
      return;
    }

    UUID userId = UUID.fromString((String) event.payload().get("userId"));

    World world = worldRepository.findByUserId(userId).orElse(null);
    if (world == null) {
      log.warn("World not found for userId={}, {} skipped", userId, event.eventType());
      return;
    }

    if (QUEST_COMPLETED.equals(event.eventType())) {
      world.incrementQuestCount();
      worldRepository.save(world);
      log.info("Quest completion recorded userId={} questCompletedCount={}",
          userId, world.getQuestCompletedCount());
      achievementUnlockService.evaluate(userId);
    } else {
      world.decrementQuestCount();
      worldRepository.save(world);
      log.info("Quest reopened — count decremented userId={} questCompletedCount={}",
          userId, world.getQuestCompletedCount());
    }
  }
}
