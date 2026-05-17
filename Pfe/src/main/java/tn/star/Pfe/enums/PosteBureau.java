package tn.star.Pfe.enums;

public enum PosteBureau {
    PRESIDENT("Président"),
    SECRETARY("Secrétaire"),
    TREASURER("Trésorier"),
    RESPONSABLE_POLE_VOYAGE_SEJOURS("Responsable Pôle Voyage & Séjours"),
    RESPONSABLE_POLE_ACTIVITES_LOISIRS("Responsable Pôle Activités & Loisirs"),
    RESPONSABLE_POLE_EVENEMENTS_CONVENTIONS("Responsable Pôle Événements & Conventions"),
    RESPONSABLE_POLE("Responsable de Pôle"),
    MEMBER("Membre");

    private final String label;

    PosteBureau(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}