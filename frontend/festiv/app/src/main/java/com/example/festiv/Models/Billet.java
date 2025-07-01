package com.example.festiv.Models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.math.BigDecimal;

public class Billet implements Serializable {
    @SerializedName("idBillet")
    private Long idBillet;

    @SerializedName("spectacle")
    private Spectacle spectacle;

    @SerializedName("performance")
    private Performance performance;

    @SerializedName("categorie")
    private String categorie; // Gold, Silver, or Normal

    @SerializedName("prix")
    private BigDecimal prix;

    @SerializedName("vendu")
    private String vendu = "Non"; // Oui ou Non

    @SerializedName("reservation")
    private Reservation reservation;

    @SerializedName("codeBillet")
    private String codeBillet;

    // Getters and Setters
    public Long getIdBillet() {
        return idBillet;
    }

    public void setIdBillet(Long idBillet) {
        this.idBillet = idBillet;
    }

    public Spectacle getSpectacle() {
        return spectacle;
    }

    public void setSpectacle(Spectacle spectacle) {
        this.spectacle = spectacle;
    }

    public Performance getPerformance() {
        return performance;
    }

    public void setPerformance(Performance performance) {
        this.performance = performance;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public BigDecimal getPrix() {
        return prix;
    }

    public void setPrix(BigDecimal prix) {
        this.prix = prix;
    }

    public String getVendu() {
        return vendu;
    }

    public void setVendu(String vendu) {
        this.vendu = vendu;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public String getCodeBillet() {
        return codeBillet;
    }

    public void setCodeBillet(String codeBillet) {
        this.codeBillet = codeBillet;
    }

    // Helper methods
    public boolean isVendu() {
        return "Oui".equalsIgnoreCase(vendu);
    }

    public String getFormattedPrix() {
        return prix != null ? prix + " TND" : "0.00 TND";
    }

    public String getCategorieColor() {
        if (categorie == null) return "#000000";
        switch (categorie.toUpperCase()) {
            case "GOLD": return "#FFD700";
            case "SILVER": return "#C0C0C0";
            case "NORMAL": return "#A9A9A9";
            default: return "#000000";
        }
    }
}