package com.questhub.modules.world.application.dto;

import java.util.List;
import java.util.UUID;

public record WorldResponse(UUID id, List<DistrictResponse> districts) {

  public record DistrictResponse(
      UUID districtId,
      UUID domainId,
      String domainName,
      String domainSlug,
      int completionCount) {}
}
