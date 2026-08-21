package com.questhub.modules.world.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.questhub.modules.world.domain.building.Building;
import com.questhub.modules.world.domain.building.BuildingRepository;
import com.questhub.modules.world.domain.district.District;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BuildingUnlockServiceTest {

  @Mock private BuildingRepository buildingRepository;

  private BuildingUnlockService service;

  @BeforeEach
  void setUp() {
    service = new BuildingUnlockService(buildingRepository);
  }

  @Test
  void noCompletion_shouldNotUnlock() {
    District district = District.create(UUID.randomUUID(), UUID.randomUUID());

    service.unlockFor(district);

    verify(buildingRepository, never()).save(any());
  }

  @Test
  void countReachesFirstThreshold_shouldUnlockHouse() {
    District district = District.create(UUID.randomUUID(), UUID.randomUUID());
    district.incrementCompletion();
    when(buildingRepository.existsByDistrictIdAndPosition(any(), org.mockito.ArgumentMatchers.anyInt())).thenReturn(false);
    when(buildingRepository.save(any(Building.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    service.unlockFor(district);

    ArgumentCaptor<Building> captor = ArgumentCaptor.forClass(Building.class);
    verify(buildingRepository, times(1)).save(captor.capture());
    assertThat(captor.getValue().getType()).isEqualTo("house");
    assertThat(captor.getValue().getPosition()).isZero();
  }

  @Test
  void countAtFive_shouldUnlockHouseAndSchool() {
    District district = District.create(UUID.randomUUID(), UUID.randomUUID());
    for (int i = 0; i < 5; i++) {
      district.incrementCompletion();
    }
    when(buildingRepository.existsByDistrictIdAndPosition(any(), org.mockito.ArgumentMatchers.anyInt())).thenReturn(false);
    when(buildingRepository.save(any(Building.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    service.unlockFor(district);

    ArgumentCaptor<Building> captor = ArgumentCaptor.forClass(Building.class);
    verify(buildingRepository, times(2)).save(captor.capture());
    assertThat(captor.getAllValues().get(0).getType()).isEqualTo("house");
    assertThat(captor.getAllValues().get(1).getType()).isEqualTo("school");
  }

  @Test
  void alreadyUnlockedPosition_shouldBeIdempotent() {
    District district = District.create(UUID.randomUUID(), UUID.randomUUID());
    district.incrementCompletion();
    when(buildingRepository.existsByDistrictIdAndPosition(any(), org.mockito.ArgumentMatchers.anyInt())).thenReturn(true);

    service.unlockFor(district);

    verify(buildingRepository, never()).save(any());
  }
}

