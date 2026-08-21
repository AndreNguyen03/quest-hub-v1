package com.questhub.modules.quest.application.usecase;

import com.questhub.modules.quest.application.command.CreateQuestCommand;
import com.questhub.modules.quest.domain.learningpath.LearningPathRepository;
import com.questhub.modules.quest.domain.chapter.Chapter;
import com.questhub.modules.quest.domain.quest.Quest;
import com.questhub.modules.quest.domain.quest.QuestRepository;
import com.questhub.modules.quest.domain.task.Task;
import com.questhub.shared.annotation.UseCase;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class CreateQuestUseCase {

  private final QuestRepository questRepository;
  private final LearningPathRepository learningPathRepository;

  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public Quest create(UUID creatorId, CreateQuestCommand request) {
    if (request.learningPathId() != null
        && !learningPathRepository.existsById(request.learningPathId())) {
      throw BusinessException.notFound(ErrorCodes.NOT_FOUND, "Không tìm thấy learning path");
    }

    Quest quest =
        Quest.create(
            creatorId,
            request.learningPathId(),
            request.title(),
            request.description(),
            request.difficulty(),
            request.reward());

    for (CreateQuestCommand.ChapterRequest chapterRequest : request.chapters()) {
      Chapter chapter = Chapter.create(chapterRequest.title(), chapterRequest.description(), 0);
      quest.addChapter(chapter);
      for (CreateQuestCommand.TaskRequest taskRequest : chapterRequest.tasks()) {
        Task task =
            Task.create(
                taskRequest.type(),
                taskRequest.title(),
                taskRequest.description(),
                0,
                taskRequest.config());
        quest.addTask(chapter.getId(), task);
      }
    }

    if (request.completionRule() != null) {
      quest.applyCompletionRule(request.completionRule());
    }

    Quest saved = questRepository.save(quest);
    log.info("Quest created creatorId={} questId={} title={} chapterCount={}",
        creatorId, saved.getId(), request.title(), saved.getChapters().size());
    return saved;
  }
}





