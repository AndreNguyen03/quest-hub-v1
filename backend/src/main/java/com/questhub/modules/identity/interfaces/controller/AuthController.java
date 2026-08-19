package com.questhub.modules.identity.interfaces.controller;

import com.questhub.modules.identity.application.request.LoginRequest;
import com.questhub.modules.identity.application.usecase.LoginUseCase;
import com.questhub.modules.identity.application.request.RefreshRequest;
import com.questhub.modules.identity.application.usecase.RefreshUseCase;
import com.questhub.modules.identity.application.usecase.LogoutUseCase;
import com.questhub.modules.identity.application.request.RegisterUserRequest;
import com.questhub.modules.identity.application.service.TokenIssuer;
import com.questhub.modules.identity.application.usecase.RegisterUserUseCase;
import com.questhub.modules.identity.domain.user.User;
import com.questhub.modules.identity.interfaces.dto.TokenPair;
import com.questhub.shared.interfaces.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final RegisterUserUseCase registerUserUseCase;
  private final LoginUseCase loginUseCase;
  private final RefreshUseCase refreshUseCase;
  private final LogoutUseCase logoutUseCase;
  private final TokenIssuer tokenIssuer;

  @PostMapping("/register")
  public ResponseEntity<ApiResponse<TokenPair>> register(
      @Valid @RequestBody RegisterUserRequest request) {
    User user = registerUserUseCase.register(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.ok(tokenIssuer.issue(user)));
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<TokenPair>> login(@Valid @RequestBody LoginRequest request) {
    User user = loginUseCase.login(request);
    return ResponseEntity.ok(ApiResponse.ok(tokenIssuer.issue(user)));
  }

  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<TokenPair>> refresh(@Valid @RequestBody RefreshRequest request) {
    User user = refreshUseCase.refresh(request.refreshToken());
    return ResponseEntity.ok(ApiResponse.ok(tokenIssuer.issue(user)));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
    logoutUseCase.logout(request.refreshToken());
    return ResponseEntity.noContent().build();
  }
}