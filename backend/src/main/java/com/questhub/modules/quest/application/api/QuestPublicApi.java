package com.questhub.modules.quest.application.api;

import com.questhub.modules.quest.application.dto.LeaderboardStatDto;
import com.questhub.modules.quest.application.dto.LearningPathDto;
import com.questhub.modules.quest.application.dto.PersonalQuestSummaryDto;
import com.questhub.modules.quest.application.dto.QuestDto;
import com.questhub.modules.quest.application.dto.SkillDomainDto;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Public contract của Quest bounded context.
 *
 * <p>Module khác CHỈ được phụ thuộc interface này cùng các DTO trong
 * {@code quest.application.dto}, không import trực tiếp query/usecase/repository
 * hay domain entity bên trong quest.
 */
public interface QuestPublicApi {

  List<QuestDto> popularQuests(int limit);

  List<QuestDto> trendingQuests(Instant since, int limit);

  /** Trả về tên visibility ("PUBLIC"/"PRIVATE"...), ném BusinessException NOT_FOUND nếu không tồn tại. */
  String questVisibility(UUID questId);

  boolean existsPersonalQuest(UUID userId, UUID questId);

  List<LearningPathDto> publicLearningPaths();

  List<SkillDomainDto> listSkillDomains();

  Optional<SkillDomainDto> findSkillDomain(UUID id);

  List<PersonalQuestSummaryDto> personalQuestsByUserAndDomain(UUID userId, UUID domainId);

  List<LeaderboardStatDto> topCompletionStats(int limit);
}
