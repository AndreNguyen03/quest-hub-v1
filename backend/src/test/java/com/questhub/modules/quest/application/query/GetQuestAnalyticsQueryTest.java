package com.questhub.modules.quest.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.when;

import com.questhub.modules.quest.application.dto.QuestAnalyticsDto;
import com.questhub.modules.quest.domain.personalquest.PersonalQuestRepository;
import com.questhub.modules.quest.domain.personalquest.TaskDropOffStat;
import com.questhub.modules.quest.domain.quest.Difficulty;
import com.questhub.modules.quest.domain.quest.Quest;
import com.questhub.modules.quest.domain.quest.QuestRepository;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetQuestAnalyticsQueryTest {

  @Mock private QuestRepository questRepository;
  @Mock private PersonalQuestRepository personalQuestRepository;

  @InjectMocks private GetQuestAnalyticsQuery query;

  @Test
  void get_asCreator_shouldReturnFullAnalytics() {
    UUID creatorId = UUID.randomUUID();
    UUID questId = UUID.randomUUID();
    UUID taskId1 = UUID.randomUUID();
    UUID taskId2 = UUID.randomUUID();

    Quest quest = Quest.create(creatorId, null, "Spring Boot", "desc", Difficulty.BEGINNER, Map.of());
    when(questRepository.findById(questId)).thenReturn(Optional.of(quest));
    when(personalQuestRepository.completionRateByQuestId(questId)).thenReturn(66.7);
    when(personalQuestRepository.taskDropOffByQuestId(questId))
        .thenReturn(List.of(
            new TaskDropOffStat(taskId1, 2, 3),
            new TaskDropOffStat(taskId2, 3, 3)));

    QuestAnalyticsDto result = query.get(questId, creatorId);

    assertThat(result.completionRate()).isEqualTo(66.7);
    assertThat(result.taskDropOff()).hasSize(2);

    QuestAnalyticsDto.TaskDropOffStatDto first = result.taskDropOff().get(0);
    assertThat(first.sourceTaskId()).isEqualTo(taskId1);
    assertThat(first.completedCount()).isEqualTo(2);
    assertThat(first.totalCount()).isEqualTo(3);
    assertThat(first.completionRate()).isCloseTo(66.67, org.assertj.core.data.Offset.offset(0.01));

    QuestAnalyticsDto.TaskDropOffStatDto second = result.taskDropOff().get(1);
    assertThat(second.completionRate()).isCloseTo(100.0, org.assertj.core.data.Offset.offset(0.01));
  }

  @Test
  void get_questNotFound_shouldThrowNotFound() {
    UUID creatorId = UUID.randomUUID();
    UUID questId = UUID.randomUUID();
    when(questRepository.findById(questId)).thenReturn(Optional.empty());

    BusinessException ex =
        catchThrowableOfType(
            () -> query.get(questId, creatorId),
            BusinessException.class);

    assertThat(ex.getCode()).isEqualTo(ErrorCodes.NOT_FOUND);
  }

  @Test
  void get_notCreator_shouldThrowNotFound() {
    UUID creatorId = UUID.randomUUID();
    UUID otherUser = UUID.randomUUID();
    UUID questId = UUID.randomUUID();

    Quest quest = Quest.create(creatorId, null, "Spring Boot", "desc", Difficulty.BEGINNER, Map.of());
    when(questRepository.findById(questId)).thenReturn(Optional.of(quest));

    BusinessException ex =
        catchThrowableOfType(
            () -> query.get(questId, otherUser),
            BusinessException.class);

    assertThat(ex.getCode()).isEqualTo(ErrorCodes.NOT_FOUND);
  }

  @Test
  void get_noForks_shouldReturnZeroCompletionRateAndEmptyDropOff() {
    UUID creatorId = UUID.randomUUID();
    UUID questId = UUID.randomUUID();

    Quest quest = Quest.create(creatorId, null, "Spring Boot", "desc", Difficulty.BEGINNER, Map.of());
    when(questRepository.findById(questId)).thenReturn(Optional.of(quest));
    when(personalQuestRepository.completionRateByQuestId(questId)).thenReturn(0.0);
    when(personalQuestRepository.taskDropOffByQuestId(questId)).thenReturn(List.of());

    QuestAnalyticsDto result = query.get(questId, creatorId);

    assertThat(result.forkCount()).isZero();
    assertThat(result.completionRate()).isZero();
    assertThat(result.taskDropOff()).isEmpty();
  }
}
