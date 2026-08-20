package com.questhub.modules.quest.infrastructure.persistence;

import com.questhub.modules.quest.domain.skilldomain.SkillDomain;

public final class SkillDomainMapper {

  private SkillDomainMapper() {}

  public static SkillDomain toDomain(SkillDomainEntity entity) {
    return SkillDomain.restore(entity.getId(), entity.getName(), entity.getSlug());
  }
}