package com.questhub.modules.world.application.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.questhub.modules.world.application.usecase.AchievementUnlockService;
import com.questhub.modules.world.domain.world.World;
import com.questhub.modules.world.domain.world.WorldRepository;
import com.questhub.shared.outbox.OutboxEventDispatched;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QuestCompletionEventHandlerTest {

  @Mock private WorldRepository worldRepository;
  @Mock private AchievementUnlockService achievementUnlockService;

  @InjectMocks private QuestCompletionEventHandler handler;

  @Test
  void handle_questCompleted_shouldIncrementCountAndSave() {
    UUID userId = UUID.randomUUID();
    World world = World.restore(UUID.randomUUID(), userId, "user1", Instant.now(), 2);
    when(worldRepository.findByUserId(userId)).thenReturn(Optional.of(world));
    when(worldRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    handler.handle(event("quest.completed", userId));

    ArgumentCaptor<World> captor = ArgumentCaptor.forClass(World.class);
    verify(worldRepository).save(captor.capture());
    assertThat(captor.getValue().getQuestCompletedCount()).isEqualTo(3);
  }

  @Test
  void handle_questCompleted_shouldEvaluateAchievements() {
    UUID userId = UUID.randomUUID();
    World world = World.restore(UUID.randomUUID(), userId, "user1", Instant.now(), 0);
    when(worldRepository.findByUserId(userId)).thenReturn(Optional.of(world));
    when(worldRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    handler.handle(event("quest.completed", userId));

    verify(achievementUnlockService).evaluate(userId);
  }

  @Test
  void handle_questReopened_shouldDecrementCountAndSave() {
    UUID userId = UUID.randomUUID();
    World world = World.restore(UUID.randomUUID(), userId, "user1", Instant.now(), 3);
    when(worldRepository.findByUserId(userId)).thenReturn(Optional.of(world));
    when(worldRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    handler.handle(event("quest.reopened", userId));

    ArgumentCaptor<World> captor = ArgumentCaptor.forClass(World.class);
    verify(worldRepository).save(captor.capture());
    assertThat(captor.getValue().getQuestCompletedCount()).isEqualTo(2);
  }

  @Test
  void handle_questReopened_shouldNotEvaluateAchievements() {
    UUID userId = UUID.randomUUID();
    World world = World.restore(UUID.randomUUID(), userId, "user1", Instant.now(), 1);
    when(worldRepository.findByUserId(userId)).thenReturn(Optional.of(world));
    when(worldRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    handler.handle(event("quest.reopened", userId));

    verify(achievementUnlockService, never()).evaluate(any());
  }

  @Test
  void handle_unknownEventType_shouldSkip() {
    handler.handle(event("task.completed", UUID.randomUUID()));

    verify(worldRepository, never()).findByUserId(any());
    verify(achievementUnlockService, never()).evaluate(any());
  }

  @Test
  void handle_worldNotFound_shouldSkipGracefully() {
    UUID userId = UUID.randomUUID();
    when(worldRepository.findByUserId(userId)).thenReturn(Optional.empty());

    handler.handle(event("quest.completed", userId));

    verify(worldRepository, never()).save(any());
    verify(achievementUnlockService, never()).evaluate(any());
  }

  @Test
  void handle_questReopened_countAlreadyZero_shouldNotGoBelowZero() {
    UUID userId = UUID.randomUUID();
    World world = World.restore(UUID.randomUUID(), userId, "user1", Instant.now(), 0);
    when(worldRepository.findByUserId(userId)).thenReturn(Optional.of(world));
    when(worldRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    handler.handle(event("quest.reopened", userId));

    ArgumentCaptor<World> captor = ArgumentCaptor.forClass(World.class);
    verify(worldRepository).save(captor.capture());
    assertThat(captor.getValue().getQuestCompletedCount()).isZero();
  }

  private OutboxEventDispatched event(String type, UUID userId) {
    return new OutboxEventDispatched(UUID.randomUUID(), type, Map.of("userId", userId.toString()));
  }
}
