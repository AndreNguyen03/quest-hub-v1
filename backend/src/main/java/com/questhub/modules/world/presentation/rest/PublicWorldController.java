package com.questhub.modules.world.presentation.rest;

import com.questhub.modules.quest.application.api.QuestPublicApi;
import com.questhub.modules.quest.application.dto.SkillDomainDto;
import com.questhub.modules.world.application.query.GetUserWorldQuery;
import com.questhub.modules.world.domain.district.District;
import com.questhub.modules.world.application.dto.WorldResponse;
import com.questhub.modules.world.application.dto.WorldResponse.DistrictResponse;
import com.questhub.shared.presentation.dto.ApiResponse;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/world")
@RequiredArgsConstructor
public class PublicWorldController {

  private final GetUserWorldQuery getUserWorldQuery;
  private final QuestPublicApi questPublicApi;

  @GetMapping("/users/{username}")
  public ResponseEntity<ApiResponse<WorldResponse>> getUserWorld(@PathVariable String username) {
    GetUserWorldQuery.Result result = getUserWorldQuery.getByUsername(username);
    List<DistrictResponse> districts =
        result.districts().stream().map(this::toDistrictResponse).toList();
    return ResponseEntity.ok(ApiResponse.ok(new WorldResponse(result.world().getId(), districts)));
  }

  private DistrictResponse toDistrictResponse(District district) {
    String domainName = null;
    String domainSlug = null;
    Optional<SkillDomainDto> domain = questPublicApi.findSkillDomain(district.getDomainId());
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
}



