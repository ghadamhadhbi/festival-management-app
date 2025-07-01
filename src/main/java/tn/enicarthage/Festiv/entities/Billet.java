package tn.enicarthage.Festiv.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "BILLET")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Billet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    @Column(name = "IDBILLET")
    private Long idBillet;
     
    @ManyToOne
    @JoinColumn(name = "IDSPEC", nullable = false)
    private Spectacle spectacle;
     
    @ManyToOne
    @JoinColumn(name = "IDPERFORMANCE", nullable = false)
    private Performance performance;
     
    @Column(name = "CATEGORIE", nullable = false, length = 50)
    private String categorie; // Gold, Silver, or Normal
     
    @Column(name = "PRIX", nullable = false)
    private BigDecimal prix;
     
    @Column(name = "VENDU", length = 10)
    private String vendu = "Non"; // Oui ou Non
     
    @ManyToOne
    @JoinColumn(name = "IDRESERVATION")
    @JsonBackReference(value = "reservation-billets")
    private Reservation reservation;
     
    @Column(name = "CODE_BILLET", length = 50, unique = true)
    private String codeBillet;
}