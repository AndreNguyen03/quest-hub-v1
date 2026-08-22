package com.questhub.modules.quest.presentation.rest;

import com.questhub.modules.quest.application.command.CreateLearningPathCommand;
import com.questhub.modules.quest.application.command.UpdateLearningPathCommand;
import com.questhub.modules.quest.application.usecase.CreateLearningPathUseCase;
import com.questhub.modules.quest.application.query.GetLearningPathQuery;
import com.questhub.modules.quest.application.usecase.UpdateLearningPathUseCase;
import com.questhub.modules.quest.domain.learningpath.LearningPath;
import com.questhub.modules.quest.application.dto.LearningPathResponse;
import com.questhub.shared.domain.AuthenticatedUser;
import com.questhub.shared.presentation.dto.ApiResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/learning-paths")
@RequiredArgsConstructor
public class LearningPathController {

  private final CreateLearningPathUseCase createLearningPathUseCase;
  private final GetLearningPathQuery GetLearningPathQuery;
  private final UpdateLearningPathUseCase updateLearningPathUseCase;

  private UUID currentUserId() {
    AuthenticatedUser current =
        (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return current.id();
  }

  @PostMapping
  public ResponseEntity<ApiResponse<LearningPathResponse>> create(
      @Valid @RequestBody CreateLearningPathCommand request) {
    LearningPath path = createLearningPathUseCase.create(currentUserId(), request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.ok(LearningPathResponse.from(path)));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<LearningPathResponse>> get(@PathVariable UUID id) {
    LearningPath path = GetLearningPathQuery.get(id, currentUserId());
    return ResponseEntity.ok(ApiResponse.ok(LearningPathResponse.from(path)));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<LearningPathResponse>> update(
      @PathVariable UUID id, @Valid @RequestBody UpdateLearningPathCommand request) {
    LearningPath path = updateLearningPathUseCase.update(id, currentUserId(), request);
    return ResponseEntity.ok(ApiResponse.ok(LearningPathResponse.from(path)));
  }
}



