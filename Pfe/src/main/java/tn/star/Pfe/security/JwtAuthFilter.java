package tn.star.Pfe.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String token = parseJwt(request);

        if (token == null || !jwtUtils.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        Boolean actif = jwtUtils.extractClaim(token, "actif", Boolean.class);
        if (!Boolean.TRUE.equals(actif)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Compte désactivé");
            return;
        }

        Long userId= jwtUtils.extractClaim(token, "userId", Long.class);
        String email= jwtUtils.extractEmail(token);
        String role= jwtUtils.extractClaim(token, "role", String.class);
        Boolean firstLogin = jwtUtils.extractClaim(token, "firstLogin", Boolean.class);

        UserPrincipal principal = new UserPrincipal(
                userId, email, null, role,
                true,
                Boolean.TRUE.equals(firstLogin)
        );

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities()
                );
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}