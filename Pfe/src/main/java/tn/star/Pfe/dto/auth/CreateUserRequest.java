package tn.star.Pfe.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tn.star.Pfe.enums.Role;

import java.util.List;

public record CreateUserRequest(

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Format d'email invalide")
        String email,

        @NotBlank(message = "Le nom est obligatoire")
        String nom,

        @NotBlank(message = "Le prénom est obligatoire")
        String prenom,

        @NotNull(message = "Le rôle est obligatoire")
        Role role,

        String posteMembre,
        Long poleId,
        List<String> typesAutorisees
) {}