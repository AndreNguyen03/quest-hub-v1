package com.questhub.modules.identity.presentation.rest;

import com.questhub.modules.identity.application.command.LoginCommand;
import com.questhub.modules.identity.application.usecase.LoginUseCase;
import com.questhub.modules.identity.application.command.RefreshCommand;
import com.questhub.modules.identity.application.usecase.RefreshUseCase;
import com.questhub.modules.identity.application.usecase.LogoutUseCase;
import com.questhub.modules.identity.application.command.RegisterUserCommand;
import com.questhub.modules.identity.application.service.TokenIssuer;
import com.questhub.modules.identity.application.usecase.RegisterUserUseCase;
import com.questhub.modules.identity.domain.user.User;
import com.questhub.modules.identity.application.dto.TokenPair;
import com.questhub.shared.presentation.dto.ApiResponse;
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
      @Valid @RequestBody RegisterUserCommand request) {
    User user = registerUserUseCase.register(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.ok(tokenIssuer.issue(user)));
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<TokenPair>> login(@Valid @RequestBody LoginCommand request) {
    User user = loginUseCase.login(request);
    return ResponseEntity.ok(ApiResponse.ok(tokenIssuer.issue(user)));
  }

  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<TokenPair>> refresh(@Valid @RequestBody RefreshCommand request) {
    User user = refreshUseCase.refresh(request.refreshToken());
    return ResponseEntity.ok(ApiResponse.ok(tokenIssuer.issue(user)));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@Valid @RequestBody RefreshCommand request) {
    logoutUseCase.logout(request.refreshToken());
    return ResponseEntity.noContent().build();
  }
}


