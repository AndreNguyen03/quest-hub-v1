package com.questhub.modules.quest.interfaces.controller;

import com.questhub.modules.quest.application.request.AddChapterRequest;
import com.questhub.modules.quest.application.request.AddTaskRequest;
import com.questhub.modules.quest.application.request.CompleteTaskRequest;
import com.questhub.modules.quest.application.request.ReorderChaptersRequest;
import com.questhub.modules.quest.application.request.ReorderTasksRequest;
import com.questhub.modules.quest.application.request.SubmitQuizRequest;
import com.questhub.modules.quest.application.usecase.CompleteTaskUseCase;
import com.questhub.modules.quest.application.usecase.EditPersonalQuestUseCase;
import com.questhub.modules.quest.application.usecase.ForkQuestUseCase;
import com.questhub.modules.quest.application.usecase.GetPersonalQuestUseCase;
import com.questhub.modules.quest.application.usecase.GetPersonalQuestsUseCase;
import com.questhub.modules.quest.application.usecase.GetQuizHistoryQuery;
import com.questhub.modules.quest.application.usecase.SubmitQuizUseCase;
import com.questhub.modules.quest.application.usecase.UndoTaskUseCase;
import com.questhub.modules.quest.domain.personalquest.PersonalQuest;
import com.questhub.modules.quest.domain.personalquest.QuizAttempt;
import com.questhub.modules.quest.interfaces.dto.PersonalQuestResponse;
import com.questhub.modules.quest.interfaces.dto.QuizAttemptResponse;
import com.questhub.modules.quest.interfaces.dto.SubmitQuizResponse;
import com.questhub.shared.domain.AuthenticatedUser;
import com.questhub.shared.interfaces.dto.ApiResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PersonalQuestController {

  private final ForkQuestUseCase forkQuestUseCase;
  private final GetPersonalQuestsUseCase getPersonalQuestsUseCase;
  private final GetPersonalQuestUseCase getPersonalQuestUseCase;
  private final CompleteTaskUseCase completeTaskUseCase;
  private final UndoTaskUseCase undoTaskUseCase;
  private final SubmitQuizUseCase submitQuizUseCase;
  private final GetQuizHistoryQuery getQuizHistoryQuery;
  private final EditPersonalQuestUseCase editPersonalQuestUseCase;

  private UUID currentUserId() {
    AuthenticatedUser current =
        (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return current.id();
  }

  @PostMapping("/quests/{id}/fork")
  public ResponseEntity<ApiResponse<PersonalQuestResponse>> fork(@PathVariable UUID id) {
    PersonalQuest personalQuest = forkQuestUseCase.fork(id, currentUserId());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.ok(PersonalQuestResponse.from(personalQuest)));
  }

  @GetMapping("/personal-quests")
  public ResponseEntity<ApiResponse<List<PersonalQuestResponse>>> list() {
    List<PersonalQuestResponse> responses =
        getPersonalQuestsUseCase.list(currentUserId()).stream()
            .map(PersonalQuestResponse::from)
            .toList();
    return ResponseEntity.ok(ApiResponse.ok(responses));
  }

  @GetMapping("/personal-quests/{id}")
  public ResponseEntity<ApiResponse<PersonalQuestResponse>> get(@PathVariable UUID id) {
    PersonalQuest personalQuest = getPersonalQuestUseCase.get(id, currentUserId());
    return ResponseEntity.ok(ApiResponse.ok(PersonalQuestResponse.from(personalQuest)));
  }

  @PutMapping("/personal-quests/{pqId}/tasks/{ptId}/complete")
  public ResponseEntity<ApiResponse<PersonalQuestResponse>> completeTask(
      @PathVariable UUID pqId, @PathVariable UUID ptId, @RequestBody(required = false) CompleteTaskRequest request) {
    Map<String, Object> evidence = request == null ? null : request.evidence();
    PersonalQuest personalQuest = completeTaskUseCase.complete(pqId, ptId, currentUserId(), evidence);
    return ResponseEntity.ok(ApiResponse.ok(PersonalQuestResponse.from(personalQuest)));
  }

  @DeleteMapping("/personal-quests/{pqId}/tasks/{ptId}/complete")
  public ResponseEntity<ApiResponse<PersonalQuestResponse>> undoTask(
      @PathVariable UUID pqId, @PathVariable UUID ptId) {
    PersonalQuest personalQuest = undoTaskUseCase.undo(pqId, ptId, currentUserId());
    return ResponseEntity.ok(ApiResponse.ok(PersonalQuestResponse.from(personalQuest)));
  }

  @PostMapping("/personal-quests/{pqId}/tasks/{ptId}/quiz-attempts")
  public ResponseEntity<ApiResponse<SubmitQuizResponse>> submitQuiz(
      @PathVariable UUID pqId,
      @PathVariable UUID ptId,
      @RequestBody(required = false) SubmitQuizRequest request) {
    Map<String, Object> answers = request == null ? null : request.answers();
    SubmitQuizUseCase.Result result =
        submitQuizUseCase.submit(pqId, ptId, currentUserId(), answers);
    SubmitQuizResponse response =
        new SubmitQuizResponse(
            result.attempt().getId(), result.attempt().isPassed(), result.taskCompleted());
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
  }

  @GetMapping("/personal-quests/{pqId}/tasks/{ptId}/quiz-attempts")
  public ResponseEntity<ApiResponse<List<QuizAttemptResponse>>> quizHistory(
      @PathVariable UUID pqId, @PathVariable UUID ptId) {
    List<QuizAttemptResponse> responses =
        getQuizHistoryQuery.get(pqId, ptId, currentUserId()).stream()
            .map(QuizAttemptResponse::from)
            .toList();
    return ResponseEntity.ok(ApiResponse.ok(responses));
  }

  @PostMapping("/personal-quests/{pqId}/chapters")
  public ResponseEntity<ApiResponse<PersonalQuestResponse>> addChapter(
      @PathVariable UUID pqId, @RequestBody AddChapterRequest request) {
    PersonalQuest quest =
        editPersonalQuestUseCase.addChapter(pqId, currentUserId(), request.title(), request.description());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.ok(PersonalQuestResponse.from(quest)));
  }

  @PostMapping("/personal-quests/{pqId}/chapters/{chapterId}/tasks")
  public ResponseEntity<ApiResponse<PersonalQuestResponse>> addTask(
      @PathVariable UUID pqId,
      @PathVariable UUID chapterId,
      @RequestBody AddTaskRequest request) {
    PersonalQuest quest =
        editPersonalQuestUseCase.addTask(
            pqId,
            chapterId,
            currentUserId(),
            request.type(),
            request.title(),
            request.description(),
            request.config(),
            null);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.ok(PersonalQuestResponse.from(quest)));
  }

  @DeleteMapping("/personal-quests/{pqId}/chapters/{chapterId}")
  public ResponseEntity<ApiResponse<PersonalQuestResponse>> removeChapter(
      @PathVariable UUID pqId, @PathVariable UUID chapterId) {
    PersonalQuest quest =
        editPersonalQuestUseCase.removeChapter(pqId, chapterId, currentUserId());
    return ResponseEntity.ok(ApiResponse.ok(PersonalQuestResponse.from(quest)));
  }

  @DeleteMapping("/personal-quests/{pqId}/chapters/{chapterId}/tasks/{taskId}")
  public ResponseEntity<ApiResponse<PersonalQuestResponse>> removeTask(
      @PathVariable UUID pqId,
      @PathVariable UUID chapterId,
      @PathVariable UUID taskId) {
    PersonalQuest quest =
        editPersonalQuestUseCase.removeTask(pqId, chapterId, taskId, currentUserId());
    return ResponseEntity.ok(ApiResponse.ok(PersonalQuestResponse.from(quest)));
  }

  @PatchMapping("/personal-quests/{pqId}/chapters/reorder")
  public ResponseEntity<ApiResponse<PersonalQuestResponse>> reorderChapters(
      @PathVariable UUID pqId, @RequestBody ReorderChaptersRequest request) {
    PersonalQuest quest =
        editPersonalQuestUseCase.reorderChapters(pqId, currentUserId(), request.chapterIds());
    return ResponseEntity.ok(ApiResponse.ok(PersonalQuestResponse.from(quest)));
  }

  @PatchMapping("/personal-quests/{pqId}/chapters/{chapterId}/tasks/reorder")
  public ResponseEntity<ApiResponse<PersonalQuestResponse>> reorderTasks(
      @PathVariable UUID pqId,
      @PathVariable UUID chapterId,
      @RequestBody ReorderTasksRequest request) {
    PersonalQuest quest =
        editPersonalQuestUseCase.reorderTasks(pqId, chapterId, currentUserId(), request.taskIds());
    return ResponseEntity.ok(ApiResponse.ok(PersonalQuestResponse.from(quest)));
  }
}