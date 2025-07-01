package tn.enicarthage.Festiv.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "BOOKMARK")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bookmark {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    @Column(name = "ID")
    private Long id;
    
    @Column(name = "USER_ID", nullable = false)
    private Long userId;
    
    @ManyToOne
    @JoinColumn(name = "SPECTACLE_ID", referencedColumnName = "IDSPEC", nullable = false)
    private Spectacle spectacle;
    
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}