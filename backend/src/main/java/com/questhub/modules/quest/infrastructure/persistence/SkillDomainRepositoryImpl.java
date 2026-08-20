package com.questhub.modules.quest.infrastructure.persistence;

import com.questhub.modules.quest.domain.skilldomain.SkillDomain;
import com.questhub.modules.quest.domain.skilldomain.SkillDomainRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class SkillDomainRepositoryImpl implements SkillDomainRepository {

  private final SpringDataSkillDomainRepository jpa;

  public SkillDomainRepositoryImpl(SpringDataSkillDomainRepository jpa) {
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
}