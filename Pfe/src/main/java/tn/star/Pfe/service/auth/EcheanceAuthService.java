package tn.star.Pfe.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.star.Pfe.entity.Echeance;
import tn.star.Pfe.entity.Pole;
import tn.star.Pfe.entity.MembreBureau;
import tn.star.Pfe.enums.PosteBureau;
import tn.star.Pfe.repository.EcheanceRepository;
import tn.star.Pfe.repository.UserRepository;
import tn.star.Pfe.security.UserPrincipal;

@Service
@RequiredArgsConstructor
public class EcheanceAuthService implements IEcheanceAuthService {

    private final EcheanceRepository echeanceRepository;
    private final UserRepository userRepository;

    public boolean canValidate(UserPrincipal principal, Long echeanceId) {
        MembreBureau membre = (MembreBureau) userRepository.findById(principal.getId())
                .orElseThrow();

        if (membre.getPoste() == PosteBureau.TRESORIER) return true;

        Echeance echeance = echeanceRepository.findById(echeanceId)
                .orElseThrow();

        Pole offrePole = echeance.getInscription().getOffre().getPole();
        return offrePole != null
                && offrePole.getId().equals(membre.getPole().getId());
    }
}