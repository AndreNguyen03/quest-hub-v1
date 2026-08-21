package com.questhub.modules.quest.application.usecase;

import com.questhub.modules.quest.application.helper.QuestAcess;
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
public class RemoveTaskUseCase {
  private final QuestAcess questAcess;
  private final QuestRepository questRepository;

  @Transactional(
      isolation = Isolation.DEFAULT,
      propagation = Propagation.REQUIRED,
      rollbackFor = Exception.class)
  public Quest remove(UUID questId, UUID taskId, UUID actorId) {
    Quest quest = questAcess.loadForWrite(questId, actorId);
    quest.removeTask(taskId);
    Quest saved = questRepository.save(quest);
    log.info("Task removed questId={} taskId={} actorId={}", questId, taskId, actorId);
    return saved;
  }
}

