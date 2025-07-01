package tn.enicarthage.Festiv.entities;

public class LoginRequest {
    private String email;
    private String motp;

    public LoginRequest() {
    }

    public LoginRequest(String email, String motp) {
        this.email = email;
        this.motp = motp;
    }

    // Getters
    public String getEmail() {
        return email;
    }

    public String getMotp() {
        return motp;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMotp(String password) {
        this.motp = password;
    }
}
