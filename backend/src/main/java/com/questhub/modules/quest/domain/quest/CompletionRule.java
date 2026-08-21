package com.questhub.modules.quest.domain.quest;

import com.questhub.modules.quest.domain.task.TaskType;
import com.questhub.shared.domain.DomainValidationException;
import java.math.BigDecimal;
import java.util.List;

public record CompletionRule(
    Type type, BigDecimal minScore, List<TaskType> requiredTaskTypes, List<CompletionRule> rules) {

  public enum Type {
    ALL_TASKS,
    QUIZ_SCORE,
    SUBMISSION,
    ALL_OF,
    ANY_OF
  }

  public CompletionRule {
    if (type == null) {
      throw new DomainValidationException("completionRule.type is required");
    }
    switch (type) {
      case Type t
          when t == Type.QUIZ_SCORE
              && (minScore == null
                  || minScore.compareTo(BigDecimal.ZERO) < 0
                  || minScore.compareTo(new BigDecimal("100")) > 0) ->
          throw new DomainValidationException("QUIZ_SCORE requires minScore between 0 and 100");
      case Type t
          when t == Type.SUBMISSION && (requiredTaskTypes == null || requiredTaskTypes.isEmpty()) ->
          throw new DomainValidationException("SUBMISSION requires requiredTaskTypes");
      case Type t
          when (t == Type.ALL_OF || t == Type.ANY_OF) && (rules == null || rules.size() < 2) ->
          throw new DomainValidationException(t + " requires at least 2 rules");
      default -> {}
    }
  }

  public static CompletionRule defaultAllTasks() {
    return new CompletionRule(Type.ALL_TASKS, null, null, null);
  }

  public static CompletionRule quizScore(BigDecimal minScore) {
    return new CompletionRule(Type.QUIZ_SCORE, minScore, null, null);
  }

  public static CompletionRule submission(List<TaskType> requiredTaskTypes) {
    return new CompletionRule(Type.SUBMISSION, null, requiredTaskTypes, null);
  }

  public static CompletionRule allOf(List<CompletionRule> rules) {
    return new CompletionRule(Type.ALL_OF, null, null, rules);
  }

  public static CompletionRule anyOf(List<CompletionRule> rules) {
    return new CompletionRule(Type.ANY_OF, null, null, rules);
  }
}


