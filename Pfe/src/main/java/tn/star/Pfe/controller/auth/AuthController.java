package tn.star.Pfe.controller.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.star.Pfe.dto.auth.AuthResponse;
import tn.star.Pfe.dto.auth.LoginRequest;
import tn.star.Pfe.dto.auth.UserResponse;
import tn.star.Pfe.entity.User;
import tn.star.Pfe.mapper.UserMapper;
import tn.star.Pfe.security.UserPrincipal;
import tn.star.Pfe.service.auth.IAuthService;
import tn.star.Pfe.service.user.IUserService;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;
    private final IUserService userService;
    private final UserMapper userMapper;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/currentUser")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal principal) {
        User user = userService.findById(principal.getId());
        return ResponseEntity.ok(userMapper.toResponse(user));
    }
}