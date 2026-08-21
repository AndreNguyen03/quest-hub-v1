package com.questhub.modules.quest.application.query;

import com.questhub.modules.quest.domain.learningpath.LearningPath;
import com.questhub.modules.quest.domain.learningpath.LearningPathRepository;
import com.questhub.shared.annotation.UseCase;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class GetLearningPathQuery {

  private final LearningPathRepository learningPathRepository;

  public LearningPath get(UUID pathId, UUID viewerId) {
    LearningPath path =
        learningPathRepository
            .findById(pathId)
            .orElseThrow(
                () -> BusinessException.notFound(ErrorCodes.NOT_FOUND, "Không tìm thấy learning path"));
    if (!path.isPublic() && !path.getAuthorId().equals(viewerId)) {
      log.warn("Forbidden view learning path pathId={} viewerId={} ownerId={}",
          pathId, viewerId, path.getAuthorId());
      throw BusinessException.forbidden(ErrorCodes.FORBIDDEN, "Không có quyền xem learning path này");
    }
    log.info("LearningPath viewed pathId={} viewerId={}", pathId, viewerId);
    return path;
  }
}
