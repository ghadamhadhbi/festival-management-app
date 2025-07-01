package com.example.festiv.Models;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Performance implements Serializable {

    private static final long serialVersionUID = 1L;

    @SerializedName("idPerformance")
    private Long idPerformance;

    @SerializedName("spectacle")
    private Spectacle spectacle;

    @SerializedName("lieu")
    private Lieu lieu;

    @SerializedName("dateHeureDebut")
    private String dateHeureDebut; // Kept for backward compatibility

    @SerializedName("datePerformance")
    private String datePerformance; // New field for date only

    @SerializedName("heureDebut")
    private String heureDebut; // New field for time only

    @SerializedName("statut")
    private String statut;

    @SerializedName("capaciteGold")
    private Integer capaciteGold;

    @SerializedName("capaciteSilver")
    private Integer capaciteSilver;

    @SerializedName("capaciteNormal")
    private Integer capaciteNormal;

    // Getters and Setters
    public Long getIdPerformance() {
        return idPerformance;
    }

    public void setIdPerformance(Long idPerformance) {
        this.idPerformance = idPerformance;
    }

    public Spectacle getSpectacle() {
        return spectacle;
    }

    public void setSpectacle(Spectacle spectacle) {
        this.spectacle = spectacle;
    }

    public Lieu getLieu() {
        return lieu;
    }

    public void setLieu(Lieu lieu) {
        this.lieu = lieu;
    }

    public String getDateHeureDebut() {
        // If dateHeureDebut is directly set, return it
        if (dateHeureDebut != null && !dateHeureDebut.isEmpty()) {
            return dateHeureDebut;
        }
        // Otherwise, try to construct it from separate date and time fields
        else if (datePerformance != null && heureDebut != null) {
            return datePerformance + "T" + heureDebut;
        }
        return null;
    }

    public void setDateHeureDebut(String dateHeureDebut) {
        this.dateHeureDebut = dateHeureDebut;

        // Also parse and set the separate date and time fields
        if (dateHeureDebut != null && !dateHeureDebut.isEmpty()) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date date = inputFormat.parse(dateHeureDebut);

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

                this.datePerformance = dateFormat.format(date);
                this.heureDebut = timeFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public String getDatePerformance() {
        // If datePerformance is directly set, return it
        if (datePerformance != null && !datePerformance.isEmpty()) {
            return datePerformance;
        }
        // Otherwise, try to extract it from dateHeureDebut
        else if (dateHeureDebut != null && !dateHeureDebut.isEmpty()) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date date = inputFormat.parse(dateHeureDebut);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                return dateFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void setDatePerformance(String datePerformance) {
        this.datePerformance = datePerformance;
        updateDateHeureDebut();
    }

    public String getHeureDebut() {
        // If heureDebut is directly set, return it
        if (heureDebut != null && !heureDebut.isEmpty()) {
            return heureDebut;
        }
        // Otherwise, try to extract it from dateHeureDebut
        else if (dateHeureDebut != null && !dateHeureDebut.isEmpty()) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date date = inputFormat.parse(dateHeureDebut);
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                return timeFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void setHeureDebut(String heureDebut) {
        this.heureDebut = heureDebut;
        updateDateHeureDebut();
    }

    // Helper method to update dateHeureDebut when separate fields change
    private void updateDateHeureDebut() {
        if (datePerformance != null && !datePerformance.isEmpty() &&
                heureDebut != null && !heureDebut.isEmpty()) {
            this.dateHeureDebut = datePerformance + "T" + heureDebut;
        }
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Integer getCapaciteGold() {
        return capaciteGold;
    }

    public void setCapaciteGold(Integer capaciteGold) {
        this.capaciteGold = capaciteGold;
    }

    public Integer getCapaciteSilver() {
        return capaciteSilver;
    }

    public void setCapaciteSilver(Integer capaciteSilver) {
        this.capaciteSilver = capaciteSilver;
    }

    public Integer getCapaciteNormal() {
        return capaciteNormal;
    }

    public void setCapaciteNormal(Integer capaciteNormal) {
        this.capaciteNormal = capaciteNormal;
    }

    // Helper methods for formatted dates using SimpleDateFormat
    public String getFormattedDate() {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(getDateHeureDebut());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    // In Performance.java
    public String getFormattedTime() {
        // First try using the dedicated time field
        if (heureDebut != null && !heureDebut.isEmpty()) {
            try {
                // Handle just the time portion (HH:mm:ss)
                SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                Date time = inputFormat.parse(heureDebut);
                SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                return outputFormat.format(time);
            } catch (Exception e) {
                Log.e("Performance", "Error formatting time: " + e.getMessage());

                // If there's an error parsing but we have the raw string, use it
                if (heureDebut.length() >= 5) {
                    return heureDebut.substring(0, 5); // Just return HH:mm portion
                }
            }
        }

        // Fallback to extracting time from the full dateHeureDebut
        if (dateHeureDebut != null && !dateHeureDebut.isEmpty()) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date date = inputFormat.parse(dateHeureDebut);
                SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                return outputFormat.format(date);
            } catch (Exception e) {
                Log.e("Performance", "Error extracting time from full date: " + e.getMessage());
            }
        }

        // If we've gotten this far, we couldn't parse the time
        return "--:--";
    }

    public String getFormattedDateTime() {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(getDateHeureDebut());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy 'à' HH:mm", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    // New helper methods specific to separate date and time
    public String getFormattedDateOnly() {
        if (datePerformance != null && !datePerformance.isEmpty()) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = inputFormat.parse(datePerformance);
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                return outputFormat.format(date);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return getFormattedDate();
    }

    public String getFormattedTimeOnly() {
        if (heureDebut != null && !heureDebut.isEmpty()) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                Date time = inputFormat.parse(heureDebut);
                SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                return outputFormat.format(time);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return getFormattedTime();
    }
}