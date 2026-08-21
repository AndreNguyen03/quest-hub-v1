package com.questhub.modules.identity.presentation.rest;

import com.questhub.modules.identity.application.query.GetUserProfileQuery;
import com.questhub.modules.identity.application.command.UpdateProfileCommand;
import com.questhub.modules.identity.application.usecase.UpdateProfileUseCase;
import com.questhub.modules.identity.domain.user.User;
import com.questhub.modules.identity.application.dto.UserResponse;
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

  private final GetUserProfileQuery GetUserProfileQuery;
  private final UpdateProfileUseCase updateProfileUseCase;

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
    AuthenticatedUser current =
        (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    User user = GetUserProfileQuery.getById(current.id());
    return ResponseEntity.ok(ApiResponse.ok(UserResponse.from(user)));
  }

  @PutMapping("/me")
  public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
      @Valid @RequestBody UpdateProfileCommand request) {
    AuthenticatedUser current =
        (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    User user = updateProfileUseCase.update(current.id(), request);
    return ResponseEntity.ok(ApiResponse.ok(UserResponse.from(user)));
  }
}




