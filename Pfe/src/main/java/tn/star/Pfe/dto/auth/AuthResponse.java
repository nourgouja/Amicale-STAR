package tn.star.Pfe.dto.auth;

public record AuthResponse(String accessToken, String role, boolean firstLogin) {}