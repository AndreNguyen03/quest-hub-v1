package com.questhub.modules.marketplace.presentation.rest;

import com.questhub.modules.marketplace.application.dto.MarketplaceHomeResponse;
import com.questhub.modules.marketplace.application.dto.MarketplaceHomeResponseMapper;
import com.questhub.modules.marketplace.application.query.MarketplaceHomeQuery;
import com.questhub.shared.interfaces.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/marketplace")
@RequiredArgsConstructor
public class MarketplaceController {

  private final MarketplaceHomeQuery marketplaceHomeQuery;

  @GetMapping("/home")
  public ResponseEntity<ApiResponse<MarketplaceHomeResponse>> home() {
    MarketplaceHomeQuery.HomeResult result = marketplaceHomeQuery.get();
    MarketplaceHomeResponse response = MarketplaceHomeResponseMapper.toResponse(result);
    return ResponseEntity.ok(ApiResponse.ok(response));
  }
}





