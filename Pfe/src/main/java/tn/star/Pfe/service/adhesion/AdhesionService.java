package tn.star.Pfe.service.adhesion;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.star.Pfe.dto.auth.create.DemandeAdhesionResponse;
import tn.star.Pfe.dto.auth.create.DemandeRequest;
import tn.star.Pfe.entity.user.Adherent;
import tn.star.Pfe.enums.Role;
import tn.star.Pfe.enums.StatutDemande;
import tn.star.Pfe.event.AdhesionDemandeEvent;
import tn.star.Pfe.exceptions.BadRequestException;
import tn.star.Pfe.exceptions.NotFoundException;
import tn.star.Pfe.repository.UserRepository;
import tn.star.Pfe.service.email.IEmailService;
import tn.star.Pfe.service.email.PasswordGenerator;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdhesionService implements IAdhesionService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGenerator passwordGenerator;
    private final IEmailService emailService;
    private final ApplicationEventPublisher publisher;


    @Override
    @Transactional
    public void soumettreDemande(DemandeRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Un compte avec cet email existe déjà.");
        }

        String tempPassword = passwordGenerator.generate();

        Adherent adherent = Adherent.builder()
                .nom(request.nom())
                .prenom(request.prenom())
                .email(request.email())
                .telephone(request.telephone())
                .matriculeStar(request.matriculeStar())
                .poste(request.poste())
                .motDePasse(passwordEncoder.encode(tempPassword))
                .role(Role.ADHERENT)
                .statut(StatutDemande.PENDING)
                .firstLogin(true)
                .actif(false)
                .build();

        Adherent saved = userRepository.save(adherent);

        publisher.publishEvent(new AdhesionDemandeEvent(saved));
        emailService.sendAccountCreatedEmail(saved.getEmail(), saved.getPrenom(), tempPassword);
        log.info("Membership request submitted from {} ({})", request.email(), request.matriculeStar());

    }

    @Override
    @Transactional
    public List<DemandeAdhesionResponse> getDemandesEnAttente() {
        return userRepository.findPendingAdhesions().stream()
                .map(a -> new DemandeAdhesionResponse(
                        a.getId(),
                        a.getNom(),
                        a.getPrenom(),
                        a.getEmail(),
                        a.getTelephone(),
                        a.getMatriculeStar(),
                        a.getStatut().name(),
                        a.getCreatedAt()
                ))
                .toList();
    }



    @Override
    @Transactional
    public void approuver(Long id) {
        Adherent adherent = findPendingAdherent(id);

        String tempPassword = passwordGenerator.generate();
        adherent.setMotDePasse(passwordEncoder.encode(tempPassword));


        adherent.setStatut(StatutDemande.APPROVED);
        adherent.setActif(true);
        adherent.setFirstLogin(true);
        userRepository.save(adherent);
        emailService.sendAccountCreatedEmail(adherent.getEmail(), adherent.getPrenom(), tempPassword);
        log.info("Membership request approved for userId={}, credentials sent", id);

    }

    @Override
    @Transactional
    public void rejeter(Long id) {
        Adherent adherent = findPendingAdherent(id);

        adherent.setStatut(StatutDemande.REJECTED);

        userRepository.save(adherent);

        try {
            emailService.sendRejectionEmail(adherent.getEmail(), adherent.getPrenom());
            log.info("Membership request rejected for userId={}, notification sent", id);
        } catch (MailException ex) {
            log.error("Failed to send rejection email for userId={}", id, ex);
            log.warn("Applicant will not be notified of rejection");
        }
    }

    private Adherent findPendingAdherent(Long id) {
        return userRepository.findById(id)
                .filter(u -> u instanceof Adherent)
                .map(u -> (Adherent) u)
                .filter(a -> a.getStatut() == StatutDemande.PENDING)
                .orElseThrow(() -> new NotFoundException("Demande introuvable ou déjà traitée."));
    }
}
