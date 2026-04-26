package tn.star.Pfe.dto.auth;

public record MembreBureauPublicResponse(
        Long id,
        String nom,
        String prenom,
        String poste,
        String poleNom
) {}
