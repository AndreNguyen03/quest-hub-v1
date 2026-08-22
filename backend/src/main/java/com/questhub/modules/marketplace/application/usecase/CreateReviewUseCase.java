package com.questhub.modules.marketplace.application.usecase;

import com.questhub.modules.marketplace.application.command.CreateReviewCommand;
import com.questhub.modules.marketplace.application.event.QuestRatedEventPublisher;
import com.questhub.modules.marketplace.domain.review.Review;
import com.questhub.modules.marketplace.domain.review.ReviewRepository;
import com.questhub.modules.quest.application.api.QuestPublicApi;
import com.questhub.shared.annotation.UseCase;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class CreateReviewUseCase {

  private final ReviewRepository reviewRepository;
  private final QuestPublicApi questPublicApi;
  private final QuestRatedEventPublisher questRatedEventPublisher;

  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public Review create(UUID questId, UUID userId, CreateReviewCommand command) {
    if (!questPublicApi.existsPersonalQuest(userId, questId)) {
      throw BusinessException.forbidden(ErrorCodes.FORBIDDEN, "Bạn cần fork quest trước khi đánh giá");
    }
    if (reviewRepository.existsByQuestIdAndUserId(questId, userId)) {
      throw BusinessException.conflict(ErrorCodes.CONFLICT, "Bạn đã đánh giá quest này rồi");
    }

    Review review = Review.create(questId, userId, command.score(), command.content());
    Review saved = reviewRepository.save(review);
    questRatedEventPublisher.publish(questId, userId, saved.getScore(), null, "CREATED");
    return saved;
  }
}
