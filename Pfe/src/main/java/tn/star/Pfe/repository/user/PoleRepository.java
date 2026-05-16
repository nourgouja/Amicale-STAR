package tn.star.Pfe.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.star.Pfe.entity.user.Pole;
import tn.star.Pfe.enums.TypeOffre;

import java.util.List;
import java.util.Optional;

public interface PoleRepository extends JpaRepository<Pole, Long> {

    @Query("SELECT p FROM Pole p WHERE :type MEMBER OF p.typesOffre")
    List<Pole> findByTypesOffreContaining(@Param("type") TypeOffre type);

    @Query("SELECT p FROM Pole p WHERE :type MEMBER OF p.typesOffre")
    Optional<Pole> findFirstByTypesOffreContaining(@Param("type") TypeOffre type);

    List<Pole> findAll();
}
