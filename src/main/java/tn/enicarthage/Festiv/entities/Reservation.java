package tn.enicarthage.Festiv.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "RESERVATION")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    @Column(name = "IDRESERVATION")
    private Long idReservation;
    
    @ManyToOne
    @JoinColumn(name = "IDCLT", nullable = true)
    @JsonBackReference(value = "client-reservations")
    private Client client;
    
    @ManyToOne
    @JoinColumn(name = "IDPERFORMANCE", nullable = false)     
    @JsonBackReference(value = "performance-reservations")
    private Performance performance;
    
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "reservation-billets")
    private List<Billet> billets;
    
    @Column(name = "DATE_RESERVATION", nullable = false)
    private LocalDateTime dateReservation = LocalDateTime.now();
    
    @Column(name = "STATUT", length = 20)
    private String statut = "Confirmée"; // Confirmée, Annulée, Remboursée
         
    @Column(name = "MONTANT_TOTAL", nullable = false)
    private BigDecimal montantTotal;
    
    @Column(name = "IS_GUEST")
    private Boolean isGuest ;

    @Column(name = "GUEST_NAME")
    private String guestName;

    @Column(name = "GUEST_EMAIL")
    private String guestEmail;

    @Column(name = "GUEST_PHONE")
    private String guestPhone;

    
 
    
}