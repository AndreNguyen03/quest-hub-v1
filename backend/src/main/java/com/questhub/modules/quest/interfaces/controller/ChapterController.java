package com.questhub.modules.quest.interfaces.controller;

import com.questhub.modules.quest.application.request.AddChapterRequest;
import com.questhub.modules.quest.application.request.UpdateChapterRequest;
import com.questhub.modules.quest.application.usecase.AddChapterUseCase;
import com.questhub.modules.quest.application.usecase.RemoveChapterUseCase;
import com.questhub.modules.quest.application.usecase.UpdateChapterUseCase;
import com.questhub.modules.quest.domain.quest.Quest;
import com.questhub.modules.quest.interfaces.dto.QuestResponse;
import com.questhub.shared.domain.AuthenticatedUser;
import com.questhub.shared.interfaces.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/quests")
public class ChapterController {
  private final AddChapterUseCase addChapterUseCase;
  private final UpdateChapterUseCase updateChapterUseCase;
  private final RemoveChapterUseCase removeChapterUseCase;

  private UUID currentUserId() {
    AuthenticatedUser current =
        (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return current.id();
  }

  @PostMapping("/{questId}/chapters")
  public ResponseEntity<ApiResponse<QuestResponse>> add(
      @PathVariable UUID questId, @Valid @RequestBody AddChapterRequest request) {
    Quest quest = addChapterUseCase.add(questId, currentUserId(), request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.ok(QuestResponse.from(quest)));
  }

  @PutMapping("/{questId}/chapters/{chapterId}")
  public ResponseEntity<ApiResponse<QuestResponse>> update(
      @PathVariable UUID questId,
      @PathVariable UUID chapterId,
      @Valid @RequestBody UpdateChapterRequest request) {
    Quest quest = updateChapterUseCase.update(questId, chapterId, currentUserId(), request);
    return ResponseEntity.ok(ApiResponse.ok(QuestResponse.from(quest)));
  }

  @DeleteMapping("/{questId}/chapters/{chapterId}")
  public ResponseEntity<ApiResponse<QuestResponse>> remove(
      @PathVariable UUID questId, @PathVariable UUID chapterId) {
    Quest quest = removeChapterUseCase.remove(questId, chapterId, currentUserId());
    return ResponseEntity.ok(ApiResponse.ok(QuestResponse.from(quest)));
  }
}
