package com.questhub.modules.quest.interfaces.controller;

import com.questhub.modules.quest.application.request.CreateQuestRequest;
import com.questhub.modules.quest.application.request.UpdateQuestRequest;
import com.questhub.modules.quest.application.usecase.CreateQuestUseCase;
import com.questhub.modules.quest.application.usecase.GetQuestUseCase;
import com.questhub.modules.quest.application.usecase.UpdateQuestUseCase;
import com.questhub.modules.quest.domain.quest.Quest;
import com.questhub.modules.quest.interfaces.dto.QuestResponse;
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
  private final GetQuestUseCase getQuestUseCase;
  private final UpdateQuestUseCase updateQuestUseCase;

  private UUID currentUserId() {
    AuthenticatedUser current =
        (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return current.id();
  }

  @PostMapping
  public ResponseEntity<ApiResponse<QuestResponse>> create(
      @Valid @RequestBody CreateQuestRequest request) {
    Quest quest = createQuestUseCase.create(currentUserId(), request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.ok(QuestResponse.from(quest)));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<QuestResponse>> get(@PathVariable UUID id) {
    Quest quest = getQuestUseCase.get(id, currentUserId());
    return ResponseEntity.ok(ApiResponse.ok(QuestResponse.from(quest)));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<QuestResponse>> update(
      @PathVariable UUID id, @Valid @RequestBody UpdateQuestRequest request) {
    Quest quest = updateQuestUseCase.update(id, currentUserId(), request);
    return ResponseEntity.ok(ApiResponse.ok(QuestResponse.from(quest)));
  }
}