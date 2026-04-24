package tn.star.Pfe.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import tn.star.Pfe.entity.MembreBureau;
import tn.star.Pfe.entity.Pole;
import tn.star.Pfe.entity.User;
import tn.star.Pfe.enums.TypeOffre;
import tn.star.Pfe.repository.OffreRepository;
import tn.star.Pfe.repository.UserRepository;
import tn.star.Pfe.security.UserPrincipal;

@Service("offreAuthService")
@RequiredArgsConstructor
public class OffreAuthService {

    private final UserRepository  userRepository;
    private final OffreRepository offreRepository;

    /** Bureau member can create an offer only if their pole matches the given typeOffre.
     *  Members without a pole (PRESIDENT, TRESORIER, SECRETAIRE) can create any type. */
    public boolean canCreate(UserDetails principal, String typeOffreName) {
        MembreBureau mb = resolveMembre(principal);
        if (mb == null) return false;
        Pole pole = mb.getPole();
        if (pole == null) return true;
        try {
            return pole.getTypeOffre() == TypeOffre.valueOf(typeOffreName);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** Bureau member can manage an offer only if the offer belongs to their pole.
     *  Members without a pole can manage any offer. */
    public boolean canManage(UserDetails principal, Long offreId) {
        MembreBureau mb = resolveMembre(principal);
        if (mb == null) return false;
        Pole pole = mb.getPole();
        if (pole == null) return true;
        return offreRepository.findById(offreId)
                .map(o -> o.getPole() != null && o.getPole().getId().equals(pole.getId()))
                .orElse(false);
    }

    private MembreBureau resolveMembre(UserDetails principal) {
        if (!(principal instanceof UserPrincipal up)) return null;
        User user = userRepository.findById(up.getId()).orElse(null);
        if (!(user instanceof MembreBureau mb)) return null;
        return mb;
    }
}
