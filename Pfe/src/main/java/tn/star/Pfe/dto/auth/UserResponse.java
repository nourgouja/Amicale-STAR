package tn.star.Pfe.dto.auth;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        String nom,
        String prenom,
        String role,
        boolean actif,
        String telephone,
        String matriculeStar,
        String posteMembre,
        Long poleId,
        String poleNom,
        java.util.List<String> poleTypesOffre,
        LocalDateTime createdAt,
        String photoBase64,
        String photoType) {
}
