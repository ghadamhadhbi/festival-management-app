package tn.enicarthage.Festiv.entities;


import java.math.BigDecimal;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequest {
    
	    private Long clientId;
	    private Long performanceId;
	    private String statut;
	    private BigDecimal montantTotal;
	    
	    private String guestName;
	    private String guestEmail;
	    private String guestPhone;
	    private boolean isGuest;
	    
	    
	    public boolean isGuest() {
	        return isGuest;
	    }
	    
	    public String getGuestPhone() {
	        return guestPhone;
	    }
	    
	    public void setGuestPhone(String guestPhone) {
	        this.guestPhone = guestPhone;
	    }
	    
}