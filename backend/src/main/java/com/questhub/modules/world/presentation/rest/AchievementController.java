package com.questhub.modules.world.presentation.rest;

import com.questhub.modules.world.application.dto.AchievementResponse;
import com.questhub.modules.world.application.dto.AchievementResponseMapper;
import com.questhub.modules.world.application.query.GetAchievementsQuery;
import com.questhub.shared.domain.AuthenticatedUser;
import com.questhub.shared.interfaces.dto.ApiResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/world")
@RequiredArgsConstructor
public class AchievementController {

  private final GetAchievementsQuery getAchievementsQuery;

  @GetMapping("/achievements")
  public ResponseEntity<ApiResponse<List<AchievementResponse>>> achievements(
      @RequestParam(required = false, defaultValue = "false") boolean onlyLocked) {
    UUID userId = currentUserId();
    List<AchievementResponse> response =
        getAchievementsQuery.get(userId, onlyLocked).stream()
            .map(AchievementResponseMapper::toResponse)
            .toList();
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  private UUID currentUserId() {
    AuthenticatedUser current =
        (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return current.id();
  }
}

