package com.questhub.modules.world.infrastructure.persistence.district;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataDistrictRepository extends JpaRepository<DistrictJpaEntity, UUID> {

  Optional<DistrictJpaEntity> findByWorldIdAndDomainId(UUID worldId, UUID domainId);

  List<DistrictJpaEntity> findByWorldIdOrderByCreatedAtAsc(UUID worldId);
}



