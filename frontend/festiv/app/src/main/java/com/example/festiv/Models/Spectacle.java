package com.example.festiv.Models;

import android.util.Log;
import android.view.translation.Translator;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

public class Spectacle implements Serializable {

    private static final long serialVersionUID = 2L;
    @SerializedName("idSpec")
    private Long idSpec;

    @SerializedName("titre")
    private String titre;

    @SerializedName("dureeMinutes")
    private Integer dureeMinutes;

    @SerializedName("prixGold")
    private BigDecimal prixGold;

    @SerializedName("prixSilver")
    private BigDecimal prixSilver;

    @SerializedName("prixNormal")
    private BigDecimal prixNormal;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("websiteUrl")
    private String websiteUrl;

    @SerializedName("description")
    private String description;

    @SerializedName("performances")
    private List<Performance> performances;

    // Fields to store original text for translation
    private String originalTitle;
    private String originalDescription;
    private String originalLocation;

    private transient boolean bookmarked;

    // Getter and setter for bookmarked status
    public boolean isBookmarked() {
        return bookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }

    // Getters and Setters
    public Long getIdSpec() {
        return idSpec;
    }

    public void setIdSpec(Long idSpec) {
        this.idSpec = idSpec;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public Integer getDureeMinutes() {
        return dureeMinutes;
    }

    public void setDureeMinutes(Integer dureeMinutes) {
        this.dureeMinutes = dureeMinutes;
    }

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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getOriginalDescription() {
        return originalDescription;
    }

    public void setOriginalDescription(String originalDescription) {
        this.originalDescription = originalDescription;
    }

    public String getOriginalLocation() {
        return originalLocation;
    }

    public void setOriginalLocation(String originalLocation) {
        this.originalLocation = originalLocation;
    }

    public List<Performance> getPerformances() {
        return performances;
    }

    public void setPerformances(List<Performance> performances) {
        this.performances = performances;
    }


    public String getFirstPerformanceDateFormatted() {
        if (performances == null || performances.isEmpty()) return "";
        try {
            Performance first = Collections.min(performances, Comparator.comparing(p -> parseDate(p.getDateHeureDebut())));
            return formatDate(first.getDateHeureDebut());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getLastPerformanceDateFormatted() {
        if (performances == null || performances.isEmpty()) return "";
        try {
            Performance last = Collections.max(performances, Comparator.comparing(p -> parseDate(p.getDateHeureDebut())));
            return formatDate(last.getDateHeureDebut());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    // Helpers
    private Date parseDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            return new Date(0); // fallback to epoch
        }
    }

    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            return outputFormat.format(date);
        } catch (ParseException e) {
            return "";
        }
    }


    // Add to Spectacle.java

    public String getPrimaryLocationName() {
        if (performances == null || performances.isEmpty()) {
            return "No location available";
        }

        // Get the first performance's venue
        Performance firstPerformance = performances.get(0);
        if (firstPerformance.getLieu() == null) {
            return "No location available";
        }

        return firstPerformance.getLieu().getNomLieu();
    }

    public String getFormattedVenueInfo() {
        if (performances == null || performances.isEmpty()) {
            return "No venues available";
        }

        // Count unique venues
        Set<String> uniqueVenues = new HashSet<>();
        for (Performance performance : performances) {
            if (performance.getLieu() != null && performance.getLieu().getNomLieu() != null) {
                uniqueVenues.add(performance.getLieu().getNomLieu());
            }
        }

        if (uniqueVenues.isEmpty()) {
            return "No venues available";
        } else if (uniqueVenues.size() == 1) {
            return "At " + uniqueVenues.iterator().next();
        } else {
            return "At " + uniqueVenues.size() + " different venues";
        }
    }

    // Add to Spectacle.java



    // Restore original values for all performances' venues
    public void restoreOriginalVenues() {
        if (performances == null) return;

        for (Performance performance : performances) {
            Lieu lieu = performance.getLieu();
            if (lieu != null && lieu.getOriginalNomLieu() != null) {
                lieu.setNomLieu(lieu.getOriginalNomLieu());
            }
        }
    }

}