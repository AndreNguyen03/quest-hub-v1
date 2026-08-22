package com.questhub.modules.world.application.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.questhub.modules.world.application.usecase.BuildingUnlockService;
import com.questhub.modules.world.domain.district.District;
import com.questhub.modules.world.domain.district.DistrictEventRepository;
import com.questhub.modules.world.domain.district.DistrictRepository;
import com.questhub.modules.world.domain.world.World;
import com.questhub.modules.world.domain.world.WorldRepository;
import com.questhub.shared.outbox.OutboxEventDispatched;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskCompletionEventHandlerTest {

  @Mock private WorldRepository worldRepository;
  @Mock private DistrictRepository districtRepository;
  @Mock private DistrictEventRepository districtEventRepository;
  @Mock private BuildingUnlockService buildingUnlockService;

  private TaskCompletionEventHandler handler;

  @BeforeEach
  void setUp() {
    handler =
        new TaskCompletionEventHandler(
            worldRepository, districtRepository, districtEventRepository, buildingUnlockService);
  }

  @Test
  void taskCompleted_whenWorldExists_shouldCreateDistrictAndIncrement() {
    UUID userId = UUID.randomUUID();
    UUID domainId = UUID.randomUUID();
    World world = World.restore(UUID.randomUUID(), userId, "alice", java.time.Instant.now(), 0);
    District district = District.create(world.getId(), domainId);
    when(worldRepository.findByUserId(userId)).thenReturn(Optional.of(world));
    when(districtRepository.findByWorldIdAndDomainId(world.getId(), domainId))
        .thenReturn(Optional.empty());
    when(districtRepository.save(any(District.class))).thenReturn(district);

    handler.handle(event("task.completed", userId, domainId));

    ArgumentCaptor<District> captor = ArgumentCaptor.forClass(District.class);
    verify(districtRepository, times(2)).save(captor.capture());
    assertThat(captor.getAllValues().get(captor.getAllValues().size() - 1).getCompletionCount())
        .isEqualTo(1);
    verify(districtEventRepository).record(any(), any(), org.mockito.ArgumentMatchers.eq(1));
  }

  @Test
  void taskCompleted_whenWorldNotFound_shouldSkip() {
    UUID userId = UUID.randomUUID();
    UUID domainId = UUID.randomUUID();
    when(worldRepository.findByUserId(userId)).thenReturn(Optional.empty());

    handler.handle(event("task.completed", userId, domainId));

    verify(worldRepository, never()).save(any());
    verify(districtRepository, never()).save(any());
    verify(districtEventRepository, never()).record(any(), any(), org.mockito.ArgumentMatchers.anyInt());
  }

  @Test
  void taskUndone_shouldDecrement() {
    UUID userId = UUID.randomUUID();
    UUID domainId = UUID.randomUUID();
    World world = World.create(userId, null);
    District district = District.create(world.getId(), domainId);
    district.incrementCompletion();
    district.incrementCompletion();
    when(worldRepository.findByUserId(userId)).thenReturn(Optional.of(world));
    when(districtRepository.findByWorldIdAndDomainId(world.getId(), domainId))
        .thenReturn(Optional.of(district));
    when(districtRepository.save(any(District.class))).thenReturn(district);

    handler.handle(event("task.undone", userId, domainId));

    ArgumentCaptor<District> captor = ArgumentCaptor.forClass(District.class);
    verify(districtRepository, times(1)).save(captor.capture());
    assertThat(captor.getValue().getCompletionCount()).isEqualTo(1);
    verify(districtEventRepository)
        .record(any(), any(), org.mockito.ArgumentMatchers.eq(-1));
  }

  @Test
  void duplicateEvent_shouldBeIdempotent() {
    UUID userId = UUID.randomUUID();
    UUID domainId = UUID.randomUUID();
    when(districtEventRepository.existsByEventId(any())).thenReturn(true);

    handler.handle(event("task.completed", userId, domainId));

    verify(worldRepository, never()).save(any());
    verify(districtRepository, never()).save(any());
    verify(districtEventRepository, never())
        .record(any(), any(), org.mockito.ArgumentMatchers.anyInt());
  }

  @Test
  void eventWithoutDomain_shouldSkip() {
    UUID userId = UUID.randomUUID();
    OutboxEventDispatched event =
        new OutboxEventDispatched(
            UUID.randomUUID(), "task.completed", Map.of("userId", userId.toString()));

    handler.handle(event);

    verify(worldRepository, never()).save(any());
    verify(districtRepository, never()).save(any());
  }

  @Test
  void unrelatedEvent_shouldSkip() {
    handler.handle(
        new OutboxEventDispatched(
            UUID.randomUUID(), "quest.forked", Map.of("questId", UUID.randomUUID().toString())));

    verify(worldRepository, never()).save(any());
    verify(districtRepository, never()).save(any());
  }

  private OutboxEventDispatched event(String eventType, UUID userId, UUID domainId) {
    return new OutboxEventDispatched(
        UUID.randomUUID(),
        eventType,
        Map.of("userId", userId.toString(), "skillDomainId", domainId.toString()));
  }
}
