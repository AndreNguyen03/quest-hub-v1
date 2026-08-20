package com.questhub.modules.quest.domain.skilldomain;

import java.util.Optional;
import java.util.UUID;

public interface SkillDomainRepository {

  Optional<SkillDomain> findById(UUID id);

  boolean existsById(UUID id);
}