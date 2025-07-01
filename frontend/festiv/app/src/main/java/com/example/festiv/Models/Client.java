package com.example.festiv.Models;

import java.io.Serializable;

public class Client implements Serializable {
    private Long idclt;
    private String nomclt;
    private String prenomclt;
    private String tel;
    private String email;
    private String motp;

    public Client() {
    }


    public Client(String nomclt, String prenomclt, String tel, String email, String motp) {
        this.nomclt = nomclt;
        this.prenomclt = prenomclt;
        this.tel = tel;
        this.email = email;
        this.motp = motp;
    }


    public Client(Long idclt, String nomclt, String prenomclt, String tel, String email, String motp) {
        this.idclt = idclt;
        this.nomclt = nomclt;
        this.prenomclt = prenomclt;
        this.tel = tel;
        this.email = email;
        this.motp = motp;
    }

    // Getters and Setters
    public Long getIdclt() {
        return idclt;
    }

    public void setIdclt(Long idclt) {
        this.idclt = idclt;
    }

    public String getNomclt() {
        return nomclt;
    }

    public void setNomclt(String nomclt) {
        this.nomclt = nomclt;
    }

    public String getPrenomclt() {
        return prenomclt;
    }

    public void setPrenomclt(String prenomclt) {
        this.prenomclt = prenomclt;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

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

    @Override
    public String toString() {
        return "Client{" +
                "idclt=" + idclt +
                ", nomclt='" + nomclt + '\'' +
                ", prenomclt='" + prenomclt + '\'' +
                ", tel='" + tel + '\'' +
                ", email='" + email + '\'' +
                ", motp='" + motp + '\'' +
                '}';
    }
}
