package tn.star.Pfe.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import tn.star.Pfe.entity.User;

import java.util.Collection;
import java.util.List;


public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String email;
    private final String motDePasse;
    private String role;
    private final boolean actif;
    private final boolean firstLogin;
    private final String prenom;
    private final String nom;


    public UserPrincipal(Long id, String email, String motDePasse, String role, boolean actif, boolean firstLogin, String prenom, String nom) {
        this.id = id;
        this.username = email;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
        this.actif = actif;
        this.firstLogin = firstLogin;
        this.prenom = prenom != null ? prenom : "";
        this.nom = nom != null ? nom : "";
    }

    public static UserPrincipal from(User u) {
        String role = u.getRole() != null ? u.getRole().name() : "ADHERENT";
        return new UserPrincipal(
                u.getId(),
                u.getEmail(),
                u.getMotDePasse(),
                role,
                u.isActif(),
                u.isFirstLogin(),
                u.getPrenom(),
                u.getNom()
        );
    }

    public Long getId() {
        return id;
    }

    public String getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return motDePasse;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return actif;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isFirstLogin() { return firstLogin; }
    public String getPrenom()     { return prenom; }
    public String getNom()        { return nom; }
}