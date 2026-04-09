package tn.star.Pfe.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.star.Pfe.dto.auth.ChangePasswordRequest;
import tn.star.Pfe.dto.auth.CreateUserRequest;
import tn.star.Pfe.dto.auth.UpdateProfilRequest;
import tn.star.Pfe.dto.auth.UserResponse;
import tn.star.Pfe.entity.*;
import tn.star.Pfe.enums.Role;
import tn.star.Pfe.exceptions.BadRequestException;
import tn.star.Pfe.exceptions.NotFoundException;
import tn.star.Pfe.mapper.UserMapper;
import tn.star.Pfe.repository.UserRepository;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.security.CryptoPrimitive.SECURE_RANDOM;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%";
//    @Transactional
//    public User updateProfile(int userId, UpdateProfilRequest request) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé avec ID: " + userId));
//
//        if (request.nom() != null && !request.nom().isBlank()) {
//            user.setNom(request.nom());
//        }
//        if (request.prenom() != null && !request.prenom().isBlank()) {
//            user.setPrenom(request.prenom());
//        }
//        if (request.motDePasse() != null && !request.motDePasse().isBlank()) {
//            if (request.motDePasse().length() < 6) {
//                throw new BadRequestException("Le mot de passe doit contenir au moins 6 caractères");
//            }
//            user.setMotDePasse(passwordEncoder.encode(request.motDePasse()));
//        }
//
//        return userRepository.save(user);
//    }


    @Transactional
    public Page<UserResponse> findAll(Role role, String search, int page, int size) {
        int safeSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, safeSize);

        Page<User> result;
        if (role != null && search != null && !search.isBlank()) {
            result = userRepository.findByRoleAndSearch(role, search, pageable);
        } else if (role != null) {
            result = userRepository.findByRole(role, pageable);
        } else if (search != null && !search.isBlank()) {
            result = userRepository.searchByKeyword(search, pageable);
        } else {
            result = userRepository.findAll(pageable);
        }

        return result.map(userMapper::toResponse);
    }

    @Transactional
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé avec ID: " + id));
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email déjà utilisé: " + request.email());
        }

        String hashedPassword = passwordEncoder.encode(request.motDePasse());
        User user = buildUserByRole(request, hashedPassword);
        User saved = userRepository.save(user);

        emailService.sendWelcomeEmail(saved.getEmail(), saved.getPrenom());
        log.info("Created new user: {} with role: {}", saved.getEmail(), saved.getRole());

        return userMapper.toResponse(saved);
    }

    private User buildUserByRole(CreateUserRequest request, String hashedPassword) {
        return switch (request.role()) {
            case ADHERENT -> Adherent.builder()
                    .email(request.email())
                    .motDePasse(hashedPassword)
                    .nom(request.nom())
                    .prenom(request.prenom())
                    .role(Role.ADHERENT)
                    .actif(true)
                    .build();

            case MEMBRE_BUREAU -> MembreBureau.builder()
                    .email(request.email())
                    .motDePasse(hashedPassword)
                    .nom(request.nom())
                    .prenom(request.prenom())
                    .poste(request.posteMembre())
                    .role(Role.MEMBRE_BUREAU)
                    .actif(true)
                    .build();

            case ADMIN -> Admin.builder()
                    .email(request.email())
                    .motDePasse(hashedPassword)
                    .nom(request.nom())
                    .prenom(request.prenom())
                    .role(Role.ADMIN)
                    .actif(true)
                    .build();
        };
    }

    @Transactional
    public User updateUser(Long id, UpdateProfilRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé avec ID: " + id));

        if (request.nom() != null) user.setNom(request.nom());
        if (request.prenom() != null) user.setPrenom(request.prenom());

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé avec ID: " + id));

        userRepository.delete(user);
        log.info("Deleted user with ID: {}", id);
    }

    @Transactional
    public User assignRole(Long id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé avec ID: " + id));

        user.setRole(role);
        log.info("Changed role for user {} to {}", user.getEmail(), role);
        return userRepository.save(user);
    }

    @Transactional
    public User toggleUserStatus(Long id, boolean actif) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé avec ID: " + id));

        user.setActif(actif);
        log.info("{} user: {}", actif ? "Activated" : "Deactivated", user.getEmail());
        return userRepository.save(user);
    }

    @Transactional
    public void adminResetPassword(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé avec ID: " + id));

        String tempPassword = generateTemporaryPassword();
        user.setMotDePasse(passwordEncoder.encode(tempPassword));
        user.setFirstLogin(true);
        userRepository.save(user);

        try {
            emailService.sendPasswordResetEmail(user.getEmail(), tempPassword);
            log.info("Password reset completed for userId={}", id);
        } catch (MailException ex) {
            log.error("Password reset email failed for userId={}", id, ex);
            throw new ServiceException("Réinitialisation effectuée mais email non envoyé. Veuillez réessayer.");
        }
    }

    private String generateTemporaryPassword() {
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append(PASSWORD_CHARS.charAt(SECURE_RANDOM.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword()))
            throw new BadRequestException("Les mots de passe ne correspondent pas");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        user.setMotDePasse(passwordEncoder.encode(request.newPassword()));
        user.setFirstLogin(false);
        userRepository.save(user);
    }
}