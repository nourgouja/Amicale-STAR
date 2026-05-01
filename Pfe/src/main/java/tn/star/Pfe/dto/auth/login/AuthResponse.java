package tn.star.Pfe.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(String accessToken, String role, boolean firstLogin) {}