package com.questhub.modules.quest.application.api;

import com.questhub.modules.quest.application.dto.LeaderboardStatDto;
import com.questhub.modules.quest.application.dto.LearningPathDto;
import com.questhub.modules.quest.application.dto.PersonalQuestSummaryDto;
import com.questhub.modules.quest.application.dto.QuestDto;
import com.questhub.modules.quest.application.dto.SkillDomainDto;
import com.questhub.modules.quest.application.query.ExistsPersonalQuestQuery;
import com.questhub.modules.quest.application.query.GetLeaderboardStatsQuery;
import com.questhub.modules.quest.application.query.GetPopularQuestsQuery;
import com.questhub.modules.quest.application.query.GetQuestVisibilityQuery;
import com.questhub.modules.quest.application.query.GetSkillDomainQuery;
import com.questhub.modules.quest.application.query.GetTrendingQuestsQuery;
import com.questhub.modules.quest.application.query.ListPersonalQuestsByDomainQuery;
import com.questhub.modules.quest.application.query.ListPublicLearningPathsQuery;
import com.questhub.modules.quest.application.query.ListSkillDomainsQuery;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Adapter expose internal queries của Quest qua contract QuestPublicApi. */
@Component
@RequiredArgsConstructor
public class QuestPublicApiAdapter implements QuestPublicApi {

  private final GetPopularQuestsQuery getPopularQuestsQuery;
  private final GetTrendingQuestsQuery getTrendingQuestsQuery;
  private final GetQuestVisibilityQuery getQuestVisibilityQuery;
  private final ExistsPersonalQuestQuery existsPersonalQuestQuery;
  private final ListPublicLearningPathsQuery listPublicLearningPathsQuery;
  private final ListSkillDomainsQuery listSkillDomainsQuery;
  private final GetSkillDomainQuery getSkillDomainQuery;
  private final ListPersonalQuestsByDomainQuery listPersonalQuestsByDomainQuery;
  private final GetLeaderboardStatsQuery getLeaderboardStatsQuery;

  @Override
  public List<QuestDto> popularQuests(int limit) {
    return getPopularQuestsQuery.get(limit);
  }

  @Override
  public List<QuestDto> trendingQuests(Instant since, int limit) {
    return getTrendingQuestsQuery.get(since, limit);
  }

  @Override
  public String questVisibility(UUID questId) {
    return getQuestVisibilityQuery.get(questId);
  }

  @Override
  public boolean existsPersonalQuest(UUID userId, UUID questId) {
    return existsPersonalQuestQuery.exists(userId, questId);
  }

  @Override
  public List<LearningPathDto> publicLearningPaths() {
    return listPublicLearningPathsQuery.list();
  }

  @Override
  public List<SkillDomainDto> listSkillDomains() {
    return listSkillDomainsQuery.list();
  }

  @Override
  public Optional<SkillDomainDto> findSkillDomain(UUID id) {
    return getSkillDomainQuery.byId(id);
  }

  @Override
  public List<PersonalQuestSummaryDto> personalQuestsByUserAndDomain(UUID userId, UUID domainId) {
    return listPersonalQuestsByDomainQuery.byUserAndDomain(userId, domainId);
  }

  @Override
  public List<LeaderboardStatDto> topCompletionStats(int limit) {
    return getLeaderboardStatsQuery.top(limit);
  }
}
