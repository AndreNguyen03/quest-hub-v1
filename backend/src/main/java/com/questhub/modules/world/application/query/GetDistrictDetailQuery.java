package com.questhub.modules.world.application.query;

import com.questhub.modules.quest.application.api.QuestPublicApi;
import com.questhub.modules.quest.application.dto.PersonalQuestSummaryDto;
import com.questhub.modules.world.domain.building.Building;
import com.questhub.modules.world.domain.building.BuildingRepository;
import com.questhub.modules.world.domain.district.District;
import com.questhub.modules.world.domain.district.DistrictRepository;
import com.questhub.modules.world.application.usecase.BuildingUnlockService;
import com.questhub.modules.world.domain.world.World;
import com.questhub.modules.world.domain.world.WorldRepository;
import com.questhub.shared.annotation.UseCase;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class GetDistrictDetailQuery {

  private static final Logger log = LoggerFactory.getLogger(GetDistrictDetailQuery.class);

  private final DistrictRepository districtRepository;
  private final WorldRepository worldRepository;
  private final BuildingRepository buildingRepository;
  private final BuildingUnlockService buildingUnlockService;
  private final QuestPublicApi questPublicApi;

  public record Result(
      District district, List<Building> buildings, List<PersonalQuestSummaryDto> quests) {}

  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public Result get(UUID districtId, UUID userId) {
    District district =
        districtRepository
            .findById(districtId)
            .orElseThrow(
                () -> BusinessException.notFound(ErrorCodes.NOT_FOUND, "Không tìm thấy district"));
    World world =
        worldRepository
            .findByUserId(userId)
            .orElseThrow(
                () -> BusinessException.notFound(ErrorCodes.NOT_FOUND, "Không tìm thấy world"));
    if (!world.getId().equals(district.getWorldId())) {
      log.warn(
          "District not owned districtId={} worldId={} userId={}",
          districtId, district.getWorldId(), userId);
      throw BusinessException.notFound(ErrorCodes.NOT_FOUND, "Không tìm thấy district");
    }

    buildingUnlockService.unlockFor(district);
    List<Building> buildings = buildingRepository.findByDistrictId(district.getId());
    List<PersonalQuestSummaryDto> quests =
        questPublicApi.personalQuestsByUserAndDomain(userId, district.getDomainId());
    log.info(
        "District detail viewed districtId={} userId={} buildings={} quests={}",
        districtId, userId, buildings.size(), quests.size());
    return new Result(district, buildings, quests);
  }
}

