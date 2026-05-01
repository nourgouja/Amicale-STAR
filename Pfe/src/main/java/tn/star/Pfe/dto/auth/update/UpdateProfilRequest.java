package tn.star.Pfe.dto.auth;

import jakarta.validation.constraints.Email;
import tn.star.Pfe.enums.Role;

import java.util.List;

public record UpdateProfilRequest(
        String nom,
        String prenom,
        String motDePasse,
        @Email String email,
        String telephone,
        String posteMembre,
        Long poleId,
        String matriculeStar,
        List<String> typesAutorisees,
        Role role
) {}
