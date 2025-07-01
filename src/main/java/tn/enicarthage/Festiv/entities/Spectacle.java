package tn.enicarthage.Festiv.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "SPECTACLE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Spectacle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    @Column(name = "IDSPEC")
    private Long idSpec;
     
    @Column(name = "TITRE", nullable = false, length = 40)
    private String titre;
     
    @Column(name = "DUREES", nullable = false)
    private Integer dureeMinutes;
     
    // Price fields for each category
    @Column(name = "PRIX_GOLD", nullable = false)
    private BigDecimal prixGold;
     
    @Column(name = "PRIX_SILVER", nullable = false)
    private BigDecimal prixSilver;
     
    @Column(name = "PRIX_NORMAL", nullable = false)
    private BigDecimal prixNormal;
     
    @Column(name = "IMAGE_URL", length = 255)
    private String imageUrl;
    
    @Column(name = "WEBSITE_URL")
    private String websiteUrl;
    
    @Column(name = "DESCRIPTION", length = 500)
    private String description;
     
    // A spectacle can have multiple performances
    @OneToMany(mappedBy = "spectacle", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "spectacle-performance")
    private List<Performance> performances;
}
