package tn.enicarthage.Festiv.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "CLIENT")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    @Column(name = "IDCLT")
    private Long idclt;
     
    @Column(name = "NOMCLT")
    private String nomclt;
     
    @Column(name = "PRENOMCLT")
    private String prenomclt;
     
    @Column(name = "TEL")
    private String tel;
     
    @Column(name = "EMAIL")
    private String email;
     
    @Column(name = "MOTP")
    private String motp;
     
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "client-reservations")
    private List<Reservation> reservations;
    
    
    
}