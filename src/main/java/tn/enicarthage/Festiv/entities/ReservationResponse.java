package tn.enicarthage.Festiv.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReservationResponse {
    private Long reservationId;
    private Long performanceId;
    private LocalDateTime dateReservation;
    private String statut;
    private BigDecimal montantTotal;
    
    // Getters and setters
    public Long getReservationId() {
        return reservationId;
    }
    
    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }
    
    public Long getPerformanceId() {
        return performanceId;
    }
    
    public void setPerformanceId(Long performanceId) {
        this.performanceId = performanceId;
    }
    
    public LocalDateTime getDateReservation() {
        return dateReservation;
    }
    
    public void setDateReservation(LocalDateTime dateReservation) {
        this.dateReservation = dateReservation;
    }
    
    public String getStatut() {
        return statut;
    }
    
    public void setStatut(String statut) {
        this.statut = statut;
    }
    
    public BigDecimal getMontantTotal() {
        return montantTotal;
    }
    
    public void setMontantTotal(BigDecimal montantTotal) {
        this.montantTotal = montantTotal;
    }
}