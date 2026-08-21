package com.questhub.modules.world.domain.district;

import static org.assertj.core.api.Assertions.assertThat;

import com.questhub.modules.world.domain.district.District;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DistrictTest {

  @Test
  void create_shouldStartAtZero() {
    District district = District.create(UUID.randomUUID(), UUID.randomUUID());

    assertThat(district.getCompletionCount()).isZero();
  }

  @Test
  void incrementCompletion_shouldIncrease() {
    District district = District.create(UUID.randomUUID(), UUID.randomUUID());

    district.incrementCompletion();
    district.incrementCompletion();

    assertThat(district.getCompletionCount()).isEqualTo(2);
  }

  @Test
  void decrementCompletion_shouldDecrease() {
    District district = District.create(UUID.randomUUID(), UUID.randomUUID());
    district.incrementCompletion();
    district.incrementCompletion();

    district.decrementCompletion();

    assertThat(district.getCompletionCount()).isEqualTo(1);
  }

  @Test
  void decrementCompletion_shouldNotGoBelowZero() {
    District district = District.create(UUID.randomUUID(), UUID.randomUUID());

    district.decrementCompletion();
    district.decrementCompletion();

    assertThat(district.getCompletionCount()).isZero();
  }
}