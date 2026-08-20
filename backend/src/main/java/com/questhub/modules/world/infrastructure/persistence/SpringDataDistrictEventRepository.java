package com.questhub.modules.world.infrastructure.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataDistrictEventRepository extends JpaRepository<DistrictEventEntity, UUID> {

  boolean existsByEventId(UUID eventId);
}