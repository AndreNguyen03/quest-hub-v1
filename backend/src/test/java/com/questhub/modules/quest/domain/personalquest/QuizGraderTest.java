package com.questhub.modules.quest.domain.personalquest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import com.questhub.shared.domain.DomainValidationException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class QuizGraderTest {

  private final QuizGrader grader = new QuizGrader();

  @Test
  void grade_8of10_withPassThreshold80_shouldPass() {
    Map<String, Object> config = quizConfig(80, 10);

    QuizGrader.QuizScore score = grader.grade(config, correctAnswers(10, 8));

    assertThat(score.score()).isEqualByComparingTo(new BigDecimal("8"));
    assertThat(score.maxScore()).isEqualByComparingTo(new BigDecimal("10"));
    assertThat(score.passed()).isTrue();
  }

  @Test
  void grade_7of10_withPassThreshold80_shouldFail() {
    Map<String, Object> config = quizConfig(80, 10);

    QuizGrader.QuizScore score = grader.grade(config, correctAnswers(10, 7));

    assertThat(score.score()).isEqualByComparingTo(new BigDecimal("7"));
    assertThat(score.passed()).isFalse();
  }

  @Test
  void grade_multipleChoiceList_shouldMatchWholeSet() {
    Map<String, Object> config =
        Map.of(
            "passThreshold", 100,
            "questions",
                List.of(
                    Map.of(
                        "id", "q1",
                        "options", List.of("a", "b", "c"),
                        "correctAnswer", List.of("b", "c"))));

    QuizGrader.QuizScore passed = grader.grade(config, Map.of("q1", List.of("b", "c")));
    QuizGrader.QuizScore failed = grader.grade(config, Map.of("q1", List.of("b")));

    assertThat(passed.passed()).isTrue();
    assertThat(failed.passed()).isFalse();
  }

  @Test
  void grade_noQuestions_shouldReject() {
    DomainValidationException ex =
        catchThrowableOfType(
            () -> grader.grade(Map.of("passThreshold", 80), Map.of("q1", "a")),
            DomainValidationException.class);

    assertThat(ex.getMessage()).contains("câu hỏi");
  }

  @Test
  void grade_missingPassThreshold_shouldDefaultToZeroAndPass() {
    Map<String, Object> config = quizConfig(null, 10);

    QuizGrader.QuizScore score = grader.grade(config, correctAnswers(10, 1));

    assertThat(score.passed()).isTrue();
  }

  private Map<String, Object> quizConfig(Integer passThreshold, int questionCount) {
    if (passThreshold == null) {
      return Map.of("questions", questions(questionCount));
    }
    return Map.of("passThreshold", passThreshold, "questions", questions(questionCount));
  }

  private List<Map<String, Object>> questions(int count) {
    return java.util.stream.IntStream.range(0, count)
        .mapToObj(
            i ->
                (Map<String, Object>)
                    Map.of(
                        "id", "q" + i,
                        "options", List.of("a", "b"),
                        "correctAnswer", i % 2 == 0 ? "a" : "b"))
        .toList();
  }

  private Map<String, Object> correctAnswers(int total, int correct) {
    Map<String, Object> answers = new HashMap<>();
    for (int i = 0; i < total; i++) {
      String expected = i % 2 == 0 ? "a" : "b";
      answers.put("q" + i, i < correct ? expected : (expected.equals("a") ? "b" : "a"));
    }
    return answers;
  }
}