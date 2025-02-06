package com.feri.redmedalertandroidapp.auth.service;

import com.feri.redmedalertandroidapp.api.dto.UserDTO;
import com.feri.redmedalertandroidapp.auth.model.AuthenticationRequest;
import com.feri.redmedalertandroidapp.auth.model.AuthenticationResponse;
import com.feri.redmedalertandroidapp.auth.model.PasswordResetRequestDTO;
import com.feri.redmedalertandroidapp.auth.model.PasswordResetDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {

    @POST("api/auth/register")
    Call<AuthenticationResponse> register(@Body UserDTO request);

    @POST("api/auth/login")
    Call<AuthenticationResponse> authenticate(@Body AuthenticationRequest request);

    @POST("api/auth/forgot-password")
    Call<Void> requestPasswordReset(@Body PasswordResetRequestDTO request);

    @POST("api/auth/reset-password")
    Call<Void> resetPassword(@Body PasswordResetDTO request);
}
