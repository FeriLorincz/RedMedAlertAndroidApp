package com.feri.redmedalertandroidapp.auth.model;

public class PasswordResetDTO {

    private String token;
    private String newPassword;

    // Constructor cu parametri
    public PasswordResetDTO(String token, String newPassword) {
        this.token = token;
        this.newPassword = newPassword;
    }

    // Constructor gol
    public PasswordResetDTO() {
    }

    //getter + setter
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
