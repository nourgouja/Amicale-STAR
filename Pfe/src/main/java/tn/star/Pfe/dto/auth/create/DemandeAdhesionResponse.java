package tn.star.Pfe.dto.auth;

import java.time.LocalDateTime;

public record DemandeAdhesionResponse(
        Long id,
        String nom,
        String prenom,
        String email,
        String telephone,
        String matriculeStar,
        String statut,
        LocalDateTime createdAt
) {}
