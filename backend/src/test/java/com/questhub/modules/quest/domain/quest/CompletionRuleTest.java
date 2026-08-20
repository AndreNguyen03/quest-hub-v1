package com.questhub.modules.quest.domain.quest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import com.questhub.shared.domain.DomainValidationException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class CompletionRuleTest {

  @Test
  void defaultAllTasks_shouldHaveAllTasksType() {
    CompletionRule rule = CompletionRule.defaultAllTasks();

    assertThat(rule.type()).isEqualTo(CompletionRule.Type.ALL_TASKS);
    assertThat(rule.minScore()).isNull();
  }

  @Test
  void quizScore_validThreshold_shouldKeepThreshold() {
    CompletionRule rule = CompletionRule.quizScore(new BigDecimal("80"));

    assertThat(rule.type()).isEqualTo(CompletionRule.Type.QUIZ_SCORE);
    assertThat(rule.minScore()).isEqualByComparingTo("80");
  }

  @Test
  void quizScore_missingThreshold_shouldThrow() {
    DomainValidationException ex =
        catchThrowableOfType(
            () -> new CompletionRule(CompletionRule.Type.QUIZ_SCORE, null, null, null),
            DomainValidationException.class);

    assertThat(ex.getMessage()).contains("QUIZ_SCORE requires minScore");
  }

  @Test
  void quizScore_thresholdOutOfRange_shouldThrow() {
    DomainValidationException ex =
        catchThrowableOfType(
            () -> CompletionRule.quizScore(new BigDecimal("101")),
            DomainValidationException.class);

    assertThat(ex.getMessage()).contains("QUIZ_SCORE requires minScore");
  }

  @Test
  void submission_emptyRequiredTaskTypes_shouldThrow() {
    DomainValidationException ex =
        catchThrowableOfType(
            () -> CompletionRule.submission(List.of()), DomainValidationException.class);

    assertThat(ex.getMessage()).contains("SUBMISSION requires requiredTaskTypes");
  }

  @Test
  void submission_validTypes_shouldKeep() {
    CompletionRule rule = CompletionRule.submission(List.of(TaskType.SUBMISSION));

    assertThat(rule.type()).isEqualTo(CompletionRule.Type.SUBMISSION);
    assertThat(rule.requiredTaskTypes()).containsExactly(TaskType.SUBMISSION);
  }

  @Test
  void allOf_lessThanTwoRules_shouldThrow() {
    DomainValidationException ex =
        catchThrowableOfType(
            () -> CompletionRule.allOf(List.of(CompletionRule.defaultAllTasks())),
            DomainValidationException.class);

    assertThat(ex.getMessage()).contains("ALL_OF requires at least 2 rules");
  }

  @Test
  void allOf_withTwoRules_shouldKeepNestedRules() {
    CompletionRule rule =
        CompletionRule.allOf(
            List.of(
                CompletionRule.defaultAllTasks(),
                CompletionRule.quizScore(new BigDecimal("70"))));

    assertThat(rule.type()).isEqualTo(CompletionRule.Type.ALL_OF);
    assertThat(rule.rules()).hasSize(2);
  }
}