package tn.star.Pfe.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tn.star.Pfe.security.CustomUserDetailsService;
import tn.star.Pfe.security.JwtAuthFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Autowired
    private final JwtAuthFilter jwtAuthFilter;

    @Autowired
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/forgot-password",
                                "/api/auth/refresh",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/offres/**").authenticated()

                        .requestMatchers(HttpMethod.POST, "/api/utilisateurs/demande-adhesion").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/offres/**").hasAnyRole("MEMBRE_BUREAU", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/offres/**").hasAnyRole("MEMBRE_BUREAU", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/offres/**").hasAnyRole("MEMBRE_BUREAU", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/offres/**").hasAnyRole("MEMBRE_BUREAU", "ADMIN")

                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        //inscriptions adherent
                        .requestMatchers(HttpMethod.POST, "/api/inscriptions/**").hasRole("ADHERENT")
                        .requestMatchers(HttpMethod.PATCH, "/api/inscriptions/annuler/**").hasRole("ADHERENT")
                        .requestMatchers(HttpMethod.GET, "/api/inscriptions/mesinscriptions").hasRole("ADHERENT")

                        //inscriptions bureau admin
                        .requestMatchers(HttpMethod.PATCH, "/api/inscriptions/confirmer/**").hasAnyRole("MEMBRE_BUREAU", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/inscriptions/offre/**").hasAnyRole("MEMBRE_BUREAU", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/inscriptions/{id}/paiement").hasAnyRole("MEMBRE_BUREAU", "ADMIN")

                        .requestMatchers("/api/utilisateurs/profil").authenticated()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }


}