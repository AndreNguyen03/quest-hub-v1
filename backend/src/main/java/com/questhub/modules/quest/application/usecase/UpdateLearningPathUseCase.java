package com.questhub.modules.quest.application.usecase;

import com.questhub.modules.quest.application.request.UpdateLearningPathRequest;
import com.questhub.modules.quest.domain.learningpath.LearningPath;
import com.questhub.modules.quest.domain.learningpath.LearningPathRepository;
import com.questhub.shared.annotation.UseCase;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class UpdateLearningPathUseCase {

  private final LearningPathRepository learningPathRepository;

  public LearningPath update(UUID pathId, UUID actorId, UpdateLearningPathRequest request) {
    LearningPath path =
        learningPathRepository
            .findById(pathId)
            .orElseThrow(
                () -> BusinessException.notFound(ErrorCodes.NOT_FOUND, "Không tìm thấy learning path"));
    if (!path.getAuthorId().equals(actorId)) {
      throw BusinessException.forbidden(ErrorCodes.FORBIDDEN, "Chỉ author mới sửa được learning path");
    }
    path.update(request.title(), request.description(), request.difficulty(), request.isPublic());
    return learningPathRepository.save(path);
  }
}