package com.example.festiv.Models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("idReservation")
    private Long idReservation;

    @SerializedName("client")
    private Client client;

    @SerializedName("performance")
    private Performance performance;

    @SerializedName("billets")
    private List<Billet> billets;

    @SerializedName("dateReservation")
    private String dateReservation;

    @SerializedName("statut")
    private String statut;

    @SerializedName("montantTotal")
    private BigDecimal montantTotal;

    @SerializedName("categorie")
    private String categorie; // For direct category access used in ReservationActivity

    @SerializedName("nbBillets")
    private Integer nbBillets; // For direct access to number of tickets

    // Getters and Setters
    public Long getIdReservation() {
        return idReservation;
    }

    public void setIdReservation(Long idReservation) {
        this.idReservation = idReservation;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Performance getPerformance() {
        return performance;
    }

    public void setPerformance(Performance performance) {
        this.performance = performance;
    }

    public List<Billet> getBillets() {
        return billets;
    }

    public void setBillets(List<Billet> billets) {
        this.billets = billets;
        // Update nbBillets when billets are set
        this.nbBillets = billets != null ? billets.size() : 0;

        // Update categorie if possible
        if (billets != null && !billets.isEmpty()) {
            this.categorie = billets.get(0).getCategorie();
        }
    }

    public String getDateReservation() {
        return dateReservation;
    }

    public void setDateReservation(String dateReservation) {
        this.dateReservation = dateReservation;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public BigDecimal getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(BigDecimal montantTotal) {
        this.montantTotal = montantTotal;
    }

    // Added direct access methods for category and number of tickets
    public String getCategorie() {
        // First check if we have the value directly
        if (categorie != null) {
            return categorie;
        }
        // Otherwise try to get from tickets
        if (billets != null && !billets.isEmpty()) {
            return billets.get(0).getCategorie();
        }
        return null;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public Integer getNbBillets() {
        // First check if we have the value directly
        if (nbBillets != null) {
            return nbBillets;
        }
        // Otherwise count tickets
        return billets != null ? billets.size() : 0;
    }

    public void setNbBillets(Integer nbBillets) {
        this.nbBillets = nbBillets;
    }

    // Date formatting methods
    public String getFormattedDate() {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = inputFormat.parse(dateReservation);

            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateReservation; // fallback
        }
    }

    // For backward compatibility with Spectacle reference
    public Spectacle getSpectacle() {
        return performance != null ? performance.getSpectacle() : null;
    }
}