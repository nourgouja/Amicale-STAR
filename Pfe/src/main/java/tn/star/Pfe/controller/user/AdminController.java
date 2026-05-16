package tn.star.Pfe.controller.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.star.Pfe.dto.auth.create.CreateUserRequest;
import tn.star.Pfe.dto.auth.update.UpdateProfilRequest;
import tn.star.Pfe.dto.auth.create.UserResponse;
import tn.star.Pfe.entity.user.User;
import tn.star.Pfe.enums.Role;
import tn.star.Pfe.mapper.UserMapper;
import tn.star.Pfe.repository.user.UserRepository;
import tn.star.Pfe.service.user.IUserService;

@RestController
@RequestMapping("/api/admin/utilisateurs")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final IUserService userService;
    private final UserMapper userMapper;
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Page<UserResponse>> listerTous(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "true") boolean actif) {
        return ResponseEntity.ok(userService.findAll(role, search, page, size, actif));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userMapper.toResponse(userService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<UserResponse> creer(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> modifier(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfilRequest request) {
        User updated = userService.updateUser(id, request);
        return ResponseEntity.ok(userMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponse> assignerRole(
            @PathVariable Long id,
            @RequestParam Role role) {
        User updated = userService.assignRole(id, role);
        return ResponseEntity.ok(userMapper.toResponse(updated));
    }

    @PatchMapping("/{id}/actif")
    public ResponseEntity<UserResponse> toggleStatut(
            @PathVariable Long id,
            @RequestParam boolean actif) {
        User updated = userService.toggleUserStatus(id, actif);
        return ResponseEntity.ok(userMapper.toResponse(updated));
    }

    @PostMapping("/{id}/reinitialiser-mot-de-passe")
    public ResponseEntity<Void> reinitialiserMotDePasse(@PathVariable Long id) {
        userService.adminResetPassword(id);
        return ResponseEntity.noContent().build();
    }


}
