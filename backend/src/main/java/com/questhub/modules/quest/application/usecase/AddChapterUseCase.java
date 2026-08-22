package com.questhub.modules.quest.application.usecase;

import com.questhub.modules.quest.application.helper.QuestCreatorGuard;
import com.questhub.modules.quest.application.command.AddChapterCommand;
import com.questhub.modules.quest.domain.chapter.Chapter;
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
public class AddChapterUseCase {
  private final QuestCreatorGuard questCreatorGuard;
  private final QuestRepository questRepository;

  @Transactional(
      isolation = Isolation.DEFAULT,
      propagation = Propagation.REQUIRED,
      rollbackFor = Exception.class)
  public Quest add(UUID questId, UUID actorId, AddChapterCommand request) {
    Quest quest = questCreatorGuard.loadForWrite(questId, actorId);
    Chapter chapter = Chapter.create(request.title(), request.description(), 0);
    quest.addChapter(chapter);
    Quest saved = questRepository.save(quest);
    log.info("Chapter added questId={} actorId={} title={}", questId, actorId, request.title());
    return saved;
  }
}





