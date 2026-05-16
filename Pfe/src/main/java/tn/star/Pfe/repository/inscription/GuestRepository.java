package tn.star.Pfe.repository.inscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.star.Pfe.entity.inscription.Guest;

import java.util.List;

@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {
    List<Guest> findByInscriptionId(Long inscriptionId);
}
