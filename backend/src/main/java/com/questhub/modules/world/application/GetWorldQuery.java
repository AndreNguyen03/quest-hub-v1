package com.questhub.modules.world.application;

import com.questhub.modules.world.domain.District;
import com.questhub.modules.world.domain.DistrictRepository;
import com.questhub.modules.world.domain.World;
import com.questhub.modules.world.domain.WorldRepository;
import com.questhub.shared.annotation.UseCase;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class GetWorldQuery {

  private final WorldRepository worldRepository;
  private final DistrictRepository districtRepository;

  public record Result(World world, List<District> districts) {}

  @Transactional(
      readOnly = true,
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public Result get(UUID userId) {
    World world =
        worldRepository
            .findByUserId(userId)
            .orElseGet(() -> worldRepository.save(World.create(userId)));
    List<District> districts = districtRepository.findByWorldId(world.getId());
    log.info(
        "World viewed userId={} worldId={} districts={}", userId, world.getId(), districts.size());
    return new Result(world, districts);
  }
}