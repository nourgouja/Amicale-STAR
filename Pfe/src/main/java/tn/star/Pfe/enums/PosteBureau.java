package tn.star.Pfe.enums;

public enum PosteBureau {
    PRESIDENT("Président"),
    VICE_PRESIDENT("Vice-Président"),
    SECRETARY("Secrétaire"),
    TRESORIER("Trésorier"),
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