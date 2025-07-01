package com.example.festiv.Models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Lieu implements Serializable {
    @SerializedName("idLieu")
    private Long idLieu;

    @SerializedName("nomLieu")
    private String nomLieu;

    @SerializedName("adresse")
    private String adresse;

    @SerializedName("ville")
    private String ville;

    @SerializedName("capacite")
    private Integer capacite;

    @SerializedName("isDeleted")
    private Character isDeleted;

    @SerializedName("performances")
    private List<Performance> performances;

    // Translation fields
    private String originalNomLieu;
    private String originalAdresse;

    // Transient fields
    private transient String fullAddress;
    private transient boolean selected;

    // Getters and Setters
    public Long getIdLieu() {
        return idLieu;
    }

    public void setIdLieu(Long idLieu) {
        this.idLieu = idLieu;
    }

    public String getNomLieu() {
        return nomLieu;
    }

    public void setNomLieu(String nomLieu) {
        this.nomLieu = nomLieu;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public Integer getCapacite() {
        return capacite;
    }

    public void setCapacite(Integer capacite) {
        this.capacite = capacite;
    }

    public Character getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Character isDeleted) {
        this.isDeleted = isDeleted;
    }

    public List<Performance> getPerformances() {
        return performances;
    }

    public void setPerformances(List<Performance> performances) {
        this.performances = performances;
    }

    public String getOriginalNomLieu() {
        return originalNomLieu;
    }

    public void setOriginalNomLieu(String originalNomLieu) {
        this.originalNomLieu = originalNomLieu;
    }

    public String getOriginalAdresse() {
        return originalAdresse;
    }

    public void setOriginalAdresse(String originalAdresse) {
        this.originalAdresse = originalAdresse;
    }

    // Helper methods
    public String getFullAddress() {
        if (fullAddress == null) {
            fullAddress = adresse + ", " + ville;
        }
        return fullAddress;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public String toString() {
        return nomLieu + " (" + ville + ")";
    }
}