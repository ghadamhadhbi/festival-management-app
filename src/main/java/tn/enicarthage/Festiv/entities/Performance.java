
package tn.enicarthage.Festiv.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "PERFORMANCE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Performance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    @Column(name = "IDPERFORMANCE")
    private Long idPerformance;
     
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "IDSPEC", nullable = false)
    @JsonBackReference(value = "spectacle-performance")
    private Spectacle spectacle;
     
    @ManyToOne
    @JoinColumn(name = "IDLIEU", nullable = false)
    @JsonBackReference(value = "lieu-performance")
    private Lieu lieu;
     
    @Column(name = "DATE_PERFORMANCE", nullable = false)
    private LocalDate datePerformance;
     
    @Column(name = "HEURE_DEBUT", nullable = false)
    private LocalTime heureDebut;
     
    @Column(name = "STATUT", length = 20)
    private String statut = "Planifiée"; // Planifiée, En cours, Terminée, Annulée
     
    // Capacity per category for this specific performance
    @Column(name = "CAPACITE_GOLD")
    private Integer capaciteGold;
     
    @Column(name = "CAPACITE_SILVER")
    private Integer capaciteSilver;
     
    @Column(name = "CAPACITE_NORMAL")
    private Integer capaciteNormal;
     
    @OneToMany(mappedBy = "performance")
    private List<Billet> billets;
     
    @OneToMany(mappedBy = "performance", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "performance-reservations") 
    private List<Reservation> reservations;    
}
