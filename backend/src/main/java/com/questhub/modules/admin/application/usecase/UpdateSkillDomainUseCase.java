package com.questhub.modules.admin.application.usecase;

import com.questhub.modules.admin.application.command.UpdateSkillDomainCommand;
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
public class UpdateSkillDomainUseCase {

  private final AdminSkillDomainRepository adminSkillDomainRepository;

  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public AdminSkillDomain update(UpdateSkillDomainCommand command) {
    UUID id = command.id();
    String name = command.name();
    String slug = command.slug();
    AdminSkillDomain domain =
        adminSkillDomainRepository
            .findById(id)
            .orElseThrow(
                () -> BusinessException.notFound(ErrorCodes.NOT_FOUND, "Skill domain không tồn tại"));

    adminSkillDomainRepository.findAll().stream()
        .filter(d -> !d.getId().equals(id) && (d.getName().equals(name) || d.getSlug().equals(slug)))
        .findFirst()
        .ifPresent(
            d -> {
              throw BusinessException.conflict(ErrorCodes.CONFLICT, "Tên hoặc slug đã được sử dụng");
            });

    domain.update(name, slug, command.description(), command.icon());
    return adminSkillDomainRepository.save(domain);
  }
}
