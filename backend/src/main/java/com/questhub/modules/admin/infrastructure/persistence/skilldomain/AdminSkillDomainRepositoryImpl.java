package com.questhub.modules.admin.infrastructure.persistence.skilldomain;

import com.questhub.modules.admin.domain.skilldomain.AdminSkillDomain;
import com.questhub.modules.admin.domain.skilldomain.AdminSkillDomainRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AdminSkillDomainRepositoryImpl implements AdminSkillDomainRepository {

  private final SpringDataAdminSkillDomainRepository jpa;

  public AdminSkillDomainRepositoryImpl(SpringDataAdminSkillDomainRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public AdminSkillDomain save(AdminSkillDomain skillDomain) {
    AdminSkillDomainJpaEntity entity =
        new AdminSkillDomainJpaEntity(
            skillDomain.getId(),
            skillDomain.getName(),
            skillDomain.getSlug(),
            skillDomain.getDescription(),
            skillDomain.getIcon(),
            skillDomain.isActive(),
            skillDomain.getCreatedAt(),
            skillDomain.getUpdatedAt());
    jpa.save(entity);
    return skillDomain;
  }

  @Override
  public List<AdminSkillDomain> findAll() {
    return jpa.findAll().stream()
        .map(
            e ->
                AdminSkillDomain.restore(
                    e.getId(),
                    e.getName(),
                    e.getSlug(),
                    e.getDescription(),
                    e.getIcon(),
                    e.isActive(),
                    e.getCreatedAt(),
                    e.getUpdatedAt()))
        .toList();
  }

  @Override
  public Optional<AdminSkillDomain> findById(UUID id) {
    return jpa.findById(id)
        .map(
            e ->
                AdminSkillDomain.restore(
                    e.getId(),
                    e.getName(),
                    e.getSlug(),
                    e.getDescription(),
                    e.getIcon(),
                    e.isActive(),
                    e.getCreatedAt(),
                    e.getUpdatedAt()));
  }
}
