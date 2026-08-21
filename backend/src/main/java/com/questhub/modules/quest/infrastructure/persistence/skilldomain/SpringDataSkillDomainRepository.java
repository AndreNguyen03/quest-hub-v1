package com.questhub.modules.quest.infrastructure.persistence.skilldomain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataSkillDomainRepository extends JpaRepository<SkillDomainJpaEntity, UUID> {}

