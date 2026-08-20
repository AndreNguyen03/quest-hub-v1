package com.questhub.modules.quest.application.usecase;

import com.questhub.modules.quest.domain.learningpath.LearningPath;
import com.questhub.modules.quest.domain.learningpath.LearningPathRepository;
import com.questhub.shared.annotation.UseCase;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class GetLearningPathUseCase {

  private final LearningPathRepository learningPathRepository;

  public LearningPath get(UUID pathId, UUID viewerId) {
    LearningPath path =
        learningPathRepository
            .findById(pathId)
            .orElseThrow(
                () -> BusinessException.notFound(ErrorCodes.NOT_FOUND, "Không tìm thấy learning path"));
    if (!path.isPublic() && !path.getAuthorId().equals(viewerId)) {
      throw BusinessException.forbidden(ErrorCodes.FORBIDDEN, "Không có quyền xem learning path này");
    }
    return path;
  }
}