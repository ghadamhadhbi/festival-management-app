package com.example.festiv.Models;

import java.io.Serializable;
import java.math.BigDecimal;

public class ReservationRequest implements Serializable {
    private Long clientId;
    private Long performanceId;
    private String status;
    private double montantTotal;
    private String categorie;
    private int nbBillets;


    // Guest information fields
    private String guestName;
    private String guestEmail;
    private String guestPhone;
    private boolean isGuest;

    public ReservationRequest(){}
    public ReservationRequest(Long clientId, Long performanceId, String status, double montantTotal) {
        this.clientId = clientId;
        this.performanceId = performanceId;
        this.status = status;
        this.montantTotal = montantTotal;
    }



    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getPerformanceId() {
        return performanceId;
    }

    public void setPerformanceId(Long performanceId) {
        this.performanceId = performanceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(double montantTotal) {
        this.montantTotal = montantTotal;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public int getNbBillets() {
        return nbBillets;
    }

    public void setNbBillets(int nbBillets) {
        this.nbBillets = nbBillets;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getGuestEmail() {
        return guestEmail;
    }

    public void setGuestEmail(String guestEmail) {
        this.guestEmail = guestEmail;
    }

    public String getGuestPhone() {
        return guestPhone;
    }

    public void setGuestPhone(String guestPhone) {
        this.guestPhone = guestPhone;
    }

    public boolean isGuest() {
        return isGuest;
    }

    public void setGuest(boolean guest) {
        isGuest = guest;
    }
    public void setGuestInfo(Client client) {
        if (client != null) {
            this.guestName = client.getNomclt() + " " + client.getPrenomclt();
            this.guestEmail = client.getEmail();
            this.guestPhone = client.getTel();
            this.isGuest = true;
        }
    }

}