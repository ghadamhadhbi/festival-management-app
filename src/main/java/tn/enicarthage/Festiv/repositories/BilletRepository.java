package tn.enicarthage.Festiv.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.enicarthage.Festiv.entities.Billet;

import java.util.List;
import java.util.Optional;

@Repository
public interface BilletRepository extends JpaRepository<Billet, Long> {
    List<Billet> findBySpectacleIdSpec(Long idSpec);
    List<Billet> findByPerformanceIdPerformance(Long idPerformance);
    List<Billet> findByCategorie(String categorie);
    List<Billet> findByVendu(String vendu);
    List<Billet> findByReservationIdReservation(Long idReservation);
    Optional<Billet> findByCodeBillet(String codeBillet);
    
    @Query("SELECT COUNT(b) FROM Billet b WHERE b.performance.idPerformance = :performanceId AND b.categorie = :categorie AND b.vendu = 'Non'")
    Long countAvailableByPerformanceAndCategorie(Long performanceId, String categorie);
}