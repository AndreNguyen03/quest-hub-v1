package com.questhub.modules.quest.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.questhub.modules.quest.domain.personalquest.PersonalChapter;
import com.questhub.modules.quest.domain.personalquest.PersonalQuest;
import com.questhub.modules.quest.domain.personalquest.PersonalQuestRepository;
import com.questhub.modules.quest.domain.personalquest.PersonalQuestStatus;
import com.questhub.modules.quest.domain.personalquest.PersonalTask;
import com.questhub.modules.quest.domain.quest.CompletionRule;
import com.questhub.modules.quest.domain.task.TaskType;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.DomainValidationException;
import com.questhub.shared.domain.ErrorCodes;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AbandonQuestUseCaseTest {

  @Mock private PersonalQuestRepository personalQuestRepository;

  @InjectMocks private AbandonQuestUseCase useCase;

  @Test
  void abandon_activeQuest_shouldSetStatusAbandoned() {
    UUID userId = UUID.randomUUID();
    PersonalQuest quest = activeQuest(userId);
    when(personalQuestRepository.findByIdAndUserId(quest.getId(), userId))
        .thenReturn(Optional.of(quest));
    when(personalQuestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    useCase.abandon(quest.getId(), userId);

    assertThat(quest.getStatus()).isEqualTo(PersonalQuestStatus.ABANDONED);
    verify(personalQuestRepository).save(quest);
  }

  @Test
  void abandon_completedQuest_shouldThrowDomainValidationException() {
    UUID userId = UUID.randomUUID();
    PersonalQuest quest = completedQuest(userId);
    when(personalQuestRepository.findByIdAndUserId(quest.getId(), userId))
        .thenReturn(Optional.of(quest));

    catchThrowableOfType(
        () -> useCase.abandon(quest.getId(), userId),
        DomainValidationException.class);

    verify(personalQuestRepository, never()).save(any());
  }

  @Test
  void abandon_questNotFound_shouldThrowNotFound() {
    UUID userId = UUID.randomUUID();
    UUID questId = UUID.randomUUID();
    when(personalQuestRepository.findByIdAndUserId(questId, userId))
        .thenReturn(Optional.empty());

    BusinessException ex =
        catchThrowableOfType(
            () -> useCase.abandon(questId, userId),
            BusinessException.class);

    assertThat(ex.getCode()).isEqualTo(ErrorCodes.NOT_FOUND);
    verify(personalQuestRepository, never()).save(any());
  }

  @Test
  void abandon_alreadyAbandoned_shouldThrowDomainValidationException() {
    UUID userId = UUID.randomUUID();
    PersonalQuest quest = activeQuest(userId);
    quest.abandon();
    when(personalQuestRepository.findByIdAndUserId(quest.getId(), userId))
        .thenReturn(Optional.of(quest));

    catchThrowableOfType(
        () -> useCase.abandon(quest.getId(), userId),
        DomainValidationException.class);

    verify(personalQuestRepository, never()).save(any());
  }

  private PersonalQuest activeQuest(UUID userId) {
    PersonalQuest quest =
        PersonalQuest.create(
            userId, UUID.randomUUID(), null, "Spring Boot Basics", CompletionRule.defaultAllTasks());
    PersonalChapter chapter = PersonalChapter.create(null, "Intro", null, 0);
    chapter.addTask(PersonalTask.create(null, TaskType.LEARN, "Đọc tài liệu", null, 0, Map.of()));
    quest.addChapter(chapter);
    return quest;
  }

  private PersonalQuest completedQuest(UUID userId) {
    PersonalQuest quest =
        PersonalQuest.create(
            userId, UUID.randomUUID(), null, "Spring Boot Basics",
            CompletionRule.quizScore(new BigDecimal("80")));
    quest.markCompleted();
    return quest;
  }
}
