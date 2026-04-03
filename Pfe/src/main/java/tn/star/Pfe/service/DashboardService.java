package tn.star.Pfe.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.star.Pfe.dto.dashboard.*;
import tn.star.Pfe.dto.inscription.InscriptionResponse;
import tn.star.Pfe.enums.StatutInscription;
import tn.star.Pfe.enums.StatutOffre;
import tn.star.Pfe.enums.StatutPaiement;
import tn.star.Pfe.mapper.InscriptionMapper;
import tn.star.Pfe.repository.InscriptionRepository;
import tn.star.Pfe.repository.OffreRepository;
import tn.star.Pfe.repository.UserRepository;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final OffreRepository offreRepository;
    private final InscriptionRepository inscriptionRepository;
    private final InscriptionMapper inscriptionMapper;

    public AdminDashboardResponse getAdminDashboard() {

        long totalUsers = userRepository.count();

        long adherents    = userRepository.findAll().stream()
                .filter(u -> u.getClass().getSimpleName().equals("Adherent")).count();
        long membres      = userRepository.findAll().stream()
                .filter(u -> u.getClass().getSimpleName().equals("MembreBureau")).count();
        long admins       = userRepository.findAll().stream()
                .filter(u -> u.getClass().getSimpleName().equals("Admin")).count();

        Map<String, Long> parRole = Map.of(
                "ADHERENT",adherents,
                "MEMBRE_BUREAU",membres,
                "ADMIN",admins
        );

        Map<String, Long> offres = Map.of(
                "OUVERTE",offreRepository.countByStatut(StatutOffre.OUVERTE),
                "FERMEE",offreRepository.countByStatut(StatutOffre.FERMEE),
                "ANNULEE",offreRepository.countByStatut(StatutOffre.ANNULEE)
        );

        Map<String, Long> inscriptions = Map.of(
                "EN_ATTENTE",inscriptionRepository.countByStatut(StatutInscription.EN_ATTENTE),
                "CONFIRMEE",inscriptionRepository.countByStatut(StatutInscription.CONFIRMEE),
                "ANNULEE",inscriptionRepository.countByStatut(StatutInscription.ANNULEE)
        );

        Map<String, Long> paiements = Map.of(
                "EN_ATTENTE",inscriptionRepository.countByStatutPaiement(StatutPaiement.EN_ATTENTE),
                "VALIDE",inscriptionRepository.countByStatutPaiement(StatutPaiement.VALIDE),
                "REJETE",inscriptionRepository.countByStatutPaiement(StatutPaiement.REJETE)
        );

        return new AdminDashboardResponse(
                totalUsers,
                parRole,
                offres,
                inscriptions,
                paiements
        );
    }

    public BureauDashboardResponse getBureauDashboard() {

        List<OffreDashboardItem> mesOffres = offreRepository.findAll()
                .stream()
                .map(offre -> new OffreDashboardItem(
                        offre.getId(),
                        offre.getTitre(),
                        offre.getStatut(),
                        offre.getPlacesRestantes(),
                        offre.getInscriptions().stream()
                                .filter(i -> i.getStatut() != StatutInscription.ANNULEE)
                                .count()
                ))
                .toList();

        long totalEnAttente = inscriptionRepository
                .countByStatut(StatutInscription.EN_ATTENTE);

        List<InscriptionResponse> inscriptionsEnAttente = inscriptionRepository
                .findByStatut(StatutInscription.EN_ATTENTE)
                .stream()
                .map(inscriptionMapper::toResponse)
                .toList();

        long totalPaiements = inscriptionRepository
                .countByStatutPaiement(StatutPaiement.EN_ATTENTE);

        List<InscriptionResponse> paiementsEnAttente = inscriptionRepository
                .findByStatutPaiement(StatutPaiement.EN_ATTENTE)
                .stream()
                .map(inscriptionMapper::toResponse)
                .toList();

        List<ParticipationItem> participation = offreRepository.findAll()
                .stream()
                .map(offre -> new ParticipationItem(
                        offre.getTitre(),
                        offre.getInscriptions().stream()
                                .filter(i -> i.getStatut() != StatutInscription.ANNULEE)
                                .count(),
                        offre.getInscriptions().stream()
                                .filter(i -> i.getStatut() == StatutInscription.CONFIRMEE)
                                .count()
                ))
                .toList();

        return new BureauDashboardResponse(
                mesOffres,
                totalEnAttente,
                inscriptionsEnAttente,
                totalPaiements,
                paiementsEnAttente,
                participation
        );
    }
}