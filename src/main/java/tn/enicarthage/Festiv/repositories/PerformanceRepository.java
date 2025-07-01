package tn.enicarthage.Festiv.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.enicarthage.Festiv.entities.Performance;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface PerformanceRepository extends JpaRepository<Performance, Long> {

    List<Performance> findBySpectacleIdSpec(Long spectacleId);

    List<Performance> findByLieuIdLieu(Long lieuId);

    @Query("SELECT p FROM Performance p WHERE (p.datePerformance > :currentDate) OR " +
           "(p.datePerformance = :currentDate AND p.heureDebut > :currentTime)")
    List<Performance> findUpcomingPerformances(@Param("currentDate") LocalDate currentDate,
                                              @Param("currentTime") LocalTime currentTime);
                                              
    @Query("SELECT p FROM Performance p WHERE p.spectacle.idSpec = :spectacleId AND " +
           "((p.datePerformance > :currentDate) OR " +
           "(p.datePerformance = :currentDate AND p.heureDebut > :currentTime))")
    List<Performance> findUpcomingPerformancesBySpectacle(@Param("spectacleId") Long spectacleId,
                                                         @Param("currentDate") LocalDate currentDate,
                                                         @Param("currentTime") LocalTime currentTime);
    
    List<Performance> findByDatePerformanceBetween(LocalDate startDate, LocalDate endDate);

}