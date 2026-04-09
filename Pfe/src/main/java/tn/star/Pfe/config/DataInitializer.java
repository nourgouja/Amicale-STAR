package tn.star.Pfe.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import tn.star.Pfe.entity.Admin;
import tn.star.Pfe.enums.Role;
import tn.star.Pfe.repository.UserRepository;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Bean
    CommandLineRunner initAdmin() {
        return args -> {

            if (userRepository.findByEmail(adminEmail).isEmpty()) {

                Admin admin = Admin.builder()
                        .nom("Admin").prenom("Admin")
                        .email(adminEmail)
                        .motDePasse(passwordEncoder.encode(adminPassword))
                        .role(Role.ADMIN)
                        .actif(true)
                        .firstLogin(false)
                        .build();

                userRepository.save(admin);

                System.out.println("ADMIN créé !");
            }
        };
    }
}