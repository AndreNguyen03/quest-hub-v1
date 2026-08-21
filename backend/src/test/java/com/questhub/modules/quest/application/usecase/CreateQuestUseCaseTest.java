package com.questhub.modules.quest.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.questhub.modules.quest.application.command.CreateQuestCommand;
import com.questhub.modules.quest.domain.learningpath.LearningPathRepository;
import com.questhub.modules.quest.domain.quest.CompletionRule;
import com.questhub.modules.quest.domain.quest.Difficulty;
import com.questhub.modules.quest.domain.quest.Quest;
import com.questhub.modules.quest.domain.quest.QuestRepository;
import com.questhub.modules.quest.domain.quest.QuestVisibility;
import com.questhub.modules.quest.domain.task.TaskType;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateQuestUseCaseTest {

  @Mock private QuestRepository questRepository;
  @Mock private LearningPathRepository learningPathRepository;

  @InjectMocks private CreateQuestUseCase useCase;

  @Test
  void create_withNestedChaptersAndTasks_shouldAssignPositionsAndDefaultDraft() {
    when(questRepository.save(any(Quest.class))).thenAnswer(inv -> inv.getArgument(0));

    Quest created =
        useCase.create(
            UUID.randomUUID(),
            requestWithTree(CompletionRule.defaultAllTasks()));

    assertThat(created).isNotNull();
    assertThat(created.getVisibility()).isEqualTo(QuestVisibility.DRAFT);
    assertThat(created.getCompletionRule().type()).isEqualTo(CompletionRule.Type.ALL_TASKS);
    assertThat(created.getChapters()).hasSize(2);
    assertThat(created.getChapters().get(0).getPosition()).isEqualTo(0);
    assertThat(created.getChapters().get(1).getPosition()).isEqualTo(1);
    assertThat(created.getChapters().get(0).getTasks()).hasSize(2);
    assertThat(created.getChapters().get(0).getTasks().get(0).getOrder()).isEqualTo(0);
    assertThat(created.getChapters().get(0).getTasks().get(1).getOrder()).isEqualTo(1);
    assertThat(created.getChapters().get(1).getTasks().get(0).getType()).isEqualTo(TaskType.QUIZ);

    verify(questRepository).save(created);
  }

  @Test
  void create_withQuizScoreRule_shouldKeepProvidedRule() {
    when(questRepository.save(any(Quest.class))).thenAnswer(inv -> inv.getArgument(0));
    CompletionRule rule = CompletionRule.quizScore(new BigDecimal("80"));

    Quest created = useCase.create(UUID.randomUUID(), requestWithTree(rule));

    assertThat(created.getCompletionRule().type()).isEqualTo(CompletionRule.Type.QUIZ_SCORE);
    assertThat(created.getCompletionRule().minScore()).isEqualByComparingTo("80");
  }

  @Test
  void create_whenLearningPathMissing_shouldThrowNotFoundAndNotSave() {
    UUID pathId = UUID.randomUUID();
    when(learningPathRepository.existsById(pathId)).thenReturn(false);
    CreateQuestCommand request = requestWithTree(CompletionRule.defaultAllTasks());
    CreateQuestCommand withPath =
        new CreateQuestCommand(
            request.title(),
            request.description(),
            pathId,
            request.difficulty(),
            request.completionRule(),
            request.reward(),
            request.chapters());

    BusinessException ex =
        catchThrowableOfType(() -> useCase.create(UUID.randomUUID(), withPath), BusinessException.class);

    assertThat(ex.getCode()).isEqualTo(ErrorCodes.NOT_FOUND);
    verify(questRepository, never()).save(any());
  }

  private CreateQuestCommand requestWithTree(CompletionRule rule) {
    return new CreateQuestCommand(
        "Spring Security Fundamentals",
        "Học Spring Security",
        null,
        Difficulty.INTERMEDIATE,
        rule,
        Map.of("icon", "🎖️"),
        List.of(
            new CreateQuestCommand.ChapterRequest(
                "Authentication",
                "Phần 1",
                List.of(
                    new CreateQuestCommand.TaskRequest(
                        TaskType.LEARN, "Xem video", "video về JWT", null),
                    new CreateQuestCommand.TaskRequest(
                        TaskType.PRACTICE, "Code login", "viết controller", Map.of("passThreshold", 80)))),
            new CreateQuestCommand.ChapterRequest(
                "Authorization",
                "Phần 2",
                List.of(
                    new CreateQuestCommand.TaskRequest(
                        TaskType.QUIZ, "Quiz role", "trắc nghiệm", Map.of("passThreshold", 80))))));
  }
}




