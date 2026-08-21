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
public class DeactivateSkillDomainUseCase {

  private final AdminSkillDomainRepository adminSkillDomainRepository;

  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public void deactivate(UUID id) {
    AdminSkillDomain domain =
        adminSkillDomainRepository
            .findById(id)
            .orElseThrow(
                () -> BusinessException.notFound(ErrorCodes.NOT_FOUND, "Skill domain không tồn tại"));
    domain.deactivate();
    adminSkillDomainRepository.save(domain);
  }
}
