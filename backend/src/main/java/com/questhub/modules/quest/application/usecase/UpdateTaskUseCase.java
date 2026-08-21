package com.questhub.modules.quest.application.usecase;

import com.questhub.modules.quest.application.helper.QuestAcess;
import com.questhub.modules.quest.application.command.UpdateTaskCommand;
import com.questhub.modules.quest.domain.quest.Quest;
import com.questhub.modules.quest.domain.quest.QuestRepository;
import com.questhub.shared.annotation.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class UpdateTaskUseCase {
  private final QuestAcess questAcess;
  private final QuestRepository questRepository;

  @Transactional(
      isolation = Isolation.DEFAULT,
      propagation = Propagation.REQUIRED,
      rollbackFor = Exception.class)
  public Quest update(UUID questId, UUID taskId, UUID actorId, UpdateTaskCommand request) {
    Quest quest = questAcess.loadForWrite(questId, actorId);
    quest.updateTask(taskId, request.title(), request.description(), request.config(), request.order());
    Quest saved = questRepository.save(quest);
    log.info("Task updated questId={} taskId={} actorId={}", questId, taskId, actorId);
    return saved;
  }
}



