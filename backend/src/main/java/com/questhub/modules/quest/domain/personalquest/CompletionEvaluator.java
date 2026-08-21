package com.questhub.modules.quest.domain.personalquest;

import com.questhub.modules.quest.domain.quest.CompletionRule;
import com.questhub.modules.quest.domain.task.TaskType;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CompletionEvaluator {

  public boolean isSatisfied(
      CompletionRule rule, PersonalQuest quest, Map<UUID, BigDecimal> bestQuizPercent) {
    return switch (rule.type()) {
      case ALL_TASKS -> allTasksCompleted(quest);
      case QUIZ_SCORE -> quizScoreSatisfied(quest, rule.minScore(), bestQuizPercent);
      case SUBMISSION -> submissionSatisfied(quest, rule.requiredTaskTypes());
      case ALL_OF -> rule.rules().stream()
          .allMatch(sub -> isSatisfied(sub, quest, bestQuizPercent));
      case ANY_OF -> rule.rules().stream()
          .anyMatch(sub -> isSatisfied(sub, quest, bestQuizPercent));
    };
  }

  private boolean allTasksCompleted(PersonalQuest quest) {
    List<PersonalTask> tasks = quest.getAllTasks();
    return tasks.isEmpty() || tasks.stream().allMatch(PersonalTask::isCompleted);
  }

  private boolean quizScoreSatisfied(
      PersonalQuest quest, BigDecimal minScore, Map<UUID, BigDecimal> bestQuizPercent) {
    List<PersonalTask> quizTasks =
        quest.getAllTasks().stream().filter(t -> t.getType() == TaskType.QUIZ).toList();
    if (quizTasks.isEmpty()) {
      return false;
    }
    return quizTasks.stream()
        .allMatch(
            task -> {
              BigDecimal percent = bestQuizPercent.getOrDefault(task.getId(), BigDecimal.ZERO);
              return percent.compareTo(minScore) >= 0;
            });
  }

  private boolean submissionSatisfied(
      PersonalQuest quest, List<TaskType> requiredTaskTypes) {
    if (requiredTaskTypes == null || requiredTaskTypes.isEmpty()) {
      return false;
    }
    Set<TaskType> required = new HashSet<>(requiredTaskTypes);
    List<PersonalTask> tasks = quest.getAllTasks();
    if (tasks.isEmpty()) {
      return false;
    }
    return tasks.stream()
        .allMatch(task -> !required.contains(task.getType()) || task.isCompleted());
  }
}


