package tn.star.Pfe.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.star.Pfe.dto.dashboard.*;
import tn.star.Pfe.dto.inscription.InscriptionResponse;
import tn.star.Pfe.enums.Role;
import tn.star.Pfe.enums.StatutInscription;
import tn.star.Pfe.enums.StatutOffre;
import tn.star.Pfe.enums.StatutPaiement;
import tn.star.Pfe.mapper.InscriptionMapper;
import tn.star.Pfe.repository.EcheanceRepository;
import tn.star.Pfe.repository.InscriptionRepository;
import tn.star.Pfe.repository.OffreRepository;
import tn.star.Pfe.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final OffreRepository offreRepository;
    private final InscriptionRepository inscriptionRepository;
    private final EcheanceRepository echeanceRepository;
    public AdminDashboardResponse getAdminDashboard() {

        long totalUtilisateurs = userRepository.count();
        Map<String, Long> parRole = Map.of(
                "ADMIN",         userRepository.countByRole(Role.ADMIN),
                "ADHERENT",      userRepository.countByRole(Role.ADHERENT),
                "MEMBRE_BUREAU", userRepository.countByRole(Role.MEMBRE_BUREAU)
        );

        List<OffreDashboardItem> offres = offreRepository.findAll()
                .stream()
                .map(o -> new OffreDashboardItem(
                        o.getId(),
                        o.getTitre(),
                        o.getStatut(),
                        o.getPlacesRestantes(),
                        inscriptionRepository.countByOffreAndStatut(
                                o, StatutInscription.CONFIRMEE)
                ))
                .toList();

        long totalInscriptions    = inscriptionRepository.count();
        long enAttente            = inscriptionRepository.countByStatut(StatutInscription.EN_ATTENTE);
        long confirmees           = inscriptionRepository.countByStatut(StatutInscription.CONFIRMEE);
        long annulees             = inscriptionRepository.countByStatut(StatutInscription.ANNULEE);

        long echeancesEnAttente   = echeanceRepository.countByStatut(StatutPaiement.EN_ATTENTE);
        long echeancesEnRetard    = echeanceRepository.countByStatut(StatutPaiement.EN_RETARD);
        long echeancesPayees      = echeanceRepository.countByStatut(StatutPaiement.PAYEE);
        BigDecimal totalCollecte  = echeanceRepository.sumMontantByStatut(StatutPaiement.PAYEE);
        BigDecimal totalAttendu   = echeanceRepository.sumMontantByStatut(StatutPaiement.EN_ATTENTE);

        return new AdminDashboardResponse(
                totalUtilisateurs,
                parRole,
                offres,
                totalInscriptions,
                enAttente,
                confirmees,
                annulees,
                echeancesEnAttente,
                echeancesEnRetard,
                echeancesPayees,
                totalCollecte,
                totalAttendu
        );
    }

    public BureauDashboardResponse getBureauDashboard() {

        List<OffreDashboardItem> mesOffres = offreRepository.findAll()
                .stream()
                .map(o -> new OffreDashboardItem(
                        o.getId(),
                        o.getTitre(),
                        o.getStatut(),
                        o.getPlacesRestantes(),
                        inscriptionRepository.countByOffreAndStatut(
                                o, StatutInscription.CONFIRMEE)
                ))
                .toList();

        long totalEnAttente = inscriptionRepository
                .countByStatut(StatutInscription.EN_ATTENTE);

        List<InscriptionResponse> inscriptionsEnAttente = inscriptionRepository
                .findByStatut(StatutInscription.EN_ATTENTE)
                .stream()
                .map(i -> new InscriptionResponse(i.getId(), i.getOffre().getTitre(), i.getAdherent().getEmail(), i.getStatut()))
                .toList();

        long totalPaiementsEnRetard = echeanceRepository
                .countByStatut(StatutPaiement.EN_RETARD);

        List<ParticipationItem> participation = offreRepository.findAll()
                .stream()
                .map(o -> new ParticipationItem(
                        o.getTitre(),
                        inscriptionRepository.countByOffreAndStatut(
                                o, StatutInscription.EN_ATTENTE),
                        inscriptionRepository.countByOffreAndStatut(
                                o, StatutInscription.CONFIRMEE)
                ))
                .toList();

        return new BureauDashboardResponse(
                mesOffres,
                totalEnAttente,
                inscriptionsEnAttente,
                totalPaiementsEnRetard,
                participation
        );
    }
}