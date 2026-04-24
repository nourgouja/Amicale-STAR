package tn.star.Pfe.dto.auth;

import jakarta.validation.constraints.Email;

public record UpdateProfilRequest(
        String nom,
        String prenom,
        String motDePasse,
        @Email String email,
        String telephone,
        String posteMembre,
        Long poleId,
        String matriculeStar
) {}
