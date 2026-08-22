package com.questhub.modules.world.application.usecase;

import com.questhub.modules.world.domain.building.Building;
import com.questhub.modules.world.domain.building.BuildingRepository;
import com.questhub.modules.world.domain.district.District;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class BuildingUnlockService {

  private static final int[] THRESHOLDS = {1, 5, 10, 20, 35, 50};
  private static final String[] TYPES = {"house", "school", "library", "gym", "museum", "tower"};

  private final BuildingRepository buildingRepository;

  public BuildingUnlockService(BuildingRepository buildingRepository) {
    this.buildingRepository = buildingRepository;
  }

  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public void unlockFor(District district) {
    for (int i = 0; i < THRESHOLDS.length; i++) {
      if (THRESHOLDS[i] <= district.getCompletionCount()
          && !buildingRepository.existsByDistrictIdAndPosition(district.getId(), i)) {
        Building building = buildingRepository.save(Building.create(district.getId(), TYPES[i], i));
        log.info(
            "Building unlocked districtId={} position={} type={}",
            district.getId(), i, building.getType());
      }
    }
  }
}
