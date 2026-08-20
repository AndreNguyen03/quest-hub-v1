package com.questhub.modules.world.application;

import com.questhub.modules.quest.domain.personalquest.PersonalQuest;
import com.questhub.modules.quest.domain.personalquest.PersonalQuestRepository;
import com.questhub.modules.quest.domain.personalquest.PersonalQuestStatus;
import com.questhub.modules.world.domain.Building;
import com.questhub.modules.world.domain.BuildingRepository;
import com.questhub.modules.world.domain.District;
import com.questhub.modules.world.domain.DistrictRepository;
import com.questhub.modules.world.domain.World;
import com.questhub.modules.world.domain.WorldRepository;
import com.questhub.shared.annotation.UseCase;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
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
public class GetDistrictDetailQuery {

  private final DistrictRepository districtRepository;
  private final WorldRepository worldRepository;
  private final BuildingRepository buildingRepository;
  private final BuildingUnlockService buildingUnlockService;
  private final PersonalQuestRepository personalQuestRepository;

  public record Result(District district, List<Building> buildings, List<PersonalQuest> quests) {}

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
    List<PersonalQuest> quests =
        personalQuestRepository.findByUserIdAndDomainIdAndStatusIn(
            userId,
            district.getDomainId(),
            List.of(PersonalQuestStatus.ACTIVE, PersonalQuestStatus.COMPLETED));
    log.info(
        "District detail viewed districtId={} userId={} buildings={} quests={}",
        districtId, userId, buildings.size(), quests.size());
    return new Result(district, buildings, quests);
  }
}