package com.questhub.modules.admin.domain.skilldomain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdminSkillDomainRepository {

  AdminSkillDomain save(AdminSkillDomain skillDomain);

  List<AdminSkillDomain> findAll();

  Optional<AdminSkillDomain> findById(UUID id);
}
