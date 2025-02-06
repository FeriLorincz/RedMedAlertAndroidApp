package com.feri.redmedalertandroidapp.auth.model;

public class PasswordResetRequestDTO {

    private String email;

    // Constructor cu parametri
    public PasswordResetRequestDTO(String email) {
        this.email = email;
    }

    // Constructor gol
    public PasswordResetRequestDTO() {
    }

    // Getters si Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
