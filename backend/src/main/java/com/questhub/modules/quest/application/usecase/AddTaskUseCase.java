package com.questhub.modules.quest.application.usecase;

import com.questhub.modules.quest.application.helper.QuestAcess;
import com.questhub.modules.quest.application.command.AddTaskCommand;
import com.questhub.modules.quest.domain.quest.Quest;
import com.questhub.modules.quest.domain.quest.QuestRepository;
import com.questhub.modules.quest.domain.task.Task;
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
public class AddTaskUseCase {
  private final QuestAcess questAcess;
  private final QuestRepository questRepository;

  @Transactional(
      isolation = Isolation.DEFAULT,
      propagation = Propagation.REQUIRED,
      rollbackFor = Exception.class)
  public Quest add(UUID questId, UUID chapterId, UUID actorId, AddTaskCommand request) {
    Quest quest = questAcess.loadForWrite(questId, actorId);
    Task task = Task.create(request.type(), request.title(), request.description(), 0, request.config());
    quest.addTask(chapterId, task);
    Quest saved = questRepository.save(quest);
    log.info(
        "Task added questId={} chapterId={} actorId={} type={}",
        questId, chapterId, actorId, request.type());
    return saved;
  }
}




