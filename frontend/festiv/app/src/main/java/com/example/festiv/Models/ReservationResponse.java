package com.example.festiv.Models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class ReservationResponse implements Serializable {
    private Long id;
    private Long clientId;
    private Performance performance;
    private String status;
    private double montantTotal;
    private String categorie;
    private int nbBillets;
    private Client guestInfo; // For guest reservation info

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Performance getPerformance() {
        return performance;
    }

    public void setPerformance(Performance performance) {
        this.performance = performance;
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

    public Client getGuestInfo() {
        return guestInfo;
    }

    public void setGuestInfo(Client guestInfo) {
        this.guestInfo = guestInfo;
    }

    // Helper method to format the amount with currency
    public String getFormattedAmount() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.getDefault());
        return formatter.format(montantTotal).replace("$", "") + " DT";
    }
}