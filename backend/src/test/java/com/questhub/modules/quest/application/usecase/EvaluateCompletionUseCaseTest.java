package com.questhub.modules.quest.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.questhub.modules.quest.domain.personalquest.PersonalChapter;
import com.questhub.modules.quest.domain.personalquest.PersonalQuest;
import com.questhub.modules.quest.domain.personalquest.PersonalQuestStatus;
import com.questhub.modules.quest.domain.personalquest.PersonalTask;
import com.questhub.modules.quest.domain.personalquest.QuizAttempt;
import com.questhub.modules.quest.domain.personalquest.QuizAttemptRepository;
import com.questhub.modules.quest.domain.quest.CompletionRule;
import com.questhub.modules.quest.domain.quest.TaskType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EvaluateCompletionUseCaseTest {

  @Mock private QuizAttemptRepository quizAttemptRepository;

  private EvaluateCompletionUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new EvaluateCompletionUseCase(new com.questhub.modules.quest.domain.personalquest.CompletionEvaluator(), quizAttemptRepository);
  }

  @Test
  void evaluate_allTasksCompleted_shouldMarkQuestCompleted() {
    UUID userId = UUID.randomUUID();
    PersonalQuest quest = allTasksQuest();
    quest.completeTask(quest.getChapters().get(0).getTasks().get(0).getId(), Map.of());
    quest.completeTask(quest.getChapters().get(0).getTasks().get(1).getId(), Map.of());

    boolean newlyCompleted = useCase.evaluate(quest, userId);

    assertThat(newlyCompleted).isTrue();
    assertThat(quest.getStatus()).isEqualTo(PersonalQuestStatus.COMPLETED);
    assertThat(quest.getCompletedAt()).isNotNull();
  }

  @Test
  void evaluate_partiallyCompleted_shouldStayActive() {
    UUID userId = UUID.randomUUID();
    PersonalQuest quest = allTasksQuest();
    quest.completeTask(quest.getChapters().get(0).getTasks().get(0).getId(), Map.of());

    boolean newlyCompleted = useCase.evaluate(quest, userId);

    assertThat(newlyCompleted).isFalse();
    assertThat(quest.getStatus()).isEqualTo(PersonalQuestStatus.ACTIVE);
    assertThat(quest.getCompletedAt()).isNull();
  }

  @Test
  void evaluate_undoAfterCompleted_shouldReopenToActive() {
    UUID userId = UUID.randomUUID();
    PersonalQuest quest = allTasksQuest();
    quest.completeTask(quest.getChapters().get(0).getTasks().get(0).getId(), Map.of());
    quest.completeTask(quest.getChapters().get(0).getTasks().get(1).getId(), Map.of());
    quest.markCompleted();
    assertThat(quest.getStatus()).isEqualTo(PersonalQuestStatus.COMPLETED);

    quest.undoTask(quest.getChapters().get(0).getTasks().get(0).getId());
    boolean newlyCompleted = useCase.evaluate(quest, userId);

    assertThat(newlyCompleted).isFalse();
    assertThat(quest.getStatus()).isEqualTo(PersonalQuestStatus.ACTIVE);
    assertThat(quest.getCompletedAt()).isNull();
  }

  @Test
  void evaluate_quizScore_bestAttemptMeetsThreshold() {
    UUID userId = UUID.randomUUID();
    PersonalQuest quest = quizQuest();
    UUID quizTaskId = quest.getChapters().get(0).getTasks().get(0).getId();
    when(quizAttemptRepository.findByPersonalTaskIdOrderByCreatedAtDesc(any()))
        .thenReturn(
            List.of(
                QuizAttempt.create(
                    quizTaskId, userId, new BigDecimal("7"), new BigDecimal("10"), false, Map.of())));

    boolean satisfiedAt70 = useCase.evaluate(quest, userId);
    assertThat(satisfiedAt70).isFalse();

    when(quizAttemptRepository.findByPersonalTaskIdOrderByCreatedAtDesc(any()))
        .thenReturn(
            List.of(
                QuizAttempt.create(
                    quizTaskId, userId, new BigDecimal("9"), new BigDecimal("10"), true, Map.of())));

    boolean satisfiedAt90 = useCase.evaluate(quest, userId);
    assertThat(satisfiedAt90).isTrue();
    assertThat(quest.getStatus()).isEqualTo(PersonalQuestStatus.COMPLETED);
  }

  @Test
  void evaluate_alreadyCompleted_shouldNotRepublish() {
    UUID userId = UUID.randomUUID();
    PersonalQuest quest = allTasksQuest();
    quest.completeTask(quest.getChapters().get(0).getTasks().get(0).getId(), Map.of());
    quest.completeTask(quest.getChapters().get(0).getTasks().get(1).getId(), Map.of());
    quest.markCompleted();

    boolean newlyCompleted = useCase.evaluate(quest, userId);

    assertThat(newlyCompleted).isFalse();
    assertThat(quest.getStatus()).isEqualTo(PersonalQuestStatus.COMPLETED);
  }

  private PersonalQuest allTasksQuest() {
    PersonalQuest quest =
        PersonalQuest.create(
            UUID.randomUUID(), UUID.randomUUID(), null, "Quest", CompletionRule.defaultAllTasks());
    PersonalChapter chapter = PersonalChapter.create(null, "c", null, 0);
    chapter.addTask(PersonalTask.create(null, TaskType.LEARN, "t1", null, 0, Map.of()));
    chapter.addTask(PersonalTask.create(null, TaskType.PRACTICE, "t2", null, 1, Map.of()));
    quest.addChapter(chapter);
    return quest;
  }

  private PersonalQuest quizQuest() {
    PersonalQuest quest =
        PersonalQuest.create(
            UUID.randomUUID(), UUID.randomUUID(), null, "Quest",
            CompletionRule.quizScore(new BigDecimal("80")));
    PersonalChapter chapter = PersonalChapter.create(null, "c", null, 0);
    chapter.addTask(PersonalTask.create(null, TaskType.QUIZ, "quiz", null, 0, Map.of()));
    quest.addChapter(chapter);
    return quest;
  }
}