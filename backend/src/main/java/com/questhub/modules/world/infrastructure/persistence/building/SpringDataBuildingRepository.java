package com.questhub.modules.world.infrastructure.persistence.building;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataBuildingRepository extends JpaRepository<BuildingJpaEntity, UUID> {

  List<BuildingJpaEntity> findByDistrictIdOrderByPositionAsc(UUID districtId);

  boolean existsByDistrictIdAndPosition(UUID districtId, int position);
}



