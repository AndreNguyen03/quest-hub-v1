package com.questhub.modules.world.infrastructure.persistence.world;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataWorldRepository extends JpaRepository<WorldJpaEntity, UUID> {

  boolean existsByUserId(UUID userId);

  Optional<WorldJpaEntity> findByUserId(UUID userId);
}



