package com.questhub.modules.marketplace.application.usecase;

import com.questhub.modules.marketplace.application.command.UpdateReviewCommand;
import com.questhub.modules.marketplace.domain.event.QuestRatedEventPublisher;
import com.questhub.modules.marketplace.domain.review.Review;
import com.questhub.modules.marketplace.domain.review.ReviewRepository;
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
public class UpdateReviewUseCase {

  private final ReviewRepository reviewRepository;
  private final QuestRatedEventPublisher questRatedEventPublisher;

  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public Review update(UUID questId, UUID userId, UpdateReviewCommand command) {
    Review review =
        reviewRepository.findByQuestId(questId).stream()
            .filter(r -> r.getUserId().equals(userId))
            .findFirst()
            .orElseThrow(
                () -> BusinessException.notFound(ErrorCodes.NOT_FOUND, "Không tìm thấy đánh giá"));

    int previousScore = review.getScore();
    review.update(command.score(), command.content());
    Review saved = reviewRepository.save(review);
    questRatedEventPublisher.publish(questId, userId, saved.getScore(), previousScore, "UPDATED");
    return saved;
  }
}
