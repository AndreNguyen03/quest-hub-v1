package com.questhub.modules.quest.application.query;

import com.questhub.modules.quest.application.dto.QuestAnalyticsDto;
import com.questhub.modules.quest.application.dto.QuestAnalyticsDto.TaskDropOffStatDto;
import com.questhub.modules.quest.domain.personalquest.PersonalQuestRepository;
import com.questhub.modules.quest.domain.quest.Quest;
import com.questhub.modules.quest.domain.quest.QuestRepository;
import com.questhub.shared.annotation.UseCase;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class GetQuestAnalyticsQuery {

  private final QuestRepository questRepository;
  private final PersonalQuestRepository personalQuestRepository;

  @Transactional(
      readOnly = true,
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public QuestAnalyticsDto get(UUID questId, UUID creatorId) {
    Quest quest =
        questRepository
            .findById(questId)
            .orElseThrow(
                () -> BusinessException.notFound(ErrorCodes.NOT_FOUND, "Không tìm thấy quest"));

    if (!quest.getCreatorId().equals(creatorId)) {
      throw BusinessException.notFound(ErrorCodes.NOT_FOUND, "Không tìm thấy quest");
    }

    double completionRate = personalQuestRepository.completionRateByQuestId(questId);

    List<TaskDropOffStatDto> taskDropOff =
        personalQuestRepository.taskDropOffByQuestId(questId).stream()
            .map(
                s -> {
                  double rate = s.totalCount() == 0
                      ? 0.0
                      : s.completedCount() * 100.0 / s.totalCount();
                  return new TaskDropOffStatDto(
                      s.sourceTaskId(), s.completedCount(), s.totalCount(), rate);
                })
            .toList();

    return new QuestAnalyticsDto(
        quest.getForkCount(),
        quest.getAvgRating(),
        quest.getRatingCount(),
        completionRate,
        taskDropOff);
  }
}
