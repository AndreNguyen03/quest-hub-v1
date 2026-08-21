package com.questhub.modules.world.infrastructure.persistence.world;

import com.questhub.modules.world.domain.world.World;
import com.questhub.modules.world.domain.world.WorldRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaWorldRepository implements WorldRepository {

  private final SpringDataWorldRepository jpa;

  public JpaWorldRepository(SpringDataWorldRepository jpa) {
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




