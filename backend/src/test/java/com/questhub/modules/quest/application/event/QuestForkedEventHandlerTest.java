package com.questhub.modules.quest.application.event;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.questhub.modules.quest.domain.quest.QuestRepository;
import com.questhub.shared.outbox.OutboxEventDispatched;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QuestForkedEventHandlerTest {

  @Mock private QuestRepository questRepository;

  @InjectMocks private QuestForkedEventHandler handler;

  @Test
  void handle_questForked_shouldIncrementForkCount() {
    UUID questId = UUID.randomUUID();
    OutboxEventDispatched event =
        new OutboxEventDispatched(UUID.randomUUID(), "quest.forked", Map.of("questId", questId.toString()));

    handler.handle(event);

    verify(questRepository).incrementForkCount(questId);
  }

  @Test
  void handle_otherEventType_shouldIgnore() {
    UUID questId = UUID.randomUUID();
    OutboxEventDispatched event =
        new OutboxEventDispatched(UUID.randomUUID(), "quest.published", Map.of("questId", questId.toString()));

    handler.handle(event);

    verify(questRepository, never()).incrementForkCount(questId);
  }
}