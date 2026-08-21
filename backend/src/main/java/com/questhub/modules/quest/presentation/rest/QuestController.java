package com.questhub.modules.quest.presentation.rest;

import com.questhub.modules.quest.application.command.CreateQuestCommand;
import com.questhub.modules.quest.application.command.SetCompletionRuleCommand;
import com.questhub.modules.quest.application.command.UpdateQuestCommand;
import com.questhub.modules.quest.application.usecase.CreateQuestUseCase;
import com.questhub.modules.quest.application.query.GetQuestQuery;
import com.questhub.modules.quest.application.usecase.PublishQuestUseCase;
import com.questhub.modules.quest.application.usecase.SetCompletionRuleUseCase;
import com.questhub.modules.quest.application.usecase.UnpublishQuestUseCase;
import com.questhub.modules.quest.application.usecase.UpdateQuestUseCase;
import com.questhub.modules.quest.domain.quest.Quest;
import com.questhub.modules.quest.application.dto.QuestResponse;
import com.questhub.shared.domain.AuthenticatedUser;
import com.questhub.shared.interfaces.dto.ApiResponse;
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
@RequestMapping("/api/v1/quests")
@RequiredArgsConstructor
public class QuestController {

  private final CreateQuestUseCase createQuestUseCase;
  private final GetQuestQuery GetQuestQuery;
  private final UpdateQuestUseCase updateQuestUseCase;
  private final PublishQuestUseCase publishQuestUseCase;
  private final UnpublishQuestUseCase unpublishQuestUseCase;
  private final SetCompletionRuleUseCase setCompletionRuleUseCase;

  private UUID currentUserId() {
    AuthenticatedUser current =
        (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return current.id();
  }

  @PostMapping
  public ResponseEntity<ApiResponse<QuestResponse>> create(
      @Valid @RequestBody CreateQuestCommand request) {
    Quest quest = createQuestUseCase.create(currentUserId(), request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.ok(QuestResponse.from(quest)));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<QuestResponse>> get(@PathVariable UUID id) {
    Quest quest = GetQuestQuery.get(id, currentUserId());
    return ResponseEntity.ok(ApiResponse.ok(QuestResponse.from(quest)));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<QuestResponse>> update(
      @PathVariable UUID id, @Valid @RequestBody UpdateQuestCommand request) {
    Quest quest = updateQuestUseCase.update(id, currentUserId(), request);
    return ResponseEntity.ok(ApiResponse.ok(QuestResponse.from(quest)));
  }

  @PostMapping("/{id}/publish")
  public ResponseEntity<ApiResponse<QuestResponse>> publish(@PathVariable UUID id) {
    Quest quest = publishQuestUseCase.publish(id, currentUserId());
    return ResponseEntity.ok(ApiResponse.ok(QuestResponse.from(quest)));
  }

  @PostMapping("/{id}/unpublish")
  public ResponseEntity<ApiResponse<QuestResponse>> unpublish(@PathVariable UUID id) {
    Quest quest = unpublishQuestUseCase.unpublish(id, currentUserId());
    return ResponseEntity.ok(ApiResponse.ok(QuestResponse.from(quest)));
  }

  @PutMapping("/{id}/completion-rule")
  public ResponseEntity<ApiResponse<QuestResponse>> setCompletionRule(
      @PathVariable UUID id, @Valid @RequestBody SetCompletionRuleCommand request) {
    Quest quest = setCompletionRuleUseCase.setRule(id, currentUserId(), request.completionRule());
    return ResponseEntity.ok(ApiResponse.ok(QuestResponse.from(quest)));
  }
}





