package com.feri.redmedalertandroidapp.auth.model;

import com.feri.redmedalertandroidapp.api.dto.UserDTO;

public class AuthenticationResponse {

    private String token;
    private UserDTO user;

    //constructorul fara parametrii
    public AuthenticationResponse() {
    }

    //getter + setter
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }
}
