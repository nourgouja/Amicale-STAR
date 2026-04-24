package tn.star.Pfe.controller.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.star.Pfe.dto.auth.UpdateProfilRequest;
import tn.star.Pfe.dto.auth.UserResponse;
import tn.star.Pfe.entity.User;
import tn.star.Pfe.mapper.UserMapper;
import tn.star.Pfe.security.UserPrincipal;
import tn.star.Pfe.service.user.IUserService;

@Slf4j
@RestController
@RequestMapping("/api/utilisateurs")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;
    private final UserMapper userMapper;

    @GetMapping("/profil")
    public ResponseEntity<UserResponse> getProfil(@AuthenticationPrincipal UserPrincipal principal) {
        User user = userService.findById(principal.getId());
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    @PutMapping("/profil")
    public ResponseEntity<UserResponse> updateProfil(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfilRequest request) {
        User updated = userService.updateUser(principal.getId(), request);
        return ResponseEntity.ok(userMapper.toResponse(updated));
    }

    @PatchMapping(value = "/profil/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> uploadPhoto(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestPart("photo") MultipartFile photo) {
        return ResponseEntity.ok(userService.uploadPhoto(principal.getId(), photo));
    }
}
