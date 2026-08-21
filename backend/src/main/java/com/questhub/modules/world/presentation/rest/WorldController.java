package com.questhub.modules.world.presentation.rest;

import com.questhub.modules.quest.application.dto.PersonalQuestSummaryDto;
import com.questhub.modules.quest.application.dto.SkillDomainDto;
import com.questhub.modules.quest.application.query.GetSkillDomainQuery;
import com.questhub.modules.world.application.query.GetDistrictDetailQuery;
import com.questhub.modules.world.application.query.GetWorldQuery;
import com.questhub.modules.world.domain.building.Building;
import com.questhub.modules.world.domain.district.District;
import com.questhub.modules.world.application.dto.DistrictDetailResponse;
import com.questhub.modules.world.application.dto.DistrictDetailResponse.BuildingResponse;
import com.questhub.modules.world.application.dto.DistrictDetailResponse.QuestRefResponse;
import com.questhub.modules.world.application.dto.WorldResponse;
import com.questhub.modules.world.application.dto.WorldResponse.DistrictResponse;
import com.questhub.shared.domain.AuthenticatedUser;
import com.questhub.shared.interfaces.dto.ApiResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/world")
@RequiredArgsConstructor
public class WorldController {

  private final GetWorldQuery getWorldQuery;
  private final GetDistrictDetailQuery getDistrictDetailQuery;
  private final GetSkillDomainQuery getSkillDomainQuery;

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<WorldResponse>> myWorld() {
    UUID userId = currentUserId();
    GetWorldQuery.Result result = getWorldQuery.get(userId);
    List<DistrictResponse> districts =
        result.districts().stream().map(this::toDistrictResponse).toList();
    return ResponseEntity.ok(ApiResponse.ok(new WorldResponse(result.world().getId(), districts)));
  }

  @GetMapping("/districts/{districtId}")
  public ResponseEntity<ApiResponse<DistrictDetailResponse>> districtDetail(
      @PathVariable UUID districtId) {
    UUID userId = currentUserId();
    GetDistrictDetailQuery.Result result = getDistrictDetailQuery.get(districtId, userId);
    District district = result.district();
    String domainName = null;
    String domainSlug = null;
    Optional<SkillDomainDto> domain = getSkillDomainQuery.byId(district.getDomainId());
    if (domain.isPresent()) {
      domainName = domain.get().name();
      domainSlug = domain.get().slug();
    }
    DistrictDetailResponse response =
        new DistrictDetailResponse(
            district.getId(),
            district.getDomainId(),
            domainName,
            domainSlug,
            district.getCompletionCount(),
            district.getCompletionCount(),
            result.quests().stream().map(this::toQuestRefResponse).toList(),
            result.buildings().stream().map(this::toBuildingResponse).toList());
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  private QuestRefResponse toQuestRefResponse(PersonalQuestSummaryDto quest) {
    return new QuestRefResponse(
        quest.id(), quest.questId(), quest.title(), quest.status(), quest.progress());
  }

  private BuildingResponse toBuildingResponse(Building building) {
    return new BuildingResponse(
        building.getId(), building.getType(), building.getUnlockedAt(), building.getPosition());
  }

  private DistrictResponse toDistrictResponse(District district) {
    String domainName = null;
    String domainSlug = null;
    Optional<SkillDomainDto> domain = getSkillDomainQuery.byId(district.getDomainId());
    if (domain.isPresent()) {
      domainName = domain.get().name();
      domainSlug = domain.get().slug();
    }
    return new DistrictResponse(
        district.getId(),
        district.getDomainId(),
        domainName,
        domainSlug,
        district.getCompletionCount());
  }

  private UUID currentUserId() {
    AuthenticatedUser current =
        (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return current.id();
  }
}



