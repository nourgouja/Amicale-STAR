package tn.star.Pfe.service.adhesion;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.star.Pfe.dto.auth.DemandeAdhesionResponse;
import tn.star.Pfe.dto.auth.DemandeRequest;
import tn.star.Pfe.entity.Adherent;
import tn.star.Pfe.enums.Role;
import tn.star.Pfe.enums.StatutDemande;
import tn.star.Pfe.event.AdhesionDemandeEvent;
import tn.star.Pfe.exceptions.BadRequestException;
import tn.star.Pfe.exceptions.NotFoundException;
import tn.star.Pfe.repository.UserRepository;
import tn.star.Pfe.service.email.IEmailService;
import tn.star.Pfe.service.email.PasswordGenerator;

import java.util.List;

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
                .motDePasse(passwordEncoder.encode(tempPassword))
                .role(Role.ADHERENT)
                .statut(StatutDemande.PENDING)
                .firstLogin(true)
                .actif(false)
                .build();

        Adherent saved = userRepository.save(adherent);
        publisher.publishEvent(new AdhesionDemandeEvent(saved));
    }

    @Override
    public List<DemandeAdhesionResponse> getDemandesEnAttente() {
        return userRepository.findByRole(Role.ADHERENT).stream()
                .filter(u -> u instanceof Adherent)
                .map(u -> (Adherent) u)
                .filter(a -> a.getStatut() == StatutDemande.PENDING)
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
        adherent.setStatut(StatutDemande.APPROVED);
        adherent.setActif(true);
        userRepository.save(adherent);
    }

    @Override
    @Transactional
    public void rejeter(Long id) {
        Adherent adherent = findPendingAdherent(id);
        adherent.setStatut(StatutDemande.REJECTED);
        userRepository.save(adherent);
    }

    private Adherent findPendingAdherent(Long id) {
        return userRepository.findById(id)
                .filter(u -> u instanceof Adherent)
                .map(u -> (Adherent) u)
                .filter(a -> a.getStatut() == StatutDemande.PENDING)
                .orElseThrow(() -> new NotFoundException("Demande introuvable ou déjà traitée."));
    }
}
