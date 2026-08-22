package com.questhub.modules.admin.application.usecase;

import com.questhub.modules.admin.application.command.ToggleFeatureFlagCommand;
import com.questhub.modules.admin.domain.featureflag.FeatureFlag;
import com.questhub.modules.admin.domain.featureflag.FeatureFlagRepository;
import com.questhub.modules.admin.domain.exception.AdminException;
import com.questhub.shared.annotation.UseCase;
import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.ErrorCodes;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class ToggleFeatureFlagUseCase {

  private final FeatureFlagRepository featureFlagRepository;

  @Transactional(
      isolation = Isolation.DEFAULT,
      rollbackFor = Exception.class,
      propagation = Propagation.REQUIRED)
  public FeatureFlag toggle(ToggleFeatureFlagCommand command) {
    String key = command.key();
    Map<String, Object> value = command.value();
    String description = command.description();
    FeatureFlag existing =
        featureFlagRepository
            .findById(key)
            .orElse(null);
    if (existing == null) {
      FeatureFlag created = FeatureFlag.create(key, value, description);
      return featureFlagRepository.save(created);
    }
    existing.toggle(value);
    if (description != null) {
      existing.update(value, description);
    }
    return featureFlagRepository.save(existing);
  }
}
