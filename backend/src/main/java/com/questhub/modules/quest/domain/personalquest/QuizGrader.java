package com.questhub.modules.quest.domain.personalquest;

import com.questhub.shared.domain.DomainValidationException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class QuizGrader {

  private static final BigDecimal HUNDRED = new BigDecimal("100");

  public QuizScore grade(Map<String, Object> config, Map<String, Object> answers) {
    Object questionsValue = config == null ? null : config.get("questions");
    if (!(questionsValue instanceof List<?> questions) || questions.isEmpty()) {
      throw new DomainValidationException("Task không cấu hình câu hỏi quiz");
    }
    Map<String, Object> safeAnswers = answers == null ? Map.of() : answers;

    long correct =
        questions.stream()
            .filter(question -> question instanceof Map<?, ?>)
            .map(question -> (Map<?, ?>) question)
            .filter(question -> isCorrect(question, safeAnswers))
            .count();

    BigDecimal maxScore = BigDecimal.valueOf(questions.size());
    BigDecimal score = BigDecimal.valueOf(correct);
    int passThreshold =
        config.get("passThreshold") instanceof Number number ? number.intValue() : 0;
    boolean passed =
        maxScore.compareTo(BigDecimal.ZERO) > 0
            && score
                    .multiply(HUNDRED)
                    .divide(maxScore, 2, RoundingMode.HALF_UP)
                    .compareTo(BigDecimal.valueOf(passThreshold))
                >= 0;
    return new QuizScore(score, maxScore, passed);
  }

  private boolean isCorrect(Map<?, ?> question, Map<String, Object> answers) {
    Object id = question.get("id");
    if (id == null || !answers.containsKey(id)) {
      return false;
    }
    Object correctAnswer = question.get("correctAnswer");
    Object givenAnswer = answers.get(id);
    return normalize(correctAnswer).equals(normalize(givenAnswer));
  }

  private Set<String> normalize(Object value) {
    Set<String> result = new HashSet<>();
    if (value instanceof Iterable<?> iterable) {
      for (Object item : iterable) {
        result.add(String.valueOf(item));
      }
    } else if (value != null) {
      result.add(String.valueOf(value));
    }
    return result;
  }

  public record QuizScore(BigDecimal score, BigDecimal maxScore, boolean passed) {}
}