package tn.star.Pfe.service.pole;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.star.Pfe.dto.pole.PoleRequest;
import tn.star.Pfe.dto.pole.PoleResponse;
import tn.star.Pfe.entity.Pole;
import tn.star.Pfe.repository.PoleRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PoleService implements IPoleService {

    private final PoleRepository poleRepository;

    public List<PoleResponse> listerTous() {
        return poleRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PoleResponse creer( PoleRequest request) {
        Pole pole = Pole.builder()
                .nom(request.getNom())
                .typeOffre(request.getTypeOffre())
                .build();
        return toResponse(poleRepository.save(pole));
    }

    private PoleResponse toResponse(Pole pole) {
        return new PoleResponse(
                pole.getId(),
                pole.getNom(),
                pole.getTypeOffre()
        );
    }
}
