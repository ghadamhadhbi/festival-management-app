package com.example.festiv.Models;

import com.google.gson.annotations.SerializedName;

public class Bookmark {
    @SerializedName("id")
    private Long id;

    @SerializedName("userId")
    private Long userId;

    @SerializedName("spectacleId")
    private Long spectacleId;

    @SerializedName("spectacle")
    private Spectacle spectacle;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getSpectacleId() {
        return spectacleId;
    }

    public void setSpectacleId(Long spectacleId) {
        this.spectacleId = spectacleId;
    }

    public Spectacle getSpectacle() {
        return spectacle;
    }

    public void setSpectacle(Spectacle spectacle) {
        this.spectacle = spectacle;
    }
}