package com.example.festiv.Models;


import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class SpectaclePrices {

    @SerializedName("prixGold")
    private BigDecimal prixGold;

    @SerializedName("prixSilver")
    private BigDecimal prixSilver;

    @SerializedName("prixNormal")
    private BigDecimal prixNormal;

    // Getters et setters
    public BigDecimal getPrixGold() {
        return prixGold;
    }

    public void setPrixGold(BigDecimal prixGold) {
        this.prixGold = prixGold;
    }

    public BigDecimal getPrixSilver() {
        return prixSilver;
    }

    public void setPrixSilver(BigDecimal prixSilver) {
        this.prixSilver = prixSilver;
    }

    public BigDecimal getPrixNormal() {
        return prixNormal;
    }

    public void setPrixNormal(BigDecimal prixNormal) {
        this.prixNormal = prixNormal;
    }
}