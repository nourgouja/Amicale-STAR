package tn.star.Pfe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.star.Pfe.entity.Pole;
import tn.star.Pfe.enums.TypeOffre;

import java.util.List;
import java.util.Optional;

public interface PoleRepository extends JpaRepository<Pole, Long> {
    Optional<Pole> findByTypeOffre(TypeOffre typeOffre);
    List<Pole> findAll();
}
