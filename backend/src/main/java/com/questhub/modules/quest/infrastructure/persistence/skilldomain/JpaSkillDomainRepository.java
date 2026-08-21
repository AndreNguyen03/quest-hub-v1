package com.questhub.modules.quest.infrastructure.persistence.skilldomain;

import com.questhub.modules.quest.domain.skilldomain.SkillDomain;
import com.questhub.modules.quest.domain.skilldomain.SkillDomainRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaSkillDomainRepository implements SkillDomainRepository {

  private final SpringDataSkillDomainRepository jpa;

  public JpaSkillDomainRepository(SpringDataSkillDomainRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public Optional<SkillDomain> findById(UUID id) {
    return jpa.findById(id).map(SkillDomainMapper::toDomain);
  }

  @Override
  public boolean existsById(UUID id) {
    return jpa.existsById(id);
  }

  @Override
  public List<SkillDomain> findAll() {
    return jpa.findAll().stream().map(SkillDomainMapper::toDomain).toList();
  }
}

