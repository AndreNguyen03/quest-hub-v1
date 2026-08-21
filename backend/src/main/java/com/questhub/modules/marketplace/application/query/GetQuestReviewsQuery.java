package com.questhub.modules.marketplace.application.query;

import com.questhub.modules.marketplace.domain.review.Review;
import com.questhub.modules.marketplace.domain.review.ReviewRepository;
import com.questhub.shared.annotation.UseCase;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class GetQuestReviewsQuery {

  private final ReviewRepository reviewRepository;

  @Transactional(
      readOnly = true,
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public List<Review> get(UUID questId, int page, int limit) {
    return reviewRepository.findByQuestId(questId).stream()
        .skip((long) page * limit)
        .limit(limit)
        .toList();
  }
}
