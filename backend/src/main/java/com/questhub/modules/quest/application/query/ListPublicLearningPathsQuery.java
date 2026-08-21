package com.questhub.modules.quest.application.query;

import com.questhub.modules.quest.application.dto.LearningPathDto;
import com.questhub.modules.quest.domain.learningpath.LearningPathRepository;
import com.questhub.shared.annotation.UseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class ListPublicLearningPathsQuery {

  private final LearningPathRepository learningPathRepository;

  @Transactional(
      readOnly = true,
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public List<LearningPathDto> list() {
    return learningPathRepository.findByIsPublicTrueOrderByCreatedAtDesc().stream()
        .map(lp -> new LearningPathDto(lp.getId(), lp.getTitle(), lp.getDescription(), lp.getDifficulty().name(), lp.getDomainId()))
        .toList();
  }
}
