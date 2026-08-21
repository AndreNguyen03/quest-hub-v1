package com.questhub.modules.world.domain.world;

import java.util.Optional;
import java.util.UUID;

public interface WorldRepository {

  World save(World world);

  boolean existsByUserId(UUID userId);

  Optional<World> findByUserId(UUID userId);
}


