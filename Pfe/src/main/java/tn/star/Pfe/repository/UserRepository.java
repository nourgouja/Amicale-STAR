package tn.star.Pfe.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.star.Pfe.entity.Adherent;
import tn.star.Pfe.entity.User;
import tn.star.Pfe.enums.Role;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByActif(boolean actif, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role = :role AND " +
            "(LOWER(u.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.prenom) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> findByRoleAndSearch(@Param("role") Role role,
                                   @Param("search") String search,
                                   Pageable pageable);
    Page<User> findByRole(Role role, Pageable pageable);
    List<User> findByRole(Role role);
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.prenom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> searchByKeyword(@Param("search") String search, Pageable pageable);

    long countByRole(Role role);

    @Query("SELECT a FROM Adherent a WHERE a.statut = 'PENDING'")
    List<Adherent> findPendingAdhesions();
}