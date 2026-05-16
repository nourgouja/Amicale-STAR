package tn.star.Pfe.service.inscription;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import tn.star.Pfe.dto.inscription.GuestDTO;
import tn.star.Pfe.dto.inscription.InscriptionResponse;
import tn.star.Pfe.entity.user.Adherent;
import tn.star.Pfe.entity.inscription.Echeance;
import tn.star.Pfe.entity.inscription.Inscription;
import tn.star.Pfe.entity.offre.Offre;
import tn.star.Pfe.enums.PeriodePaiement;
import tn.star.Pfe.enums.StatutInscription;
import tn.star.Pfe.enums.StatutOffre;
import tn.star.Pfe.enums.TypeOffre;
import org.springframework.lang.Nullable;
import tn.star.Pfe.event.InscriptionCreeeEvent;
import tn.star.Pfe.event.InscriptionStatusChangedEvent;
import tn.star.Pfe.exceptions.*;
import tn.star.Pfe.mapper.InscriptionMapper;
import tn.star.Pfe.repository.inscription.InscriptionRepository;
import tn.star.Pfe.repository.offer.OffreRepository;
import tn.star.Pfe.service.echeance.EcheanceFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InscriptionService implements IInscriptionService {

    private final InscriptionRepository inscriptionRepository;
    private final OffreRepository offreRepository;
    private final InscriptionMapper inscriptionMapper;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public InscriptionResponse inscrire(Long offreId, Adherent adherent, List<GuestDTO> guests, PeriodePaiement periodePaiement) {
        Offre offre = offreRepository.findById(offreId)
                .orElseThrow(() -> new NotFoundException("Offre non trouvée"));

        if (offre.getType() == TypeOffre.CONVENTION)
            throw new BadRequestException("Les conventions sont des partenariats publiés — aucune inscription possible.");

        if (offre.getStatut() != StatutOffre.OUVERTE)
            throw new OffreFermee("L'offre n'est pas ouverte");

        if (offre.getDateFin() != null && offre.getDateFin().isBefore(LocalDate.now()))
            throw new BadRequestException("L'offre est expirée");

        if (inscriptionRepository.existsByOffreAndAdherentAndStatutNotIn(
                offre, adherent, List.of(StatutInscription.ANNULEE, StatutInscription.REJETEE)))
            throw new InscriptionExistants("Déjà inscrit à cette offre");

        List<GuestDTO> guestList = guests != null ? guests : new ArrayList<>();
        validateGuests(guestList);

        int nombreAccompagnants = guestList.size();
        int totalPersonnes = 1 + nombreAccompagnants;
        if (offre.getPlacesRestantes() < totalPersonnes)
            throw new CapaciteMaxAtteint("Plus assez de places disponibles (" + offre.getPlacesRestantes() + " restante(s))");

        java.math.BigDecimal montantTotal = offre.getPrixParPersonne() != null
                ? offre.getPrixParPersonne().multiply(java.math.BigDecimal.valueOf(totalPersonnes))
                : null;

        boolean supportsPaymentModes = offre.getType() == TypeOffre.VOYAGE || offre.getType() == TypeOffre.SEJOUR;
        PeriodePaiement periode = supportsPaymentModes
                ? (periodePaiement != null ? periodePaiement : PeriodePaiement.COMPTANT)
                : PeriodePaiement.COMPTANT;

        Inscription ins = Inscription.builder()
                .offre(offre)
                .adherent(adherent)
                .montant(montantTotal)
                .nombreAccompagnants(nombreAccompagnants)
                .guests(guestList)
                .periodePaiement(periode)
                .build();

        if (montantTotal != null) {
            List<Echeance> echeances = EcheanceFactory.generate(ins, montantTotal, periode);
            ins.getEcheances().addAll(echeances);
        }

        Inscription saved = inscriptionRepository.save(ins);
        publisher.publishEvent(new InscriptionCreeeEvent(saved));

        return inscriptionMapper.toResponse(saved);
    }

    private void validateGuests(List<GuestDTO> guests) {
        if (guests == null || guests.isEmpty()) return;
        for (GuestDTO guest : guests) {
            if (guest.getNom() == null || guest.getNom().isBlank())
                throw new BadRequestException("Le nom de l'accompagnant est requis");
            if (guest.getAge() == null || guest.getAge() < 0 || guest.getAge() > 120)
                throw new BadRequestException("L'âge de l'accompagnant est invalide");
            if (guest.getSexe() == null || !List.of("M", "F", "AUTRE").contains(guest.getSexe()))
                throw new BadRequestException("Le sexe de l'accompagnant doit être M, F ou AUTRE");
        }
    }

    public InscriptionResponse getById(Long inscriptionId) {
        return inscriptionRepository.findById(inscriptionId)
                .map(inscriptionMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Inscription introuvable"));
    }

    public List<InscriptionResponse> listerToutesInscriptions() {
        return inscriptionRepository.findAll()
                .stream()
                .map(inscriptionMapper::toResponse)
                .toList();
    }

    public List<InscriptionResponse> listerFiltrees(
            @Nullable StatutInscription statut,
            @Nullable TypeOffre type,
            @Nullable String search) {
        String searchParam = (search != null && !search.isBlank()) ? search.trim() : null;
        return inscriptionRepository.findFiltered(statut, type, searchParam)
                .stream()
                .map(inscriptionMapper::toResponse)
                .toList();
    }

    @Transactional
    public InscriptionResponse annuler(Adherent adherent, Long inscriptionId) {
        Inscription inscription = inscriptionRepository
                .findByIdAndAdherent(inscriptionId, adherent)
                .orElseThrow(() -> new NotFoundException("Inscription introuvable"));

        StatutInscription oldStatut = inscription.getStatut();
        inscription.setStatut(StatutInscription.ANNULEE);
        inscription.setDateAnnulation(LocalDateTime.now());
        Inscription saved = inscriptionRepository.save(inscription);

        publisher.publishEvent(new InscriptionStatusChangedEvent(saved, oldStatut, StatutInscription.ANNULEE));
        return inscriptionMapper.toResponse(saved);
    }

    @Transactional
    public InscriptionResponse confirmer(Long inscriptionId) {
        Inscription inscription = inscriptionRepository.findById(inscriptionId)
                .orElseThrow(() -> new NotFoundException("Inscription introuvable"));

        StatutInscription oldStatut = inscription.getStatut();
        inscription.setStatut(StatutInscription.CONFIRMEE);
        Inscription saved = inscriptionRepository.save(inscription);

        publisher.publishEvent(new InscriptionStatusChangedEvent(saved, oldStatut, StatutInscription.CONFIRMEE));
        return inscriptionMapper.toResponse(saved);
    }

    @Transactional
    public InscriptionResponse refuser(Long inscriptionId) {
        Inscription inscription = inscriptionRepository.findById(inscriptionId)
                .orElseThrow(() -> new NotFoundException("Inscription introuvable"));

        StatutInscription oldStatut = inscription.getStatut();
        inscription.setStatut(StatutInscription.REJETEE);
        Inscription saved = inscriptionRepository.save(inscription);

        publisher.publishEvent(new InscriptionStatusChangedEvent(saved, oldStatut, StatutInscription.REJETEE));
        return inscriptionMapper.toResponse(saved);
    }

    public List<InscriptionResponse> mesInscriptions(Adherent adherent) {
        return inscriptionRepository.findByAdherent(adherent)
                .stream()
                .map(inscriptionMapper::toResponse)
                .toList();
    }

    public List<InscriptionResponse> inscritsParOffre(Long offreId) {
        Offre offre = offreRepository.findById(offreId)
                .orElseThrow(() -> new NotFoundException("Offre introuvable"));

        return inscriptionRepository.findByOffre(offre)
                .stream()
                .map(inscriptionMapper::toResponse)
                .toList();
    }

}
