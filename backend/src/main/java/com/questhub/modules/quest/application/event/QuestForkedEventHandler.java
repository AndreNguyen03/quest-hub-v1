package com.questhub.modules.quest.application.event;

import com.questhub.modules.quest.domain.quest.QuestRepository;
import com.questhub.shared.outbox.OutboxEventDispatched;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class QuestForkedEventHandler {

  private static final String EVENT_TYPE = "quest.forked";

  private final QuestRepository questRepository;

  public QuestForkedEventHandler(QuestRepository questRepository) {
    this.questRepository = questRepository;
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

    UUID questId = UUID.fromString((String) event.payload().get("questId"));
    questRepository.incrementForkCount(questId);
    log.info("Fork count incremented questId={}", questId);
  }
}