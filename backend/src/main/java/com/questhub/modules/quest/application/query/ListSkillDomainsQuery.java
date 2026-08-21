package com.questhub.modules.quest.application.query;

import com.questhub.modules.quest.application.dto.SkillDomainDto;
import com.questhub.modules.quest.domain.skilldomain.SkillDomainRepository;
import com.questhub.shared.annotation.UseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class ListSkillDomainsQuery {

  private final SkillDomainRepository skillDomainRepository;

  @Transactional(
      readOnly = true,
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public List<SkillDomainDto> list() {
    return skillDomainRepository.findAll().stream()
        .map(s -> new SkillDomainDto(s.getId(), s.getName(), s.getSlug()))
        .toList();
  }
}
