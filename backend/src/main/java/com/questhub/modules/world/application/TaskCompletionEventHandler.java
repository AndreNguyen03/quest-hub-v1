package com.questhub.modules.world.application;

import com.questhub.modules.world.domain.District;
import com.questhub.modules.world.domain.DistrictEventRepository;
import com.questhub.modules.world.domain.DistrictRepository;
import com.questhub.modules.world.domain.World;
import com.questhub.modules.world.domain.WorldRepository;
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
public class TaskCompletionEventHandler {

  private static final String TASK_COMPLETED = "task.completed";
  private static final String TASK_UNDONE = "task.undone";

  private final WorldRepository worldRepository;
  private final DistrictRepository districtRepository;
  private final DistrictEventRepository districtEventRepository;
  private final BuildingUnlockService buildingUnlockService;

  public TaskCompletionEventHandler(
      WorldRepository worldRepository,
      DistrictRepository districtRepository,
      DistrictEventRepository districtEventRepository,
      BuildingUnlockService buildingUnlockService) {
    this.worldRepository = worldRepository;
    this.districtRepository = districtRepository;
    this.districtEventRepository = districtEventRepository;
    this.buildingUnlockService = buildingUnlockService;
  }

  @EventListener
  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public void handle(OutboxEventDispatched event) {
    if (!TASK_COMPLETED.equals(event.eventType()) && !TASK_UNDONE.equals(event.eventType())) {
      return;
    }

    String domainIdValue = (String) event.payload().get("skillDomainId");
    if (domainIdValue == null) {
      log.info("Task event without domain skipped eventId={}", event.eventId());
      return;
    }

    if (districtEventRepository.existsByEventId(event.eventId())) {
      log.info("District event already applied eventId={}, skip", event.eventId());
      return;
    }

    UUID domainId = UUID.fromString(domainIdValue);
    UUID userId = UUID.fromString((String) event.payload().get("userId"));

    World world =
        worldRepository
            .findByUserId(userId)
            .orElseGet(() -> worldRepository.save(World.create(userId)));

    District district =
        districtRepository
            .findByWorldIdAndDomainId(world.getId(), domainId)
            .orElseGet(() -> districtRepository.save(District.create(world.getId(), domainId)));

    int before = district.getCompletionCount();
    if (TASK_COMPLETED.equals(event.eventType())) {
      district.incrementCompletion();
    } else {
      district.decrementCompletion();
    }
    districtRepository.save(district);
    buildingUnlockService.unlockFor(district);
    int delta = district.getCompletionCount() - before;

    districtEventRepository.record(event.eventId(), district.getId(), delta);
    log.info(
        "District completion applied eventId={} eventType={} districtId={} count={} delta={}",
        event.eventId(), event.eventType(), district.getId(), district.getCompletionCount(), delta);
  }
}