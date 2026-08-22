package com.questhub.modules.marketplace.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.questhub.modules.marketplace.application.command.CreateReviewCommand;
import com.questhub.modules.marketplace.domain.event.QuestRatedEventPublisher;
import com.questhub.modules.marketplace.domain.review.Review;
import com.questhub.modules.marketplace.domain.review.ReviewRepository;
import com.questhub.modules.quest.application.query.ExistsPersonalQuestQuery;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateReviewUseCaseTest {

  @Mock private ReviewRepository reviewRepository;
  @Mock private ExistsPersonalQuestQuery existsPersonalQuestQuery;
  @Mock private QuestRatedEventPublisher questRatedEventPublisher;

  @InjectMocks private CreateReviewUseCase useCase;

  @Test
  void create_withValidFork_shouldPersistAndPublishEvent() {
    UUID userId = UUID.randomUUID();
    UUID questId = UUID.randomUUID();
    CreateReviewCommand cmd = new CreateReviewCommand(5, "Excellent quest!");

    when(existsPersonalQuestQuery.exists(userId, questId)).thenReturn(true);
    when(reviewRepository.existsByQuestIdAndUserId(questId, userId)).thenReturn(false);
    when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

    Review result = useCase.create(questId, userId, cmd);

    assertThat(result.getScore()).isEqualTo(5);
    assertThat(result.getContent()).isEqualTo("Excellent quest!");
    verify(reviewRepository).save(any(Review.class));
    verify(questRatedEventPublisher).publish(eq(questId), eq(userId), eq(5), isNull(), eq("CREATED"));
  }

  @Test
  void create_notForked_shouldThrowForbidden() {
    UUID userId = UUID.randomUUID();
    UUID questId = UUID.randomUUID();

    when(existsPersonalQuestQuery.exists(userId, questId)).thenReturn(false);

    BusinessException ex = catchThrowableOfType(
        () -> useCase.create(questId, userId, new CreateReviewCommand(4, "Great")),
        BusinessException.class);

    assertThat(ex.getCode()).isEqualTo(ErrorCodes.FORBIDDEN);
    verify(reviewRepository, never()).save(any());
    verify(questRatedEventPublisher, never()).publish(any(UUID.class), any(UUID.class), anyInt(), any(), anyString());
  }

  @Test
  void create_duplicateReview_shouldThrowConflict() {
    UUID userId = UUID.randomUUID();
    UUID questId = UUID.randomUUID();

    when(existsPersonalQuestQuery.exists(userId, questId)).thenReturn(true);
    when(reviewRepository.existsByQuestIdAndUserId(questId, userId)).thenReturn(true);

    BusinessException ex = catchThrowableOfType(
        () -> useCase.create(questId, userId, new CreateReviewCommand(3, "OK")),
        BusinessException.class);

    assertThat(ex.getCode()).isEqualTo(ErrorCodes.CONFLICT);
    verify(reviewRepository, never()).save(any());
    verify(questRatedEventPublisher, never()).publish(any(UUID.class), any(UUID.class), anyInt(), any(), anyString());
  }
}
