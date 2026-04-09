package tn.star.Pfe.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.star.Pfe.dto.auth.AuthResponse;
import tn.star.Pfe.dto.auth.LoginRequest;
import tn.star.Pfe.exceptions.BadRequestException;
import tn.star.Pfe.repository.UserRepository;
import tn.star.Pfe.security.JwtUtils;
import tn.star.Pfe.security.UserPrincipal;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authManager;
    private final JwtUtils jwtUtils;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.motDePasse()
                    )
            );

            UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
            log.info("User logged in: {}", principal.getUsername());

            return buildAuthResponse(principal);

        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for: {}", request.email());
            throw new BadRequestException("Email ou mot de passe incorrect");
        }
    }

    public void logout() {
        log.info("User logged out");
    }

    private AuthResponse buildAuthResponse(UserPrincipal principal) {
        String role = principal.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_ADHERENT");

        String roleName = role.replace("ROLE_", "");

        return new AuthResponse(
                jwtUtils.generateAccessToken(principal),
                roleName,
                principal.isFirstLogin());
    }
}