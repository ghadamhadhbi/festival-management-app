package tn.enicarthage.Festiv.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.enicarthage.Festiv.entities.Lieu;

import java.util.List;

@Repository
public interface LieuRepository extends JpaRepository<Lieu, Long> {
    List<Lieu> findByNomLieuContainingIgnoreCase(String nomLieu);
    List<Lieu> findByVilleContainingIgnoreCase(String ville);
    List<Lieu> findByCapaciteGreaterThanEqual(Integer capacite);
    List<Lieu> findByIsDeletedIsNull();
    List<Lieu> findByIsDeleted(Character isDeleted);
}
