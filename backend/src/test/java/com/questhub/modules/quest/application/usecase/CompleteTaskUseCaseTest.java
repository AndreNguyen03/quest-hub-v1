package com.questhub.modules.quest.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.questhub.modules.quest.application.event.QuestEventPublisher;
import com.questhub.modules.quest.application.event.TaskEventPublisher;
import com.questhub.modules.quest.domain.personalquest.PersonalChapter;
import com.questhub.modules.quest.domain.personalquest.PersonalQuest;
import com.questhub.modules.quest.domain.personalquest.PersonalQuestRepository;
import com.questhub.modules.quest.domain.personalquest.PersonalTask;
import com.questhub.modules.quest.domain.personalquest.TaskCompletion;
import com.questhub.modules.quest.domain.personalquest.TaskCompletionRepository;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompleteTaskUseCaseTest {

  @Mock private PersonalQuestRepository personalQuestRepository;
  @Mock private TaskCompletionRepository taskCompletionRepository;
  @Mock private EvaluateCompletionUseCase evaluateCompletion;
  @Mock private TaskEventPublisher taskEventPublisher;
  @Mock private QuestEventPublisher questEventPublisher;

  @InjectMocks private CompleteTaskUseCase useCase;

  @Test
  void complete_learnTask_shouldSetCompletedAndCreateCompletionAndPublishEvent() {
    UUID userId = UUID.randomUUID();
    PersonalQuest quest = personalQuest(userId);
    UUID taskId = quest.getChapters().get(0).getTasks().get(0).getId();
    when(personalQuestRepository.findByIdAndUserId(quest.getId(), userId))
        .thenReturn(Optional.of(quest));
    when(personalQuestRepository.save(any(PersonalQuest.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    PersonalQuest result = useCase.complete(quest.getId(), taskId, userId, Map.of());

    PersonalTask task = result.getChapters().get(0).getTasks().get(0);
    assertThat(task.isCompleted()).isTrue();
    assertThat(task.getCompletedAt()).isNotNull();
    assertThat(result.getProgress()).isEqualTo(50);

    ArgumentCaptor<TaskCompletion> completion = ArgumentCaptor.forClass(TaskCompletion.class);
    verify(taskCompletionRepository).save(completion.capture());
    assertThat(completion.getValue().getPersonalTaskId()).isEqualTo(taskId);
    assertThat(completion.getValue().getUserId()).isEqualTo(userId);

    verify(taskEventPublisher).publishCompleted(eq(quest), eq(task), eq(userId));
    verify(questEventPublisher, never()).publishCompleted(any(), any());
  }

  @Test
  void complete_whenEvaluatorMarksQuestCompleted_shouldPublishQuestCompleted() {
    UUID userId = UUID.randomUUID();
    PersonalQuest quest = personalQuest(userId);
    UUID taskId = quest.getChapters().get(0).getTasks().get(0).getId();
    when(personalQuestRepository.findByIdAndUserId(quest.getId(), userId))
        .thenReturn(Optional.of(quest));
    when(evaluateCompletion.evaluate(quest, userId)).thenReturn(true);
    when(personalQuestRepository.save(any(PersonalQuest.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    PersonalQuest result = useCase.complete(quest.getId(), taskId, userId, Map.of());

    assertThat(result.isCompleted()).isFalse();
    verify(evaluateCompletion).evaluate(eq(quest), eq(userId));
    verify(taskEventPublisher).publishCompleted(eq(quest), eq(quest.getChapters().get(0).getTasks().get(0)), eq(userId));
    verify(questEventPublisher).publishCompleted(eq(quest), eq(userId));
  }

  @Test
  void complete_submissionWithoutEvidence_shouldReject() {
    UUID userId = UUID.randomUUID();
    PersonalQuest quest = personalQuest(userId);
    UUID taskId =
        quest.getChapters().get(0).getTasks().stream()
            .filter(t -> t.getType() == TaskType.SUBMISSION)
            .findFirst()
            .orElseThrow()
            .getId();
    when(personalQuestRepository.findByIdAndUserId(quest.getId(), userId))
        .thenReturn(Optional.of(quest));

    DomainValidationException ex =
        catchThrowableOfType(
            () -> useCase.complete(quest.getId(), taskId, userId, Map.of()),
            DomainValidationException.class);

    assertThat(ex.getMessage()).contains("SUBMISSION");
    verify(taskCompletionRepository, never()).save(any());
    verify(taskEventPublisher, never()).publishCompleted(any(), any(), any());
  }

  @Test
  void complete_submissionWithUrl_shouldSucceed() {
    UUID userId = UUID.randomUUID();
    PersonalQuest quest = personalQuest(userId);
    UUID taskId =
        quest.getChapters().get(0).getTasks().stream()
            .filter(t -> t.getType() == TaskType.SUBMISSION)
            .findFirst()
            .orElseThrow()
            .getId();
    when(personalQuestRepository.findByIdAndUserId(quest.getId(), userId))
        .thenReturn(Optional.of(quest));
    when(personalQuestRepository.save(any(PersonalQuest.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    PersonalQuest result =
        useCase.complete(quest.getId(), taskId, userId, Map.of("url", "https://example.com"));

    assertThat(result.getChapters().get(0).getTasks().get(1).isCompleted()).isTrue();
  }

  @Test
  void complete_reflectionShorterThanMinLength_shouldReject() {
    UUID userId = UUID.randomUUID();
    PersonalQuest quest = personalQuest(userId);
    PersonalChapter chapter = PersonalChapter.create(null, "Reflection chapter", null, 1);
    chapter.addTask(
        PersonalTask.create(
            null, TaskType.REFLECTION, "Reflect", null, 0, Map.of("minLength", 10)));
    quest.addChapter(chapter);
    UUID taskId = chapter.getTasks().get(0).getId();
    when(personalQuestRepository.findByIdAndUserId(quest.getId(), userId))
        .thenReturn(Optional.of(quest));

    DomainValidationException ex =
        catchThrowableOfType(
            () -> useCase.complete(quest.getId(), taskId, userId, Map.of("text", "ngắn")),
            DomainValidationException.class);

    assertThat(ex.getMessage()).contains("REFLECTION");
    verify(taskCompletionRepository, never()).save(any());
  }

  @Test
  void complete_reflectionLongEnough_shouldSucceed() {
    UUID userId = UUID.randomUUID();
    PersonalQuest quest = personalQuest(userId);
    PersonalChapter chapter = PersonalChapter.create(null, "Reflection chapter", null, 1);
    chapter.addTask(
        PersonalTask.create(
            null, TaskType.REFLECTION, "Reflect", null, 0, Map.of("minLength", 3)));
    quest.addChapter(chapter);
    UUID taskId = chapter.getTasks().get(0).getId();
    when(personalQuestRepository.findByIdAndUserId(quest.getId(), userId))
        .thenReturn(Optional.of(quest));
    when(personalQuestRepository.save(any(PersonalQuest.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    PersonalQuest result =
        useCase.complete(quest.getId(), taskId, userId, Map.of("text", "đủ dài"));

    assertThat(result.getChapters().get(1).getTasks().get(0).isCompleted()).isTrue();
  }

  @Test
  void complete_quizTask_shouldReject() {
    UUID userId = UUID.randomUUID();
    PersonalQuest quest = personalQuest(userId);
    PersonalChapter chapter = PersonalChapter.create(null, "Quiz chapter", null, 2);
    chapter.addTask(PersonalTask.create(null, TaskType.QUIZ, "Quiz", null, 0, Map.of()));
    quest.addChapter(chapter);
    UUID taskId = chapter.getTasks().get(0).getId();
    when(personalQuestRepository.findByIdAndUserId(quest.getId(), userId))
        .thenReturn(Optional.of(quest));

    DomainValidationException ex =
        catchThrowableOfType(
            () -> useCase.complete(quest.getId(), taskId, userId, Map.of()),
            DomainValidationException.class);

    assertThat(ex.getMessage()).contains("QUIZ");
    verify(taskCompletionRepository, never()).save(any());
  }

  @Test
  void complete_alreadyCompleted_shouldThrowConflict() {
    UUID userId = UUID.randomUUID();
    PersonalQuest quest = personalQuest(userId);
    UUID taskId = quest.getChapters().get(0).getTasks().get(0).getId();
    quest.completeTask(taskId, Map.of());
    when(personalQuestRepository.findByIdAndUserId(quest.getId(), userId))
        .thenReturn(Optional.of(quest));

    BusinessException ex =
        catchThrowableOfType(
            () -> useCase.complete(quest.getId(), taskId, userId, Map.of()),
            BusinessException.class);

    assertThat(ex.getCode()).isEqualTo(ErrorCodes.CONFLICT);
    verify(personalQuestRepository, never()).save(any());
    verify(taskCompletionRepository, never()).save(any());
    verify(taskEventPublisher, never()).publishCompleted(any(), any(), any());
  }

  @Test
  void complete_unknownTask_shouldThrowNotFound() {
    UUID userId = UUID.randomUUID();
    PersonalQuest quest = personalQuest(userId);
    when(personalQuestRepository.findByIdAndUserId(quest.getId(), userId))
        .thenReturn(Optional.of(quest));

    BusinessException ex =
        catchThrowableOfType(
            () -> useCase.complete(quest.getId(), UUID.randomUUID(), userId, Map.of()),
            BusinessException.class);

    assertThat(ex.getCode()).isEqualTo(ErrorCodes.NOT_FOUND);
    verify(personalQuestRepository, never()).save(any());
  }

  @Test
  void complete_unknownQuest_shouldThrowNotFound() {
    UUID questId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    when(personalQuestRepository.findByIdAndUserId(questId, userId))
        .thenReturn(Optional.empty());

    BusinessException ex =
        catchThrowableOfType(
            () -> useCase.complete(questId, UUID.randomUUID(), userId, Map.of()),
            BusinessException.class);

    assertThat(ex.getCode()).isEqualTo(ErrorCodes.NOT_FOUND);
    verify(taskCompletionRepository, never()).save(any());
  }

  private PersonalQuest personalQuest(UUID userId) {
    PersonalQuest quest =
        PersonalQuest.create(
            userId,
            UUID.randomUUID(),
            null,
            "Spring Security Fundamentals",
            CompletionRule.quizScore(new BigDecimal("70")));
    PersonalChapter chapter = PersonalChapter.create(null, "Authentication", "Phần 1", 0);
    chapter.addTask(PersonalTask.create(null, TaskType.LEARN, "Xem video", "v", 0, Map.of()));
    chapter.addTask(
        PersonalTask.create(null, TaskType.SUBMISSION, "Nộp bài", "n", 1, Map.of()));
    quest.addChapter(chapter);
    return quest;
  }
}


