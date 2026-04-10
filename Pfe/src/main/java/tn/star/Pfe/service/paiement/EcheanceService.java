package tn.star.Pfe.service.paiement;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.star.Pfe.dto.paiement.EcheanceResponse;
import tn.star.Pfe.entity.Echeance;
import tn.star.Pfe.entity.Inscription;
import tn.star.Pfe.enums.StatutPaiement;
import tn.star.Pfe.exceptions.BadRequestException;
import tn.star.Pfe.exceptions.NotFoundException;
import tn.star.Pfe.repository.EcheanceRepository;
import tn.star.Pfe.repository.InscriptionRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EcheanceService {

    private final EcheanceRepository echeanceRepository;
    private final InscriptionRepository inscriptionRepository;

    public List<EcheanceResponse> parInscription(Long inscriptionId) {
        Inscription inscription = inscriptionRepository.findById(inscriptionId)
                .orElseThrow(() -> new NotFoundException("Inscription introuvable"));

        return echeanceRepository.findByInscription(inscription)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public EcheanceResponse marquerPayee(Long echeanceId) {
        Echeance echeance = echeanceRepository.findById(echeanceId)
                .orElseThrow(() -> new NotFoundException("Échéance introuvable"));

        if (echeance.getStatut() == StatutPaiement.PAYEE)
            throw new BadRequestException("Cette échéance est déjà payée.");

        echeance.setStatut(StatutPaiement.PAYEE);
        return toResponse(echeanceRepository.save(echeance));
    }

    @Transactional
    public void marquerEnRetard() {
        List<Echeance> enRetard = echeanceRepository
                .findByStatutAndDateEcheanceBefore(
                        StatutPaiement.EN_ATTENTE,
                        LocalDate.now()
                );

        enRetard.forEach(e -> e.setStatut(StatutPaiement.EN_RETARD));
        echeanceRepository.saveAll(enRetard);

        if (!enRetard.isEmpty())
            log.info("{} échéance(s) marquées EN_RETARD", enRetard.size());
    }

    private EcheanceResponse toResponse(Echeance e) {
        return new EcheanceResponse(
                e.getId(),
                e.getInscription().getId(),
                e.getNumero(),
                e.getMontant(),
                e.getDateEcheance(),
                e.getStatut()
        );
    }
}