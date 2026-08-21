package com.questhub.modules.admin.application.query;

import com.questhub.modules.admin.domain.skilldomain.AdminSkillDomain;
import com.questhub.modules.admin.domain.skilldomain.AdminSkillDomainRepository;
import com.questhub.shared.annotation.UseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class AdminListSkillDomainsQuery {

  private final AdminSkillDomainRepository adminSkillDomainRepository;

  @Transactional(
      readOnly = true,
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public List<AdminSkillDomain> list() {
    return adminSkillDomainRepository.findAll();
  }
}
