package com.feri.redmedalertandroidapp.auth.service;

import android.content.Context;
import android.util.Log;

import com.feri.redmedalertandroidapp.api.RetrofitClient;
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
import retrofit2.Retrofit;

import android.os.Handler;
import android.os.Looper;

public class AuthService {

    private static final String TAG = "AuthService";
    private final Context context;
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
        if (context == null) {
            Log.e(TAG, "AuthService inițializat cu context null!");
            throw new IllegalArgumentException("Context cannot be null in AuthService constructor");
        }

        this.context = context.getApplicationContext(); // Folosim applicationContext pentru siguranță
        this.authApiService = authApiService;
        this.tokenManager = new TokenManager(this.context);

        // Asigurăm că RetrofitClient are context
        RetrofitClient.getInstance(this.context);

        Log.d(TAG, "AuthService inițializat cu succes");
    }

    public void register(UserDTO userDTO, AuthCallback callback) {
        // Logging pentru request
        try {
            String jsonBody = new Gson().toJson(userDTO);
            Log.d(TAG, "Starting registration for: " + userDTO.getEmailUser());
            Log.d(TAG, "Request body: " + jsonBody);
        } catch (Exception e) {
            Log.e(TAG, "Error serializing request", e);
        }

        // Efectuăm cererea de înregistrare
        authApiService.register(userDTO).enqueue(new Callback<AuthenticationResponse>() {
            @Override
            public void onResponse(Call<AuthenticationResponse> call, Response<AuthenticationResponse> response) {
                Log.d(TAG, "Response code: " + response.code());
                Log.d(TAG, "Request URL: " + call.request().url());

                if (!response.isSuccessful()) {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Error body: " + errorBody);
                        callback.onError("Înregistrare eșuată: " + errorBody);
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                        callback.onError("Eroare la citirea răspunsului: " + e.getMessage());
                    }
                    return;
                }

                // Dacă răspunsul este successful
                if (response.body() != null) {
                    try {
                        AuthenticationResponse authResponse = response.body();
                        Log.d(TAG, "Registration successful, saving token");
                        tokenManager.saveToken(authResponse.getToken());

                        if (authResponse.getUser() != null) {
                            tokenManager.saveUserId(authResponse.getUser().getIdUser());
                            Log.d(TAG, "User data saved successfully");
                        } else {
                            Log.w(TAG, "User data is null in response");
                        }

                        callback.onSuccess(authResponse.getUser());
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing successful response", e);
                        callback.onError("Eroare la procesarea răspunsului: " + e.getMessage());
                    }
                } else {
                    Log.e(TAG, "Response body is null");
                    callback.onError("Răspuns invalid de la server");
                }
            }

            @Override
            public void onFailure(Call<AuthenticationResponse> call, Throwable t) {
                Log.e(TAG, "Registration network failure", t);
                String errorMessage = t.getMessage();
                if (errorMessage == null) {
                    errorMessage = "Eroare de rețea necunoscută";
                }
                Log.e(TAG, "Error details: " + errorMessage);
                callback.onError("Eroare de rețea: " + errorMessage);
            }
        });
    }

    public void login(String email, String password, AuthCallback callback) {
        Log.d(TAG, "Încercare de autentificare pentru email: " + email);
        Log.d(TAG, "URL curent: " + RetrofitClient.getCurrentUrl());

        try {
            // Verificăm dacă avem serviciul configurat corect
            if (authApiService == null) {
                Log.e(TAG, "AuthApiService este null!");
                callback.onError("Eroare internă: serviciul de autentificare nu este configurat corect");
                return;
            }

            // Verificăm dacă TokenManager are context valid
            if (tokenManager == null) {
                Log.e(TAG, "TokenManager este null!");
                callback.onError("Eroare internă: TokenManager nu este inițializat corect");
                return;
            }

            // Creăm cererea de autentificare
            final AuthenticationRequest request = new AuthenticationRequest(email, password);

            // Verificăm dacă suntem deja în procesul de reîncercare
            if (RetrofitClient.isRetrying()) {
                Log.d(TAG, "Reîncercare în curs, URL curent: " + RetrofitClient.getCurrentUrl());
            }

            // Trimitem cererea de autentificare
            authApiService.authenticate(request).enqueue(new Callback<AuthenticationResponse>() {
                @Override
                public void onResponse(Call<AuthenticationResponse> call, Response<AuthenticationResponse> response) {
                    Log.d(TAG, "Răspuns autentificare: cod=" + response.code());

                    if (response.isSuccessful() && response.body() != null) {
                        // Resetăm contorul de reîncercări la succes
                        RetrofitClient.resetRetryCount();
                        RetrofitClient.setRetrying(false);

                        AuthenticationResponse authResponse = response.body();
                        Log.d(TAG, "Autentificare reușită, salvare token");
                        tokenManager.saveToken(authResponse.getToken());
                        tokenManager.saveUserId(authResponse.getUser().getIdUser());
                        callback.onSuccess(authResponse.getUser());
                    } else {
                        String errorMessage = "Autentificare eșuată";
                        try {
                            if (response.errorBody() != null) {
                                errorMessage += ": " + response.errorBody().string();
                            } else {
                                errorMessage += ": " + response.message();
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Eroare la citirea răspunsului de eroare", e);
                        }
                        Log.e(TAG, errorMessage);
                        callback.onError(errorMessage);
                    }
                }

                @Override
                public void onFailure(Call<AuthenticationResponse> call, Throwable t) {
                    Log.e(TAG, "Eșec autentificare", t);

                    // Verifică dacă eroarea este de conexiune și dacă mai putem face reîncercări
                    if ((t instanceof java.net.ConnectException ||
                            t instanceof java.net.SocketTimeoutException) &&
                            RetrofitClient.canRetry()) {

                        // Incrementăm contorul de reîncercări
                        RetrofitClient.incrementRetryCount();
                        RetrofitClient.setRetrying(true);

                        // Încercăm următorul URL
                        String nextUrl = RetrofitClient.tryNextServerUrl();
                        Log.d(TAG, "Reîncercare autentificare cu URL: " + nextUrl);

                        // Resetăm Retrofit
                        RetrofitClient.resetInstance();

                        // Așteptăm puțin înainte de reîncercare
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            // Reîncercăm autentificarea - folosind instanțierea locală de serviciu
                            // pentru a evita problema cu câmpul final
                            Retrofit retrofit = RetrofitClient.getClient();
                            AuthApiService tempAuthService = retrofit.create(AuthApiService.class);

                            // Reîncercăm autentificarea cu serviciul temporar
                            tempAuthService.authenticate(request).enqueue(new Callback<AuthenticationResponse>() {
                                @Override
                                public void onResponse(Call<AuthenticationResponse> call, Response<AuthenticationResponse> response) {
                                    // Procesăm răspunsul la fel ca mai sus
                                    if (response.isSuccessful() && response.body() != null) {
                                        // Resetăm contorul de reîncercări la succes
                                        RetrofitClient.resetRetryCount();
                                        RetrofitClient.setRetrying(false);

                                        AuthenticationResponse authResponse = response.body();
                                        Log.d(TAG, "Autentificare reușită după reîncercare, salvare token");
                                        tokenManager.saveToken(authResponse.getToken());
                                        tokenManager.saveUserId(authResponse.getUser().getIdUser());
                                        callback.onSuccess(authResponse.getUser());
                                    } else {
                                        String errorMessage = "Autentificare eșuată după reîncercare";
                                        try {
                                            if (response.errorBody() != null) {
                                                errorMessage += ": " + response.errorBody().string();
                                            } else {
                                                errorMessage += ": " + response.message();
                                            }
                                        } catch (IOException e) {
                                            Log.e(TAG, "Eroare la citirea răspunsului de eroare", e);
                                        }
                                        Log.e(TAG, errorMessage);
                                        callback.onError(errorMessage);
                                    }
                                }

                                @Override
                                public void onFailure(Call<AuthenticationResponse> call, Throwable t) {
                                    // Continuăm cu încercările dacă mai avem reîncercări disponibile
                                    if (RetrofitClient.canRetry()) {
                                        // Reapelăm metoda de login pentru următoarea încercare
                                        login(email, password, callback);
                                    } else {
                                        // Resetăm starea pentru viitoarele încercări
                                        RetrofitClient.resetRetryCount();
                                        RetrofitClient.setRetrying(false);
                                        callback.onError("Eroare de rețea după reîncercări: " + t.getMessage());
                                    }
                                }
                            });
                        }, 1000); // Așteptăm 1 secundă

                        return;
                    }

                    callback.onError("Eroare de rețea: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Excepție în timpul autentificării: " + e.getMessage(), e);
            callback.onError("Eroare de autentificare: " + e.getMessage());
        }
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