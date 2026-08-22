package com.questhub.modules.quest.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.questhub.modules.quest.application.helper.QuestCreatorGuard;
import com.questhub.modules.quest.domain.chapter.Chapter;
import com.questhub.modules.quest.domain.quest.CompletionRule;
import com.questhub.modules.quest.domain.quest.Difficulty;
import com.questhub.modules.quest.domain.quest.Quest;
import com.questhub.modules.quest.domain.quest.QuestRepository;
import com.questhub.modules.quest.domain.task.Task;
import com.questhub.modules.quest.domain.task.TaskType;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SetCompletionRuleUseCaseTest {

  @Mock private QuestCreatorGuard questAccess;
  @Mock private QuestRepository questRepository;

  @InjectMocks private SetCompletionRuleUseCase useCase;

  @Test
  void setRule_onDraftQuest_shouldPersistAndReturnRule() {
    UUID creatorId = UUID.randomUUID();
    Quest quest = draftQuest(creatorId);
    when(questAccess.loadForWrite(quest.getId(), creatorId)).thenReturn(quest);
    when(questRepository.save(any(Quest.class))).thenAnswer(inv -> inv.getArgument(0));

    CompletionRule rule = CompletionRule.quizScore(new BigDecimal("80"));
    Quest saved = useCase.setRule(quest.getId(), creatorId, rule);

    assertThat(saved.getCompletionRule().type()).isEqualTo(CompletionRule.Type.QUIZ_SCORE);
    assertThat(saved.getCompletionRule().minScore()).isEqualByComparingTo("80");
    verify(questRepository).save(quest);
  }

  @Test
  void setRule_nonCreator_shouldThrowForbidden() {
    UUID questId = UUID.randomUUID();
    UUID actorId = UUID.randomUUID();
    when(questAccess.loadForWrite(questId, actorId))
        .thenThrow(BusinessException.forbidden(ErrorCodes.FORBIDDEN, "Chỉ creator mới sửa được quest"));

    BusinessException ex =
        catchThrowableOfType(
            () -> useCase.setRule(questId, actorId, CompletionRule.defaultAllTasks()),
            BusinessException.class);

    assertThat(ex.getCode()).isEqualTo(ErrorCodes.FORBIDDEN);
    verify(questRepository, never()).save(any());
  }

  @Test
  void setRule_onPublicQuest_shouldThrowConflict() {
    UUID creatorId = UUID.randomUUID();
    Quest quest = draftQuest(creatorId);
    quest.publish();
    when(questAccess.loadForWrite(quest.getId(), creatorId)).thenReturn(quest);

    BusinessException ex =
        catchThrowableOfType(
            () -> useCase.setRule(quest.getId(), creatorId, CompletionRule.defaultAllTasks()),
            BusinessException.class);

    assertThat(ex.getCode()).isEqualTo(ErrorCodes.CONFLICT);
    verify(questRepository, never()).save(any());
  }

  private Quest draftQuest(UUID creatorId) {
    Quest quest = Quest.create(creatorId, null, "title", "desc", Difficulty.BEGINNER, Map.of());
    Chapter chapter = Chapter.create("c1", "d1", 0);
    chapter.addTask(Task.create(TaskType.LEARN, "t1", "d1", 0, Map.of()));
    quest.addChapter(chapter);
    return quest;
  }
}


