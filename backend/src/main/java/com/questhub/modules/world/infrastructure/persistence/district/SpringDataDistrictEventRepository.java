package com.questhub.modules.world.infrastructure.persistence.district;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataDistrictEventRepository extends JpaRepository<DistrictEventJpaEntity, UUID> {

  boolean existsByEventId(UUID eventId);
}



