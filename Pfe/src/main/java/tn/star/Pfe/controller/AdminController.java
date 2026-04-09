package tn.star.Pfe.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.star.Pfe.dto.auth.CreateUserRequest;
import tn.star.Pfe.dto.auth.UserResponse;
import tn.star.Pfe.enums.Role;
import tn.star.Pfe.service.UserService;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/utilisateurs")
    public ResponseEntity<Page<UserResponse>> listerTous(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(userService.findAll(role, search, page, size));
    }

    @PostMapping("/utilisateurs")
    public ResponseEntity<UserResponse> creer(
            @Valid @RequestBody CreateUserRequest req) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(req));
    }

    @PutMapping("/utilisateurs/{id}/activer")
    public ResponseEntity<Void> activer(@PathVariable Long id) {
        userService.toggleUserStatus(id, true);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/utilisateurs/{id}/desactiver")
    public ResponseEntity<Void> desactiver(@PathVariable Long id) {
        userService.toggleUserStatus(id, false);
        return ResponseEntity.noContent().build();
    }
}