package tn.star.Pfe.security;

public record JwtPrincipal(Long userId, String email, String role) {}