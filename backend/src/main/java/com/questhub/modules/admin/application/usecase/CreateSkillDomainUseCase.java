package com.questhub.modules.admin.application.usecase;

import com.questhub.modules.admin.domain.skilldomain.AdminSkillDomain;
import com.questhub.modules.admin.domain.skilldomain.AdminSkillDomainRepository;
import com.questhub.shared.annotation.UseCase;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class CreateSkillDomainUseCase {

  private final AdminSkillDomainRepository adminSkillDomainRepository;

  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public AdminSkillDomain create(String name, String slug, String description, String icon) {
    AdminSkillDomain existing =
        adminSkillDomainRepository.findAll().stream()
            .filter(d -> d.getName().equals(name) || d.getSlug().equals(slug))
            .findFirst()
            .orElse(null);
    if (existing != null) {
      throw BusinessException.conflict(ErrorCodes.CONFLICT, "Skill domain đã tồn tại");
    }

    AdminSkillDomain domain = AdminSkillDomain.create(UUID.randomUUID(), name, slug, description, icon);
    return adminSkillDomainRepository.save(domain);
  }
}
