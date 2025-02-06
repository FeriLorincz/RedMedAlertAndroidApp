package com.feri.redmedalertandroidapp.auth.service;

import android.content.Context;
import android.util.Log;

import com.feri.redmedalertandroidapp.api.dto.UserDTO;
import com.feri.redmedalertandroidapp.auth.model.AuthenticationRequest;
import com.feri.redmedalertandroidapp.auth.model.AuthenticationResponse;
import com.feri.redmedalertandroidapp.auth.model.PasswordResetRequestDTO;
import com.feri.redmedalertandroidapp.auth.model.PasswordResetDTO;
import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthService {

    private final AuthApiService authApiService;
    private final TokenManager tokenManager;

    public interface AuthCallback {
        void onSuccess(UserDTO user);
        void onError(String message);
    }

    public interface ResetCallback {
        void onSuccess();
        void onError(String message);
    }

    public AuthService(Context context, AuthApiService authApiService) {
        this.authApiService = authApiService;
        this.tokenManager = new TokenManager(context);
    }

    public void register(UserDTO userDTO, AuthCallback callback) {
        // Logging pentru request
        try {
            String jsonBody = new Gson().toJson(userDTO);
            Log.d("AuthService", "Starting registration for: " + userDTO.getEmailUser());
            Log.d("AuthService", "Request body: " + jsonBody);
        } catch (Exception e) {
            Log.e("AuthService", "Error serializing request", e);
        }

        // Efectuăm cererea de înregistrare
        authApiService.register(userDTO).enqueue(new Callback<AuthenticationResponse>() {
            @Override
            public void onResponse(Call<AuthenticationResponse> call, Response<AuthenticationResponse> response) {
                Log.d("AuthService", "Response code: " + response.code());
                Log.d("AuthService", "Request URL: " + call.request().url());

                if (!response.isSuccessful()) {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e("AuthService", "Error body: " + errorBody);
                        callback.onError("Înregistrare eșuată: " + errorBody);
                    } catch (IOException e) {
                        Log.e("AuthService", "Error reading error body", e);
                        callback.onError("Eroare la citirea răspunsului: " + e.getMessage());
                    }
                    return;
                }

                // Dacă răspunsul este successful
                if (response.body() != null) {
                    try {
                        AuthenticationResponse authResponse = response.body();
                        Log.d("AuthService", "Registration successful, saving token");
                        tokenManager.saveToken(authResponse.getToken());

                        if (authResponse.getUser() != null) {
                            tokenManager.saveUserId(authResponse.getUser().getIdUser());
                            Log.d("AuthService", "User data saved successfully");
                        } else {
                            Log.w("AuthService", "User data is null in response");
                        }

                        callback.onSuccess(authResponse.getUser());
                    } catch (Exception e) {
                        Log.e("AuthService", "Error processing successful response", e);
                        callback.onError("Eroare la procesarea răspunsului: " + e.getMessage());
                    }
                } else {
                    Log.e("AuthService", "Response body is null");
                    callback.onError("Răspuns invalid de la server");
                }
            }

            @Override
            public void onFailure(Call<AuthenticationResponse> call, Throwable t) {
                Log.e("AuthService", "Registration network failure", t);
                String errorMessage = t.getMessage();
                if (errorMessage == null) {
                    errorMessage = "Eroare de rețea necunoscută";
                }
                Log.e("AuthService", "Error details: " + errorMessage);
                callback.onError("Eroare de rețea: " + errorMessage);
            }
        });
    }

    public void login(String email, String password, AuthCallback callback) {
        AuthenticationRequest request = new AuthenticationRequest(email, password);
        authApiService.authenticate(request).enqueue(new Callback<AuthenticationResponse>() {
            @Override
            public void onResponse(Call<AuthenticationResponse> call, Response<AuthenticationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthenticationResponse authResponse = response.body();
                    tokenManager.saveToken(authResponse.getToken());
                    tokenManager.saveUserId(authResponse.getUser().getIdUser());
                    callback.onSuccess(authResponse.getUser());
                } else {
                    callback.onError("Autentificare eșuată: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<AuthenticationResponse> call, Throwable t) {
                Log.e("AuthService", "Registration failed", t);
                callback.onError("Eroare de rețea: " + t.getMessage());
            }
        });
    }

    public void requestPasswordReset(String email, ResetCallback callback) {
        PasswordResetRequestDTO request = new PasswordResetRequestDTO(email);
        authApiService.requestPasswordReset(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError("Eroare la cererea de resetare a parolei");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Eroare de rețea: " + t.getMessage());
            }
        });
    }

    public void resetPassword(String token, String newPassword, ResetCallback callback) {
        PasswordResetDTO request = new PasswordResetDTO(token, newPassword);
        authApiService.resetPassword(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError("Eroare la resetarea parolei");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Eroare de rețea: " + t.getMessage());
            }
        });
    }

    public void logout() {
        tokenManager.clearAll();
    }

    public boolean isLoggedIn() {
        return tokenManager.isLoggedIn();
    }

    public String getUserId() {
        return tokenManager.getUserId();
    }
}
