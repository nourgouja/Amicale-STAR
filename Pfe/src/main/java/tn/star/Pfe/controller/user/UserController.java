package tn.star.Pfe.controller.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.star.Pfe.dto.auth.*;
import tn.star.Pfe.entity.User;
import tn.star.Pfe.enums.Role;
import tn.star.Pfe.mapper.UserMapper;
import tn.star.Pfe.security.UserPrincipal;
import tn.star.Pfe.service.user.IUserService;

@Slf4j
@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;
    private final UserMapper userMapper;

    @PostMapping("/demande-adhesion")
    public ResponseEntity<Void> demanderAdhesion(@Valid @RequestBody DemandeRequest request) {
        userService.demanderAdhesion(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/profil")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getProfil(@AuthenticationPrincipal UserPrincipal principal) {
        User user = userService.findById(principal.getId());
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

//    @PutMapping("/profil")
//    @PreAuthorize("isAuthenticated()")
//    public ResponseEntity<UserResponse> updateProfil(
//            @AuthenticationPrincipal UserPrincipal principal,
//            @Valid @RequestBody UpdateProfilRequest request) {
//
//        User updated = userService.updateProfile(principal.getId(), request);
//        return ResponseEntity.ok(userMapper.toResponse(updated));
//    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<UserResponse> response = userService.findAll(role, search, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse created = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfilRequest request) {

        User updated = userService.updateUser(id, request);
        return ResponseEntity.ok(userMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> assignRole(
            @PathVariable Long id,
            @RequestParam Role role) {

        User updated = userService.assignRole(id, role);
        return ResponseEntity.ok(userMapper.toResponse(updated));
    }

    @PatchMapping("/{id}/actif")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> toggleUserStatus(
            @PathVariable Long id,
            @RequestParam boolean actif) {

        User updated = userService.toggleUserStatus(id, actif);
        return ResponseEntity.ok(userMapper.toResponse(updated));
    }

    @PostMapping("/{id}/reinitialiser-mot-de-passe")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> adminResetPassword(@PathVariable Long id) {
        userService.adminResetPassword(id);
        return ResponseEntity.noContent().build();
    }

}