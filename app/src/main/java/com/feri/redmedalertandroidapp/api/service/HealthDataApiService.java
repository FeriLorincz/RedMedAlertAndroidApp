package com.feri.redmedalertandroidapp.api.service;

import com.feri.redmedalertandroidapp.api.model.HealthDataPayload;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Header;

public interface HealthDataApiService {

    // Pentru datele reale, cu autentificare
    @POST("api/health-data")
    Call<Void> sendHealthData(
            @Header("Authorization") String authToken,
            @Body HealthDataPayload data
    );

    // Pentru testare, fără autentificare
    @POST("api/alerts/test")
    Call<Void> testHealthData(
            @Body HealthDataPayload data
    );
}