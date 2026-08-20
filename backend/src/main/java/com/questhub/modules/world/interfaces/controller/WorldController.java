package com.questhub.modules.world.interfaces.controller;

import com.questhub.modules.quest.domain.personalquest.PersonalQuest;
import com.questhub.modules.quest.domain.skilldomain.SkillDomainRepository;
import com.questhub.modules.world.application.GetDistrictDetailQuery;
import com.questhub.modules.world.application.GetWorldQuery;
import com.questhub.modules.world.domain.Building;
import com.questhub.modules.world.domain.District;
import com.questhub.modules.world.interfaces.dto.DistrictDetailResponse;
import com.questhub.modules.world.interfaces.dto.DistrictDetailResponse.BuildingResponse;
import com.questhub.modules.world.interfaces.dto.DistrictDetailResponse.QuestRefResponse;
import com.questhub.modules.world.interfaces.dto.WorldResponse;
import com.questhub.modules.world.interfaces.dto.WorldResponse.DistrictResponse;
import com.questhub.shared.domain.AuthenticatedUser;
import com.questhub.shared.interfaces.dto.ApiResponse;
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
  private final SkillDomainRepository skillDomainRepository;

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<WorldResponse>> myWorld() {
    UUID userId = currentUserId();
    GetWorldQuery.Result result = getWorldQuery.get(userId);
    java.util.List<DistrictResponse> districts =
        result.districts().stream()
            .map(this::toDistrictResponse)
            .toList();
    return ResponseEntity.ok(
        ApiResponse.ok(new WorldResponse(result.world().getId(), districts)));
  }

  @GetMapping("/districts/{districtId}")
  public ResponseEntity<ApiResponse<DistrictDetailResponse>> districtDetail(
      @PathVariable UUID districtId) {
    UUID userId = currentUserId();
    GetDistrictDetailQuery.Result result = getDistrictDetailQuery.get(districtId, userId);
    District district = result.district();
    String domainName = null;
    String domainSlug = null;
    var domain = skillDomainRepository.findById(district.getDomainId());
    if (domain.isPresent()) {
      domainName = domain.get().getName();
      domainSlug = domain.get().getSlug();
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

  private QuestRefResponse toQuestRefResponse(PersonalQuest quest) {
    return new QuestRefResponse(
        quest.getId(), quest.getQuestId(), quest.getTitle(), quest.getStatus().name(), quest.getProgress());
  }

  private BuildingResponse toBuildingResponse(Building building) {
    return new BuildingResponse(
        building.getId(), building.getType(), building.getUnlockedAt(), building.getPosition());
  }

  private DistrictResponse toDistrictResponse(District district) {
    String domainName = null;
    String domainSlug = null;
    var domain = skillDomainRepository.findById(district.getDomainId());
    if (domain.isPresent()) {
      domainName = domain.get().getName();
      domainSlug = domain.get().getSlug();
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