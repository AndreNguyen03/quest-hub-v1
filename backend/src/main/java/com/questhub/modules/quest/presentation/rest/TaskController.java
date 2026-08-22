package com.questhub.modules.quest.presentation.rest;

import com.questhub.modules.quest.application.command.AddResourceCommand;
import com.questhub.modules.quest.application.command.AddTaskCommand;
import com.questhub.modules.quest.application.command.UpdateTaskCommand;
import com.questhub.modules.quest.application.usecase.AddResourceUseCase;
import com.questhub.modules.quest.application.usecase.AddTaskUseCase;
import com.questhub.modules.quest.application.usecase.RemoveResourceUseCase;
import com.questhub.modules.quest.application.usecase.RemoveTaskUseCase;
import com.questhub.modules.quest.application.usecase.UpdateTaskUseCase;
import com.questhub.modules.quest.domain.quest.Quest;
import com.questhub.modules.quest.application.dto.QuestResponse;
import com.questhub.shared.domain.AuthenticatedUser;
import com.questhub.shared.presentation.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/quests")
public class TaskController {
  private final AddTaskUseCase addTaskUseCase;
  private final UpdateTaskUseCase updateTaskUseCase;
  private final RemoveTaskUseCase removeTaskUseCase;
  private final AddResourceUseCase addResourceUseCase;
  private final RemoveResourceUseCase removeResourceUseCase;

  private UUID currentUserId() {
    AuthenticatedUser current =
        (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return current.id();
  }

  @PostMapping("/{questId}/chapters/{chapterId}/tasks")
  public ResponseEntity<ApiResponse<QuestResponse>> add(
      @PathVariable UUID questId,
      @PathVariable UUID chapterId,
      @Valid @RequestBody AddTaskCommand request) {
    Quest quest = addTaskUseCase.add(questId, chapterId, currentUserId(), request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.ok(QuestResponse.from(quest)));
  }

  @PutMapping("/{questId}/tasks/{taskId}")
  public ResponseEntity<ApiResponse<QuestResponse>> update(
      @PathVariable UUID questId,
      @PathVariable UUID taskId,
      @Valid @RequestBody UpdateTaskCommand request) {
    Quest quest = updateTaskUseCase.update(questId, taskId, currentUserId(), request);
    return ResponseEntity.ok(ApiResponse.ok(QuestResponse.from(quest)));
  }

  @DeleteMapping("/{questId}/tasks/{taskId}")
  public ResponseEntity<ApiResponse<QuestResponse>> remove(
      @PathVariable UUID questId, @PathVariable UUID taskId) {
    Quest quest = removeTaskUseCase.remove(questId, taskId, currentUserId());
    return ResponseEntity.ok(ApiResponse.ok(QuestResponse.from(quest)));
  }

  @PostMapping("/tasks/{taskId}/resources")
  public ResponseEntity<ApiResponse<QuestResponse>> addResource(
      @PathVariable UUID taskId, @Valid @RequestBody AddResourceCommand request) {
    Quest quest = addResourceUseCase.add(taskId, currentUserId(), request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.ok(QuestResponse.from(quest)));
  }

  @DeleteMapping("/resources/{resourceId}")
  public ResponseEntity<ApiResponse<QuestResponse>> removeResource(
      @PathVariable UUID resourceId) {
    Quest quest = removeResourceUseCase.remove(resourceId, currentUserId());
    return ResponseEntity.ok(ApiResponse.ok(QuestResponse.from(quest)));
  }
}




