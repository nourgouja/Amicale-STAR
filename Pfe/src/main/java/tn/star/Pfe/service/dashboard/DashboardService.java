package tn.star.Pfe.service.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.star.Pfe.dto.dashboard.*;
import tn.star.Pfe.dto.inscription.InscriptionResponse;
import tn.star.Pfe.dto.offre.OffreResponse;
import tn.star.Pfe.entity.user.Adherent;
import tn.star.Pfe.entity.inscription.Echeance;
import tn.star.Pfe.entity.inscription.Inscription;
import tn.star.Pfe.entity.offre.Offre;
import tn.star.Pfe.enums.Role;
import tn.star.Pfe.enums.StatutOffre;
import tn.star.Pfe.enums.StatutPaiement;
import tn.star.Pfe.exceptions.NotFoundException;
import tn.star.Pfe.mapper.OffreMapper;
import tn.star.Pfe.repository.inscription.EcheanceRepository;
import tn.star.Pfe.repository.inscription.InscriptionRepository;
import tn.star.Pfe.repository.offer.OffreRepository;
import tn.star.Pfe.repository.user.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static tn.star.Pfe.enums.StatutInscription.*;
import static tn.star.Pfe.enums.StatutInscription.EN_ATTENTE;
import static tn.star.Pfe.enums.StatutPaiement.*;

@Service
@RequiredArgsConstructor
public class DashboardService implements IDashboardService {

    private final UserRepository userRepository;
    private final OffreRepository offreRepository;
    private final InscriptionRepository inscriptionRepository;
    private final EcheanceRepository echeanceRepository;
    private final OffreMapper            offreMapper;

    @Override
    public AdminDashboardResponse getAdminDashboard() {

        long totalUtilisateurs = userRepository.count();
        Map<String, Long> parRole = Map.of(
                "ADMIN",         userRepository.countByRole(Role.ADMIN),
                "ADHERENT",      userRepository.countByRole(Role.ADHERENT),
                "MEMBRE_BUREAU", userRepository.countByRole(Role.MEMBRE_BUREAU)
        );

        List<Offre> toutes = offreRepository.findAll();

        List<OffreDashboardItem> offres = toutes.stream()
                .map(o -> new OffreDashboardItem(
                        o.getId(),
                        o.getTitre(),
                        o.getStatut(),
                        o.getPlacesRestantes(),
                        inscriptionRepository.countByOffreAndStatut(o, CONFIRMEE)
                ))
                .toList();

        long totalInscriptions = inscriptionRepository.count();
        long enAttente         = inscriptionRepository.countByStatut(EN_ATTENTE);
        long confirmees        = inscriptionRepository.countByStatut(CONFIRMEE);
        long annulees          = inscriptionRepository.countByStatut(ANNULEE);

        long echeancesEnAttente = echeanceRepository.countByStatut(StatutPaiement.EN_ATTENTE);
        long echeancesEnRetard  = echeanceRepository.countByStatut(EN_RETARD);
        long echeancesPayees    = echeanceRepository.countByStatut(PAYEE);
        BigDecimal totalCollecte = echeanceRepository.sumMontantByStatut(PAYEE);
        BigDecimal totalAttendu  = echeanceRepository.sumMontantByStatut(StatutPaiement.EN_ATTENTE);

        return new AdminDashboardResponse(
                totalUtilisateurs, parRole, offres,
                totalInscriptions, enAttente, confirmees, annulees,
                echeancesEnAttente, echeancesEnRetard, echeancesPayees,
                totalCollecte, totalAttendu
        );
    }

    @Override
    public BureauDashboardResponse getBureauDashboard(String email) {

        List<Offre> toutes = offreRepository.findAll();

        List<OffreDashboardItem> mesOffres = toutes.stream()
                .map(o -> new OffreDashboardItem(
                        o.getId(),
                        o.getTitre(),
                        o.getStatut(),
                        o.getPlacesRestantes(),
                        inscriptionRepository.countByOffreAndStatut(o, CONFIRMEE)
                ))
                .toList();

        long totalEnAttente = inscriptionRepository.countByStatut(EN_ATTENTE);

        List<InscriptionResponse> inscriptionsEnAttente = inscriptionRepository
                .findByStatut(EN_ATTENTE)
                .stream()
                .map(i -> new InscriptionResponse(
                        i.getId(),
                        i.getOffre().getTitre(),
                        i.getAdherent().getEmail(),
                        i.getStatut()))
                .toList();

        long totalPaiementsEnRetard = echeanceRepository.countByStatut(EN_RETARD);

        List<ParticipationItem> participation = toutes.stream()
                .map(o -> new ParticipationItem(
                        o.getTitre(),
                        inscriptionRepository.countByOffreAndStatut(o, EN_ATTENTE),
                        inscriptionRepository.countByOffreAndStatut(o, CONFIRMEE)
                ))
                .toList();

        return new BureauDashboardResponse(
                mesOffres, totalEnAttente, inscriptionsEnAttente,
                totalPaiementsEnRetard, participation
        );
    }

    @Override
    public AdherentDashboardResponse getAdherentDashboard(Long adherentId) {
        var user = userRepository.findById(adherentId)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable : " + adherentId));

        if (!(user instanceof Adherent adherent))
            throw new IllegalStateException("L'utilisateur n'est pas un adhérent");

        List<Inscription> mesInscriptions = inscriptionRepository.findByAdherent(adherent);

        long confirmees = mesInscriptions.stream().filter(i -> i.getStatut() == CONFIRMEE).count();
        long enAttente  = mesInscriptions.stream().filter(i -> i.getStatut() == EN_ATTENTE).count();
        long annulees   = mesInscriptions.stream().filter(i -> i.getStatut() == ANNULEE).count();

        List<Echeance> mesEcheances = echeanceRepository.findByInscription_Adherent(adherent);

        long ecAttente = mesEcheances.stream().filter(e -> e.getStatut() == StatutPaiement.EN_ATTENTE).count();
        long ecRetard  = mesEcheances.stream().filter(e -> e.getStatut() == EN_RETARD).count();
        BigDecimal duTotal = mesEcheances.stream()
                .filter(e -> e.getStatut() != PAYEE)
                .map(Echeance::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<InscriptionSummary> prochainsEvenements = mesInscriptions.stream()
                .filter(i -> i.getStatut() == CONFIRMEE
                        && i.getOffre().getDateDebut() != null
                        && i.getOffre().getDateDebut().isAfter(LocalDate.now()))
                .sorted(Comparator.comparing(i -> i.getOffre().getDateDebut()))
                .limit(3)
                .map(i -> new InscriptionSummary(
                        i.getId(),
                        i.getOffre().getTitre(),
                        i.getOffre().getType(),
                        i.getOffre().getDateDebut(),
                        i.getStatut()))
                .toList();

        List<EcheanceSummary> prochainesEcheances = mesEcheances.stream()
                .filter(e -> e.getStatut() != PAYEE)
                .sorted(Comparator.comparing(Echeance::getDateEcheance))
                .limit(3)
                .map(e -> new EcheanceSummary(
                        e.getId(),
                        e.getInscription().getOffre().getTitre(),
                        e.getMontant(),
                        e.getDateEcheance(),
                        e.getStatut(),
                        e.getNumero()))
                .toList();

        //Hedhi important barcha tansech taawed tchoufha
        List<OffreResponse> offresDisponibles = offreRepository.findByStatut(StatutOffre.OUVERTE)
                .stream()
                .filter(o -> !inscriptionRepository.existsByOffreAndAdherentAndStatutNotIn(
                        o, adherent, List.of(ANNULEE, REJETEE)))
                .map(offreMapper::toResponse)
                .limit(6)
                .toList();

        return new AdherentDashboardResponse(
                mesInscriptions.size(), confirmees, enAttente, annulees,
                ecAttente, ecRetard, duTotal,
                offresDisponibles, prochainsEvenements, prochainesEcheances
        );
    }
}
