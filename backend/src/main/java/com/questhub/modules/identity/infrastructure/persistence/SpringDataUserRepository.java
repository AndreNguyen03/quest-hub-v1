package com.questhub.modules.identity.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataUserRepository extends JpaRepository<UserEntity, UUID> {

  Optional<UserEntity> findByEmail(String email);

  boolean existsByEmail(String email);

  boolean existsByUsername(String username);
}