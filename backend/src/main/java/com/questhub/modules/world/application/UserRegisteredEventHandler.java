package com.questhub.modules.world.application;

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
public class UserRegisteredEventHandler {

  private static final String EVENT_TYPE = "user.registered";

  private final WorldRepository worldRepository;

  public UserRegisteredEventHandler(WorldRepository worldRepository) {
    this.worldRepository = worldRepository;
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

    UUID userId = UUID.fromString((String) event.payload().get("userId"));
    if (worldRepository.existsByUserId(userId)) {
      log.info("World already exists for userId={}, skip", userId);
      return;
    }

    World world = worldRepository.save(World.create(userId));
    log.info("World created worldId={} userId={}", world.getId(), userId);
  }
}