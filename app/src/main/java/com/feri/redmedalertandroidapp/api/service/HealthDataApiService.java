package com.feri.redmedalertandroidapp.api.service;

import com.feri.redmedalertandroidapp.api.model.HealthDataPayload;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface HealthDataApiService {

    // FOLOSEȘTE ENDPOINT-UL CORECT DIN SPRING BOOT!
    @POST("api/sensor-data/device/{deviceId}")
    Call<String> sendHealthData(
            @Path("deviceId") String deviceId,
            @Body Map<String, Double> sensorData  // FORMATUL CORECT!
    );


    // Pentru testare, fără autentificare
    @POST("api/devices/health")
    Call<String> healthCheck();
}
