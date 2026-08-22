package com.questhub.modules.world.application.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.questhub.modules.world.domain.world.World;
import com.questhub.modules.world.domain.world.WorldRepository;
import com.questhub.shared.outbox.OutboxEventDispatched;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserRegisteredEventHandlerTest {

  @Mock private WorldRepository worldRepository;

  @InjectMocks private UserRegisteredEventHandler handler;

  @Test
  void handle_userRegistered_shouldCreateWorldWithCorrectUserId() {
    UUID userId = UUID.randomUUID();
    when(worldRepository.existsByUserId(userId)).thenReturn(false);
    when(worldRepository.save(any(World.class))).thenAnswer(inv -> inv.getArgument(0));

    handler.handle(event(userId, "andre_nguyen"));

    verify(worldRepository).save(argThat(w -> w.getUserId().equals(userId)
        && "andre_nguyen".equals(w.getUsername())));
  }

  @Test
  void handle_userRegistered_worldAlreadyExists_shouldSkip() {
    UUID userId = UUID.randomUUID();
    when(worldRepository.existsByUserId(userId)).thenReturn(true);

    handler.handle(event(userId, "andre_nguyen"));

    verify(worldRepository, never()).save(any());
  }

  @Test
  void handle_otherEventType_shouldIgnore() {
    UUID userId = UUID.randomUUID();

    handler.handle(new OutboxEventDispatched(
        UUID.randomUUID(), "quest.completed", Map.of("userId", userId.toString())));

    verify(worldRepository, never()).existsByUserId(any());
    verify(worldRepository, never()).save(any());
  }

  private OutboxEventDispatched event(UUID userId, String username) {
    return new OutboxEventDispatched(
        UUID.randomUUID(),
        "user.registered",
        Map.of("userId", userId.toString(), "username", username));
  }
}
