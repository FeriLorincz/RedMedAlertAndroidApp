package com.feri.redmedalertandroidapp.api.config;

import com.feri.redmedalertandroidapp.api.model.HealthDataPayload;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface HealthDataApiService {

    @POST("api/health-data/test")
    Call<Void> testHealthData(@Body HealthDataPayload payload);

    @POST("api/health-data")
    Call<Void> sendHealthData(@Body HealthDataPayload payload);
}
