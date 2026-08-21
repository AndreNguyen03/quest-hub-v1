package com.questhub.modules.admin.presentation.rest;

import com.questhub.modules.admin.application.dto.FeatureFlagResponse;
import com.questhub.modules.admin.application.dto.QuestAdminResponse;
import com.questhub.modules.admin.application.dto.SkillDomainAdminResponse;
import com.questhub.modules.admin.domain.featureflag.FeatureFlag;
import com.questhub.modules.admin.domain.skilldomain.AdminSkillDomain;
import com.questhub.modules.admin.application.query.GetFeatureFlagQuery;
import com.questhub.modules.admin.application.query.ListAllQuestsQuery;
import com.questhub.modules.admin.application.query.ListFeatureFlagsQuery;
import com.questhub.modules.admin.application.query.AdminListSkillDomainsQuery;
import com.questhub.modules.admin.application.usecase.CreateSkillDomainUseCase;
import com.questhub.modules.admin.application.usecase.DeactivateSkillDomainUseCase;
import com.questhub.modules.admin.application.usecase.HideQuestUseCase;
import com.questhub.modules.admin.application.usecase.RestoreQuestUseCase;
import com.questhub.modules.admin.application.usecase.ToggleFeatureFlagUseCase;
import com.questhub.modules.admin.application.usecase.UpdateSkillDomainUseCase;
import com.questhub.shared.interfaces.dto.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

  private final ListAllQuestsQuery listAllQuestsQuery;
  private final AdminListSkillDomainsQuery listSkillDomainsQuery;
  private final ListFeatureFlagsQuery listFeatureFlagsQuery;
  private final GetFeatureFlagQuery getFeatureFlagQuery;
  private final HideQuestUseCase hideQuestUseCase;
  private final RestoreQuestUseCase restoreQuestUseCase;
  private final CreateSkillDomainUseCase createSkillDomainUseCase;
  private final UpdateSkillDomainUseCase updateSkillDomainUseCase;
  private final DeactivateSkillDomainUseCase deactivateSkillDomainUseCase;
  private final ToggleFeatureFlagUseCase toggleFeatureFlagUseCase;

  @GetMapping("/quests")
  public ResponseEntity<ApiResponse<List<QuestAdminResponse>>> listQuests(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int limit) {
    List<QuestAdminResponse> response =
        listAllQuestsQuery.list(page, limit).stream()
            .map(QuestAdminResponse::from)
            .toList();
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  @PostMapping("/quests/{id}/hide")
  public ResponseEntity<ApiResponse<Void>> hideQuest(@PathVariable UUID id) {
    hideQuestUseCase.hide(id);
    return ResponseEntity.ok(ApiResponse.ok(null));
  }

  @PostMapping("/quests/{id}/restore")
  public ResponseEntity<ApiResponse<Void>> restoreQuest(@PathVariable UUID id) {
    restoreQuestUseCase.restore(id);
    return ResponseEntity.ok(ApiResponse.ok(null));
  }

  @GetMapping("/skill-domains")
  public ResponseEntity<ApiResponse<List<SkillDomainAdminResponse>>> listSkillDomains() {
    List<SkillDomainAdminResponse> response =
        listSkillDomainsQuery.list().stream()
            .map(SkillDomainAdminResponse::from)
            .toList();
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  @PostMapping("/skill-domains")
  public ResponseEntity<ApiResponse<SkillDomainAdminResponse>> createSkillDomain(
      @Valid @RequestBody CreateSkillDomainRequest request) {
    AdminSkillDomain domain =
        createSkillDomainUseCase.create(
            request.name(), request.slug(), request.description(), request.icon());
    SkillDomainAdminResponse response = SkillDomainAdminResponse.from(domain);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.ok(response));
  }

  @PutMapping("/skill-domains/{id}")
  public ResponseEntity<ApiResponse<SkillDomainAdminResponse>> updateSkillDomain(
      @PathVariable UUID id, @Valid @RequestBody UpdateSkillDomainRequest request) {
    AdminSkillDomain domain =
        updateSkillDomainUseCase.update(
            id, request.name(), request.slug(), request.description(), request.icon());
    SkillDomainAdminResponse response = SkillDomainAdminResponse.from(domain);
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  @PostMapping("/skill-domains/{id}/deactivate")
  public ResponseEntity<ApiResponse<Void>> deactivateSkillDomain(@PathVariable UUID id) {
    deactivateSkillDomainUseCase.deactivate(id);
    return ResponseEntity.ok(ApiResponse.ok(null));
  }

  @GetMapping("/feature-flags")
  public ResponseEntity<ApiResponse<List<FeatureFlagResponse>>> listFeatureFlags() {
    List<FeatureFlagResponse> response =
        listFeatureFlagsQuery.list().stream()
            .map(FeatureFlagResponse::from)
            .toList();
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  @PutMapping("/feature-flags/{key}")
  public ResponseEntity<ApiResponse<FeatureFlagResponse>> toggleFeatureFlag(
      @PathVariable String key,
      @Valid @RequestBody ToggleFeatureFlagRequest request) {
    FeatureFlag flag =
        toggleFeatureFlagUseCase.toggle(key, request.value(), request.description());
    FeatureFlagResponse response = FeatureFlagResponse.from(flag);
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  public record CreateSkillDomainRequest(
      @NotNull String name,
      @NotNull String slug,
      String description,
      String icon) {}

  public record UpdateSkillDomainRequest(
      @NotNull String name,
      @NotNull String slug,
      String description,
      String icon) {}

  public record ToggleFeatureFlagRequest(
      @NotNull Map<String, Object> value, String description) {}
}
