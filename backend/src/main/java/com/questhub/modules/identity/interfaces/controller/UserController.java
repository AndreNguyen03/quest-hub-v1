package com.questhub.modules.identity.interfaces.controller;

import com.questhub.modules.identity.application.usecase.GetCurrentUserUseCase;
import com.questhub.modules.identity.application.request.UpdateProfileRequest;
import com.questhub.modules.identity.application.usecase.UpdateProfileUseCase;
import com.questhub.modules.identity.domain.user.User;
import com.questhub.modules.identity.interfaces.dto.UserResponse;
import com.questhub.shared.domain.AuthenticatedUser;
import com.questhub.shared.interfaces.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

  private final GetCurrentUserUseCase getCurrentUserUseCase;
  private final UpdateProfileUseCase updateProfileUseCase;

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
    AuthenticatedUser current =
        (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    User user = getCurrentUserUseCase.getById(current.id());
    return ResponseEntity.ok(ApiResponse.ok(UserResponse.from(user)));
  }

  @PutMapping("/me")
  public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
      @Valid @RequestBody UpdateProfileRequest request) {
    AuthenticatedUser current =
        (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    User user = updateProfileUseCase.update(current.id(), request);
    return ResponseEntity.ok(ApiResponse.ok(UserResponse.from(user)));
  }
}
