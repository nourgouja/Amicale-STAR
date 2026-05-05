package tn.star.Pfe.service.user;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.star.Pfe.dto.auth.create.UserResponse;
import tn.star.Pfe.dto.auth.create.CreateUserRequest;
import tn.star.Pfe.dto.auth.create.MembreBureauPublicResponse;
import tn.star.Pfe.dto.auth.login.ChangePasswordRequest;
import tn.star.Pfe.dto.auth.update.UpdateProfilRequest;
import tn.star.Pfe.entity.user.*;
import tn.star.Pfe.enums.PosteBureau;
import tn.star.Pfe.enums.Role;
import tn.star.Pfe.enums.TypeOffre;
import tn.star.Pfe.exceptions.BadRequestException;
import tn.star.Pfe.exceptions.NotFoundException;
import tn.star.Pfe.exceptions.ServiceException;
import tn.star.Pfe.mapper.UserMapper;
import tn.star.Pfe.repository.PoleRepository;
import tn.star.Pfe.repository.UserRepository;
import tn.star.Pfe.service.email.IEmailService;
import tn.star.Pfe.service.email.PasswordGenerator;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final IEmailService emailService;
    private final PoleRepository poleRepository;
    private final PasswordGenerator passwordGenerator;
    private final ApplicationEventPublisher publisher;


    @Transactional
    public Page<UserResponse> findAll(Role role, String search, int page, int size, boolean actif) {
        int safeSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, safeSize);

        Page<User> result;
        if (role != null && search != null && !search.isBlank()) {
            result = userRepository.findByActifAndRoleAndSearch(actif, role, search, pageable);
        } else if (role != null) {
            result = userRepository.findByActifAndRole(actif, role, pageable);
        } else if (search != null && !search.isBlank()) {
            result = userRepository.searchByKeywordAndActif(actif, search, pageable);
        } else {
            result = userRepository.findByActif(actif, pageable);
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

        String rawPassword = passwordGenerator.generate();
        String hashedPassword = passwordEncoder.encode(rawPassword);

        User user = buildUserByRole(request, hashedPassword);
        user.setFirstLogin(true);
        User saved = userRepository.save(user);

        emailService.sendAccountCreatedEmail(saved.getEmail(), saved.getPrenom(), rawPassword);
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
                    .telephone(request.telephone())
                    .matriculeStar(request.matriculeStar())
                    .role(Role.ADHERENT)
                    .actif(true)
                    .build();

            case MEMBRE_BUREAU -> {
                Pole pole = null;
                if (request.poleId() != null) {
                    pole = poleRepository.findById(request.poleId())
                            .orElseThrow(() -> new NotFoundException("Pôle introuvable avec ID: " + request.poleId()));
                }

                java.util.Set<TypeOffre> types = new java.util.HashSet<>();
                if (request.typesAutorisees() != null) {
                    for (String t : request.typesAutorisees()) {
                        try {
                            types.add(TypeOffre.valueOf(t));
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                }

                yield MembreBureau.builder()
                        .email(request.email())
                        .motDePasse(hashedPassword)
                        .nom(request.nom())
                        .prenom(request.prenom())
                        .telephone(request.telephone())
                        .matriculeStar(request.matriculeStar())
                        .poste(PosteBureau.valueOf(request.posteMembre()))
                        .pole(pole)
                        .typesAutorisees(types)
                        .role(Role.MEMBRE_BUREAU)
                        .actif(true)
                        .build();
            }

            case ADMIN -> Admin.builder()
                    .email(request.email())
                    .motDePasse(hashedPassword)
                    .nom(request.nom())
                    .prenom(request.prenom())
                    .telephone(request.telephone())
                    .matriculeStar(request.matriculeStar())
                    .role(Role.ADMIN)
                    .actif(true)
                    .build();
        };
    }


    @Transactional
    public User updateUser(Long id, UpdateProfilRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé avec ID: " + id));

        if (request.role() != null && request.role() != user.getRole()) {
            return migrateUserRole(user, request);
        }

        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new BadRequestException("Email déjà utilisé: " + request.email());
            }
        }

        if (request.nom() != null)            user.setNom(request.nom());
        if (request.prenom() != null)         user.setPrenom(request.prenom());
        if (request.email() != null)          user.setEmail(request.email());
        if (request.telephone() != null)      user.setTelephone(request.telephone());
        if (request.matriculeStar() != null)  user.setMatriculeStar(request.matriculeStar());

        if (user instanceof MembreBureau mb) {
            if (request.posteMembre() != null && !request.posteMembre().isBlank()) {
                mb.setPoste(PosteBureau.valueOf(request.posteMembre()));
            }
            if (request.poleId() != null) {
                Pole pole = poleRepository.findById(request.poleId())
                        .orElseThrow(() -> new NotFoundException("Pôle introuvable avec ID: " + request.poleId()));
                mb.setPole(pole);
            } else if (request.posteMembre() != null && !request.posteMembre().equals("RESPONSABLE_POLE")) {
                mb.setPole(null);
            }
            if (request.typesAutorisees() != null) {
                java.util.Set<TypeOffre> types = new java.util.HashSet<>();
                for (String t : request.typesAutorisees()) {
                    try { types.add(TypeOffre.valueOf(t)); } catch (IllegalArgumentException ignored) {}
                }
                mb.setTypesAutorisees(types);
            }
        }

        return userRepository.save(user);
    }

    private User migrateUserRole(User existing, UpdateProfilRequest request) {
        if (existing instanceof Adherent a && !a.getInscriptions().isEmpty()) {
            throw new BadRequestException("Impossible de changer le rôle : l'utilisateur a des inscriptions actives.");
        }

        String email         = request.email()         != null ? request.email()         : existing.getEmail();
        String nom           = request.nom()           != null ? request.nom()           : existing.getNom();
        String prenom        = request.prenom()        != null ? request.prenom()        : existing.getPrenom();
        String telephone     = request.telephone()     != null ? request.telephone()     : existing.getTelephone();
        String matriculeStar = request.matriculeStar() != null ? request.matriculeStar() : existing.getMatriculeStar();

        if (!email.equals(existing.getEmail()) && userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email déjà utilisé: " + email);
        }

        try {
            userRepository.delete(existing);
            userRepository.flush();
        } catch (Exception ex) {
            throw new BadRequestException("Impossible de changer le rôle : l'utilisateur est référencé par d'autres données.");
        }

        User newUser = switch (request.role()) {
            case ADHERENT -> Adherent.builder()
                    .email(email).motDePasse(existing.getMotDePasse()).nom(nom).prenom(prenom)
                    .telephone(telephone).matriculeStar(matriculeStar)
                    .role(Role.ADHERENT).actif(true).firstLogin(existing.isFirstLogin()).build();
            case MEMBRE_BUREAU -> {
                if (request.posteMembre() == null || request.posteMembre().isBlank()) {
                    throw new BadRequestException("Le poste est obligatoire pour un membre du bureau.");
                }
                Pole pole = request.poleId() != null
                        ? poleRepository.findById(request.poleId()).orElse(null) : null;
                yield MembreBureau.builder()
                        .email(email).motDePasse(existing.getMotDePasse()).nom(nom).prenom(prenom)
                        .telephone(telephone).matriculeStar(matriculeStar)
                        .role(Role.MEMBRE_BUREAU).actif(true).firstLogin(existing.isFirstLogin())
                        .poste(PosteBureau.valueOf(request.posteMembre())).pole(pole).build();
            }
            case ADMIN -> Admin.builder()
                    .email(email).motDePasse(existing.getMotDePasse()).nom(nom).prenom(prenom)
                    .telephone(telephone).matriculeStar(matriculeStar)
                    .role(Role.ADMIN).actif(true).firstLogin(existing.isFirstLogin()).build();
        };

        return userRepository.save(newUser);
    }


    @Transactional
    public UserResponse uploadPhoto(Long id, MultipartFile photo) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé avec ID: " + id));
        try {
            user.setPhoto(photo.getBytes());
            user.setPhotoNom(photo.getOriginalFilename());
            user.setPhotoType(photo.getContentType());
        } catch (Exception e) {
            throw new BadRequestException("Erreur lecture image.");
        }
        return userMapper.toResponse(userRepository.save(user));
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

        String tempPassword = passwordGenerator.generate();
        user.setMotDePasse(passwordEncoder.encode(tempPassword));
        user.setFirstLogin(true);
        userRepository.save(user);

        emailService.sendPasswordResetEmail(user.getEmail(), tempPassword);
        log.info("Password reset completed for userId={}", id);
    }


    @Transactional
    public void forgotPasswordByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Aucun compte trouvé avec cet email: " + email));

        String tempPassword = passwordGenerator.generate();
        user.setMotDePasse(passwordEncoder.encode(tempPassword));
        user.setFirstLogin(true);
        userRepository.save(user);

        try {
            emailService.sendPasswordResetEmail(user.getEmail(), tempPassword);
            log.info("Forgot-password email sent to: {}", email);
        } catch (MailException ex) {
            log.error("Failed to send forgot-password email to: {}", email, ex);
            throw new ServiceException("Réinitialisation effectuée mais email non envoyé. Veuillez réessayer.");
        }
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!user.isFirstLogin() && request.getCurrentPassword() != null) {
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getMotDePasse())) {
                throw new BadCredentialsException("Current password is incorrect");
            }
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirmation do not match");
        }

        user.setMotDePasse(passwordEncoder.encode(request.getNewPassword()));
        user.setFirstLogin(false);
        userRepository.save(user);
    }


    public List<MembreBureauPublicResponse> findMembresBureau() {
        return userRepository.findByRole(Role.MEMBRE_BUREAU).stream()
                .filter(u -> u instanceof MembreBureau && u.isActif())
                .map(u -> {
                    MembreBureau mb = (MembreBureau) u;
                    return new MembreBureauPublicResponse(
                            mb.getId(),
                            mb.getNom(),
                            mb.getPrenom(),
                            mb.getPoste() != null ? mb.getPoste().name() : null,
                            mb.getPole() != null ? mb.getPole().getNom() : null
                    );
                })
                .toList();
    }
}
