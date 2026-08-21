package com.questhub.modules.quest.application.query;

import com.questhub.modules.quest.application.dto.QuestDto;
import com.questhub.modules.quest.domain.quest.QuestRepository;
import com.questhub.shared.annotation.UseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class GetPopularQuestsQuery {

  private final QuestRepository questRepository;

  @Transactional(
      readOnly = true,
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public List<QuestDto> get(int limit) {
    return questRepository.findPopular(limit).stream()
        .map(q -> new QuestDto(q.getId(), q.getTitle(), q.getDescription(), q.getDifficulty().name(), q.getLearningPathId(), q.getForkCount(), q.getAvgRating()))
        .toList();
  }
}
