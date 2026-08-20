package com.questhub.modules.world.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataWorldRepository extends JpaRepository<WorldEntity, UUID> {

  boolean existsByUserId(UUID userId);

  Optional<WorldEntity> findByUserId(UUID userId);
}