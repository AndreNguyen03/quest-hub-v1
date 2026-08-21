package com.questhub.modules.marketplace.application.helper;

import com.questhub.modules.quest.application.query.GetQuestVisibilityQuery;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestAccess {

  private final GetQuestVisibilityQuery getQuestVisibilityQuery;

  public void verifyPublic(UUID questId) {
    String visibility = getQuestVisibilityQuery.get(questId);
    if (!"PUBLIC".equals(visibility)) {
      throw BusinessException.forbidden(ErrorCodes.FORBIDDEN, "Quest không công khai");
    }
  }
}
