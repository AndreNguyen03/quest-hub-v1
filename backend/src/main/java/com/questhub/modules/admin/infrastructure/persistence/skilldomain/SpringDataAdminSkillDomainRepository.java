package com.questhub.modules.admin.infrastructure.persistence.skilldomain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAdminSkillDomainRepository extends JpaRepository<AdminSkillDomainJpaEntity, java.util.UUID> {

  Optional<AdminSkillDomainJpaEntity> findById(java.util.UUID id);
}
