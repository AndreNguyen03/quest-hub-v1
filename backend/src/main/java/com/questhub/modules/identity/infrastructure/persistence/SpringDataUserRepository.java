package com.questhub.modules.identity.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, UUID> {

  Optional<UserJpaEntity> findByEmail(String email);

  Optional<UserJpaEntity> findByUsername(String username);

  boolean existsByEmail(String email);

  boolean existsByUsername(String username);
}
