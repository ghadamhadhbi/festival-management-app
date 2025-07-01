package com.example.festiv.Activities;

public class LoginRequest {
    private String email;
    private String motp;  // Change this to "motp" as expected by the backend

    public LoginRequest(String email, String motp) {
        this.email = email;
        this.motp = motp;
    }

    // Getters and setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotp() {
        return motp;
    }

    public void setMotp(String motp) {
        this.motp = motp;
    }
}
