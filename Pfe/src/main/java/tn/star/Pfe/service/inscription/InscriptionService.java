package tn.star.Pfe.service.inscription;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import tn.star.Pfe.dto.inscription.InscriptionResponse;
import tn.star.Pfe.entity.Adherent;
import tn.star.Pfe.entity.Echeance;
import tn.star.Pfe.entity.Inscription;
import tn.star.Pfe.entity.Offre;
import tn.star.Pfe.enums.StatutInscription;
import tn.star.Pfe.enums.StatutOffre;
import tn.star.Pfe.enums.StatutPaiement;
import tn.star.Pfe.enums.TypeOffre;
import tn.star.Pfe.event.InscriptionStatusChangedEvent;
import tn.star.Pfe.exceptions.*;
import tn.star.Pfe.mapper.InscriptionMapper;
import tn.star.Pfe.repository.EcheanceRepository;
import tn.star.Pfe.repository.InscriptionRepository;
import tn.star.Pfe.repository.OffreRepository;
import tn.star.Pfe.service.echeance.EcheanceFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InscriptionService implements IInscriptionService {

    private final InscriptionRepository inscriptionRepository;
    private final OffreRepository offreRepository;
    private final InscriptionMapper inscriptionMapper;
    private final EcheanceRepository echeanceRepository;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public InscriptionResponse inscrire(Long offreId, Adherent adherent) {
        Offre offre = offreRepository.findById(offreId)
                .orElseThrow(() -> new NotFoundException("Offre non trouvée"));

        if (offre.getType() == TypeOffre.CONVENTION)
            throw new BadRequestException("Les conventions sont des partenariats publiés — aucune inscription possible.");

        if (offre.getStatut() != StatutOffre.OUVERTE)
            throw new OffreFermee("L'offre n'est pas ouverte");

        if (offre.getDateFin() != null && offre.getDateFin().isBefore(LocalDate.now()))
            throw new BadRequestException("L'offre est expirée");

        if (inscriptionRepository.existsByOffreAndAdherent(offre, adherent))
            throw new InscriptionExistants("Déjà inscrit à cette offre");

        if (offre.getPlacesRestantes() <= 0)
            throw new CapaciteMaxAtteint("Plus de places disponibles");

        Inscription ins = Inscription.builder()
                .offre(offre)
                .adherent(adherent)
                .montant(offre.getPrixParPersonne())
                .build();

        Inscription saved = inscriptionRepository.save(ins);

        if (offre.getType() == TypeOffre.VOYAGE && offre.getModePaiement() != null) {
            List<Echeance> echeances = EcheanceFactory.generate(
                    saved, offre.getPrixParPersonne(), offre.getModePaiement()
            );
            echeanceRepository.saveAll(echeances);
        }

        return inscriptionMapper.toResponse(saved);
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

    @Transactional
    public InscriptionResponse mettreAjourPaiement(Long inscriptionId, StatutPaiement statut) {
        Inscription inscription = inscriptionRepository.findById(inscriptionId)
                .orElseThrow(() -> new NotFoundException("Inscription non trouvée"));

        inscription.setStatutPaiement(statut);
        inscription.setDatePaiement(LocalDateTime.now());

        return inscriptionMapper.toResponse(inscriptionRepository.save(inscription));
    }
}
