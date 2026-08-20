package com.questhub.modules.world.infrastructure.persistence;

import com.questhub.modules.world.domain.World;
import com.questhub.modules.world.domain.WorldRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class WorldRepositoryImpl implements WorldRepository {

  private final SpringDataWorldRepository jpa;

  public WorldRepositoryImpl(SpringDataWorldRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public World save(World world) {
    return WorldMapper.toDomain(jpa.save(WorldMapper.toEntity(world)));
  }

  @Override
  public boolean existsByUserId(UUID userId) {
    return jpa.existsByUserId(userId);
  }

  @Override
  public Optional<World> findByUserId(UUID userId) {
    return jpa.findByUserId(userId).map(WorldMapper::toDomain);
  }
}