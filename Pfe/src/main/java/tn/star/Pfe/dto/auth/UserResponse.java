package tn.star.Pfe.dto.auth;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        String nom,
        String prenom,
        String role,
        boolean actif ,
        LocalDateTime createdAt) {
}