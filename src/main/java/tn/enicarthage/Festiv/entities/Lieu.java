package tn.enicarthage.Festiv.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "LIEU")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lieu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    @Column(name = "IDLIEU")
    private Long idLieu;
     
    @Column(name = "NOMLIEU", nullable = false, length = 30)
    private String nomLieu;
     
    @Column(name = "ADRESSE", nullable = false, length = 100)
    private String adresse;
     
    @Column(name = "VILLE", length = 50)
    private String ville;
     
    @Column(name = "CAPACITE", nullable = false)
    private Integer capacite;
     
    @Column(name = "IS_DELETED", length = 1)
    private Character isDeleted;
            
    @OneToMany(mappedBy = "lieu")
    @JsonManagedReference(value = "lieu-performance")
    private List<Performance> performances;
}