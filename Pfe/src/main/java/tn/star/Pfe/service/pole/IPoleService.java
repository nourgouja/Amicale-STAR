package tn.star.Pfe.service.pole;

import tn.star.Pfe.dto.pole.PoleRequest;
import tn.star.Pfe.dto.pole.PoleResponse;

import java.util.List;

public interface IPoleService {
    List<PoleResponse> listerTous();
    PoleResponse creer(PoleRequest request);
}
