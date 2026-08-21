package com.questhub.modules.quest.domain.personalquest;

import static org.assertj.core.api.Assertions.assertThat;

import com.questhub.modules.quest.domain.quest.CompletionRule;
import com.questhub.modules.quest.domain.task.TaskType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CompletionEvaluatorTest {

  private final CompletionEvaluator evaluator = new CompletionEvaluator();

  @Test
  void allTasks_completeAll_shouldSatisfy() {
    PersonalQuest quest = mixedQuest();
    assertThat(evaluator.isSatisfied(CompletionRule.defaultAllTasks(), quest, Map.of()))
        .isFalse();

    quest.completeTaskByQuiz(quest.getChapters().get(0).getTasks().get(0).getId());
    quest.completeTask(quest.getChapters().get(0).getTasks().get(1).getId(), Map.of("url", "https://example.com"));

    assertThat(evaluator.isSatisfied(CompletionRule.defaultAllTasks(), quest, Map.of()))
        .isTrue();
  }

  @Test
  void quizScore_meetsThreshold_shouldSatisfy() {
    PersonalQuest quest = mixedQuest();
    UUID quizTaskId = quizTaskId(quest);
    CompletionRule rule = CompletionRule.quizScore(new BigDecimal("80"));

    assertThat(
            evaluator.isSatisfied(rule, quest, Map.of(quizTaskId, new BigDecimal("70.00"))))
        .isFalse();
    assertThat(
            evaluator.isSatisfied(rule, quest, Map.of(quizTaskId, new BigDecimal("80.00"))))
        .isTrue();
    assertThat(evaluator.isSatisfied(rule, quest, Map.of())).isFalse();
  }

  @Test
  void quizScore_noQuizTasks_shouldNotSatisfy() {
    PersonalQuest quest = PersonalQuest.create(
        UUID.randomUUID(), UUID.randomUUID(), null, "q", CompletionRule.defaultAllTasks());
    PersonalChapter chapter = PersonalChapter.create(null, "c", null, 0);
    chapter.addTask(PersonalTask.create(null, TaskType.LEARN, "t", null, 0, Map.of()));
    quest.addChapter(chapter);

    assertThat(
            evaluator.isSatisfied(
                CompletionRule.quizScore(new BigDecimal("80")), quest, Map.of()))
        .isFalse();
  }

  @Test
  void submission_requiresAllTasksOfRequiredTypes() {
    PersonalQuest quest = mixedQuest();
    CompletionRule rule = CompletionRule.submission(List.of(TaskType.SUBMISSION));
    UUID submissionTaskId = quest.getChapters().get(0).getTasks().get(1).getId();

    assertThat(evaluator.isSatisfied(rule, quest, Map.of())).isFalse();

    quest.completeTaskByQuiz(quest.getChapters().get(0).getTasks().get(0).getId());
    assertThat(evaluator.isSatisfied(rule, quest, Map.of())).isFalse();

    quest.completeTask(submissionTaskId, Map.of("url", "https://example.com"));
    assertThat(evaluator.isSatisfied(rule, quest, Map.of())).isTrue();
  }

  @Test
  void allOf_requiresAllSubRules() {
    PersonalQuest quest = mixedQuest();
    UUID quizTaskId = quizTaskId(quest);
    CompletionRule rule =
        CompletionRule.allOf(
            List.of(
                CompletionRule.defaultAllTasks(),
                CompletionRule.quizScore(new BigDecimal("80"))));

    quest.completeTaskByQuiz(quest.getChapters().get(0).getTasks().get(0).getId());
    quest.completeTask(quest.getChapters().get(0).getTasks().get(1).getId(), Map.of("url", "https://example.com"));

    assertThat(
            evaluator.isSatisfied(
                rule, quest, Map.of(quizTaskId, new BigDecimal("70.00"))))
        .isFalse();
    assertThat(
            evaluator.isSatisfied(
                rule, quest, Map.of(quizTaskId, new BigDecimal("90.00"))))
        .isTrue();
  }

  @Test
  void anyOf_requiresOneSubRule() {
    PersonalQuest quest = mixedQuest();
    UUID quizTaskId = quizTaskId(quest);
    CompletionRule rule =
        CompletionRule.anyOf(
            List.of(
                CompletionRule.defaultAllTasks(),
                CompletionRule.quizScore(new BigDecimal("80"))));

    assertThat(
            evaluator.isSatisfied(
                rule, quest, Map.of(quizTaskId, new BigDecimal("85.00"))))
        .isTrue();
    assertThat(
            evaluator.isSatisfied(
                rule, quest, Map.of(quizTaskId, new BigDecimal("50.00"))))
        .isFalse();
  }

  private PersonalQuest mixedQuest() {
    PersonalQuest quest =
        PersonalQuest.create(
            UUID.randomUUID(),
            UUID.randomUUID(),
            null,
            "Quest",
            CompletionRule.defaultAllTasks());
    PersonalChapter chapter = PersonalChapter.create(null, "c", null, 0);
    chapter.addTask(PersonalTask.create(null, TaskType.QUIZ, "quiz", null, 0, Map.of()));
    chapter.addTask(PersonalTask.create(null, TaskType.SUBMISSION, "sub", null, 1, Map.of()));
    quest.addChapter(chapter);
    return quest;
  }

  private UUID quizTaskId(PersonalQuest quest) {
    return quest.getChapters().get(0).getTasks().get(0).getId();
  }
}


