package com.questhub.modules.quest.application.usecase;

import com.questhub.modules.quest.application.command.CreateLearningPathCommand;
import com.questhub.modules.quest.domain.learningpath.LearningPath;
import com.questhub.modules.quest.domain.learningpath.LearningPathRepository;
import com.questhub.modules.quest.domain.skilldomain.SkillDomainRepository;
import com.questhub.shared.annotation.UseCase;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class CreateLearningPathUseCase {
  private final SkillDomainRepository skillDomainRepository;
  private final LearningPathRepository learningPathRepository;

  public LearningPath create(UUID authorId, CreateLearningPathCommand request) {
    if (!skillDomainRepository.existsById(request.domainId())) {
      throw BusinessException.notFound(ErrorCodes.NOT_FOUND, "skill domain didn't exists");
    }

    LearningPath createLearningPath =
        LearningPath.create(
            authorId,
            request.domainId(),
            request.title(),
            request.description(),
            request.difficulty());

    LearningPath saved = learningPathRepository.save(createLearningPath);
    log.info("LearningPath created authorId={} pathId={} title={}",
        authorId, saved.getId(), request.title());
    return saved;
  }
}


