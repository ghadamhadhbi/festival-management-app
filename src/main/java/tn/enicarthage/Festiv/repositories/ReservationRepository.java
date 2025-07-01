package tn.enicarthage.Festiv.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.enicarthage.Festiv.entities.Reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByClientIdclt(Long idClient);
    List<Reservation> findByPerformanceIdPerformance(Long idPerformance);
    List<Reservation> findByStatut(String statut);
    List<Reservation> findByDateReservationBetween(LocalDateTime start, LocalDateTime end);
    
    
}