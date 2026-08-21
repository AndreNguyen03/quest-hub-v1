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
import com.questhub.modules.quest.domain.personalquest.QuizAttempt;
import com.questhub.modules.quest.domain.personalquest.QuizAttemptRepository;
import com.questhub.modules.quest.domain.personalquest.QuizGrader;
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
class SubmitQuizUseCaseTest {

  @Mock private PersonalQuestRepository personalQuestRepository;
  @Mock private QuizAttemptRepository quizAttemptRepository;
  @Mock private TaskCompletionRepository taskCompletionRepository;
  @Mock private QuizGrader quizGrader;
  @Mock private EvaluateCompletionUseCase evaluateCompletion;
  @Mock private TaskEventPublisher taskEventPublisher;
  @Mock private QuestEventPublisher questEventPublisher;

  @InjectMocks private SubmitQuizUseCase useCase;

  @Test
  void submit_pass_shouldCompleteTaskSaveAttemptAndPublishEvent() {
    UUID userId = UUID.randomUUID();
    PersonalQuest quest = personalQuest(userId);
    UUID taskId = quizTaskId(quest);
    when(personalQuestRepository.findByIdAndUserId(quest.getId(), userId))
        .thenReturn(Optional.of(quest));
    when(quizGrader.grade(any(), any()))
        .thenReturn(new QuizGrader.QuizScore(new BigDecimal("8"), new BigDecimal("10"), true));
    when(quizAttemptRepository.save(any(QuizAttempt.class)))
        .thenAnswer(inv -> inv.getArgument(0));
    when(personalQuestRepository.save(any(PersonalQuest.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    SubmitQuizUseCase.Result result =
        useCase.submit(quest.getId(), taskId, userId, Map.of("q1", "a"));

    assertThat(result.attempt().isPassed()).isTrue();
    assertThat(result.attempt().getScore()).isEqualByComparingTo(new BigDecimal("8"));
    assertThat(result.taskCompleted()).isTrue();
    assertThat(quizTask(quest).isCompleted()).isTrue();
    assertThat(quest.getProgress()).isEqualTo(50);

    ArgumentCaptor<QuizAttempt> attempt = ArgumentCaptor.forClass(QuizAttempt.class);
    verify(quizAttemptRepository).save(attempt.capture());
    assertThat(attempt.getValue().getPersonalTaskId()).isEqualTo(taskId);
    assertThat(attempt.getValue().getUserId()).isEqualTo(userId);

    verify(taskCompletionRepository).save(any());
    verify(taskEventPublisher).publishCompleted(eq(quest), eq(quizTask(quest)), eq(userId));
    verify(questEventPublisher, never()).publishCompleted(any(), any());
  }

  @Test
  void submit_pass_whenEvaluatorMarksQuestCompleted_shouldPublishQuestCompleted() {
    UUID userId = UUID.randomUUID();
    PersonalQuest quest = personalQuest(userId);
    UUID taskId = quizTaskId(quest);
    when(personalQuestRepository.findByIdAndUserId(quest.getId(), userId))
        .thenReturn(Optional.of(quest));
    when(quizGrader.grade(any(), any()))
        .thenReturn(new QuizGrader.QuizScore(new BigDecimal("8"), new BigDecimal("10"), true));
    when(quizAttemptRepository.save(any(QuizAttempt.class)))
        .thenAnswer(inv -> inv.getArgument(0));
    when(evaluateCompletion.evaluate(quest, userId)).thenReturn(true);
    when(personalQuestRepository.save(any(PersonalQuest.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    SubmitQuizUseCase.Result result =
        useCase.submit(quest.getId(), taskId, userId, Map.of("q1", "a"));

    assertThat(result.taskCompleted()).isTrue();
    verify(evaluateCompletion).evaluate(eq(quest), eq(userId));
    verify(questEventPublisher).publishCompleted(eq(quest), eq(userId));
  }

  @Test
  void submit_fail_shouldSaveAttemptButNotCompleteTask() {
    UUID userId = UUID.randomUUID();
    PersonalQuest quest = personalQuest(userId);
    UUID taskId = quizTaskId(quest);
    when(personalQuestRepository.findByIdAndUserId(quest.getId(), userId))
        .thenReturn(Optional.of(quest));
    when(quizGrader.grade(any(), any()))
        .thenReturn(new QuizGrader.QuizScore(new BigDecimal("6"), new BigDecimal("10"), false));
    when(quizAttemptRepository.save(any(QuizAttempt.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    SubmitQuizUseCase.Result result =
        useCase.submit(quest.getId(), taskId, userId, Map.of("q1", "b"));

    assertThat(result.attempt().isPassed()).isFalse();
    assertThat(result.taskCompleted()).isFalse();
    assertThat(quizTask(quest).isCompleted()).isFalse();
    assertThat(quest.getProgress()).isEqualTo(0);
    verify(personalQuestRepository, never()).save(any());
    verify(taskCompletionRepository, never()).save(any());
    verify(taskEventPublisher, never()).publishCompleted(any(), any(), any());
  }

  @Test
  void submit_nonQuizTask_shouldReject() {
    UUID userId = UUID.randomUUID();
    PersonalQuest quest = personalQuest(userId);
    UUID taskId = quest.getChapters().get(0).getTasks().get(1).getId();
    when(personalQuestRepository.findByIdAndUserId(quest.getId(), userId))
        .thenReturn(Optional.of(quest));

    DomainValidationException ex =
        catchThrowableOfType(
            () -> useCase.submit(quest.getId(), taskId, userId, Map.of()),
            DomainValidationException.class);

    assertThat(ex.getMessage()).contains("QUIZ");
    verify(quizAttemptRepository, never()).save(any());
  }

  @Test
  void submit_unknownTask_shouldThrowNotFound() {
    UUID userId = UUID.randomUUID();
    PersonalQuest quest = personalQuest(userId);
    when(personalQuestRepository.findByIdAndUserId(quest.getId(), userId))
        .thenReturn(Optional.of(quest));

    BusinessException ex =
        catchThrowableOfType(
            () -> useCase.submit(quest.getId(), UUID.randomUUID(), userId, Map.of()),
            BusinessException.class);

    assertThat(ex.getCode()).isEqualTo(ErrorCodes.NOT_FOUND);
    verify(quizAttemptRepository, never()).save(any());
  }

  @Test
  void submit_unknownQuest_shouldThrowNotFound() {
    UUID questId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    when(personalQuestRepository.findByIdAndUserId(questId, userId))
        .thenReturn(Optional.empty());

    BusinessException ex =
        catchThrowableOfType(
            () -> useCase.submit(questId, UUID.randomUUID(), userId, Map.of()),
            BusinessException.class);

    assertThat(ex.getCode()).isEqualTo(ErrorCodes.NOT_FOUND);
    verify(quizAttemptRepository, never()).save(any());
  }

  private PersonalQuest personalQuest(UUID userId) {
    PersonalQuest quest =
        PersonalQuest.create(
            userId,
            UUID.randomUUID(),
            null,
            "Learn TypeScript",
            CompletionRule.defaultAllTasks());
    PersonalChapter chapter = PersonalChapter.create(null, "Type Basics", null, 0);
    chapter.addTask(
        PersonalTask.create(
            null,
            TaskType.QUIZ,
            "Quiz basics",
            null,
            0,
            Map.of(
                "passThreshold", 80,
                "questions",
                    java.util.List.of(
                        Map.of("id", "q1", "options", java.util.List.of("a", "b"), "correctAnswer", "a")))));
    chapter.addTask(PersonalTask.create(null, TaskType.LEARN, "Xem video", "v", 1, Map.of()));
    quest.addChapter(chapter);
    return quest;
  }

  private UUID quizTaskId(PersonalQuest quest) {
    return quizTask(quest).getId();
  }

  private PersonalTask quizTask(PersonalQuest quest) {
    return quest.getChapters().get(0).getTasks().get(0);
  }
}


