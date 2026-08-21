package com.questhub.modules.admin.application.query;

import com.questhub.modules.admin.domain.featureflag.FeatureFlag;
import com.questhub.modules.admin.domain.featureflag.FeatureFlagRepository;
import com.questhub.shared.annotation.UseCase;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class ListFeatureFlagsQuery {

  private final FeatureFlagRepository featureFlagRepository;

  @Transactional(
      readOnly = true,
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public List<FeatureFlag> list() {
    return featureFlagRepository.findAll();
  }
}
