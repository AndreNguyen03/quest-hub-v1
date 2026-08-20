package com.questhub.modules.world.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataDistrictRepository extends JpaRepository<DistrictEntity, UUID> {

  Optional<DistrictEntity> findByWorldIdAndDomainId(UUID worldId, UUID domainId);

  List<DistrictEntity> findByWorldIdOrderByCreatedAtAsc(UUID worldId);
}