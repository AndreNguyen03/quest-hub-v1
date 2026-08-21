package com.questhub.modules.admin.application.query;

import com.questhub.modules.admin.domain.featureflag.FeatureFlag;
import com.questhub.modules.admin.domain.featureflag.FeatureFlagRepository;
import com.questhub.shared.annotation.UseCase;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class GetFeatureFlagQuery {

  private final FeatureFlagRepository featureFlagRepository;

  @Transactional(
      readOnly = true,
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public FeatureFlag get(String key) {
    return featureFlagRepository
        .findById(key)
        .orElseThrow(
            () -> BusinessException.notFound(ErrorCodes.NOT_FOUND, "Feature flag không tồn tại"));
  }
}
