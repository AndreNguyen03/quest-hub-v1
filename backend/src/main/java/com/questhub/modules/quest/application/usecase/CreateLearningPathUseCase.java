package com.questhub.modules.quest.application.usecase;

import com.questhub.modules.quest.application.request.CreateLearningPathRequest;
import com.questhub.modules.quest.domain.learningpath.LearningPath;
import com.questhub.modules.quest.domain.learningpath.LearningPathRepository;
import com.questhub.modules.quest.domain.skilldomain.SkillDomainRepository;
import com.questhub.shared.annotation.UseCase;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@UseCase
@RequiredArgsConstructor
public class CreateLearningPathUseCase {
  private final SkillDomainRepository skillDomainRepository;
  private final LearningPathRepository learningPathRepository;

  public LearningPath create(UUID authorId, CreateLearningPathRequest request) {
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

    return learningPathRepository.save(createLearningPath);
  }
}
