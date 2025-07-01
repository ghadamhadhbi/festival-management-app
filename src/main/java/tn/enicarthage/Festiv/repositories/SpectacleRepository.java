package tn.enicarthage.Festiv.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.enicarthage.Festiv.entities.Spectacle;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SpectacleRepository extends JpaRepository<Spectacle, Long> {
    List<Spectacle> findByTitreContainingIgnoreCase(String titre);
    
    @Query("SELECT s FROM Spectacle s JOIN s.performances p WHERE p.lieu.idLieu = :lieuId")
    List<Spectacle> findByLieuId(Long lieuId);
    
    @Query("SELECT s FROM Spectacle s WHERE s.prixNormal BETWEEN :minPrice AND :maxPrice")
    List<Spectacle> findByPriceRange(Double minPrice, Double maxPrice);

	List<Spectacle> findDistinctByPerformances_DatePerformanceBetween(LocalDate startDate, LocalDate endDate);
}