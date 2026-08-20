package com.questhub.modules.world.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataBuildingRepository extends JpaRepository<BuildingEntity, UUID> {

  List<BuildingEntity> findByDistrictIdOrderByPositionAsc(UUID districtId);

  boolean existsByDistrictIdAndPosition(UUID districtId, int position);
}