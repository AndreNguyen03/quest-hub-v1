package com.questhub.modules.world.application.event;

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
public class UserRegisteredEventHandler {

  private static final Logger log = LoggerFactory.getLogger(UserRegisteredEventHandler.class);

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

    String username = (String) event.payload().get("username");
    World world = worldRepository.save(World.create(userId, username));
    log.info("World created worldId={} userId={}", world.getId(), userId);
  }
}
