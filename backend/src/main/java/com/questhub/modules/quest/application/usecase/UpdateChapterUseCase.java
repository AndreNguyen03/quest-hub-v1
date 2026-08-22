package com.questhub.modules.quest.application.usecase;

import com.questhub.modules.quest.application.helper.QuestCreatorGuard;
import com.questhub.modules.quest.application.command.UpdateChapterCommand;
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
public class UpdateChapterUseCase {
  private final QuestCreatorGuard questCreatorGuard;
  private final QuestRepository questRepository;

  @Transactional(
      isolation = Isolation.DEFAULT,
      propagation = Propagation.REQUIRED,
      rollbackFor = Exception.class)
  public Quest update(UUID questId, UUID chapterId, UUID actorId, UpdateChapterCommand request) {
    Quest quest = questCreatorGuard.loadForWrite(questId, actorId);
    quest.updateChapter(chapterId, request.title(), request.description(), request.position());
    Quest saved = questRepository.save(quest);
    log.info("Chapter updated questId={} chapterId={} actorId={}", questId, chapterId, actorId);
    return saved;
  }
}



