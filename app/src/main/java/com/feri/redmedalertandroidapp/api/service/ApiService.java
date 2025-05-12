package com.feri.redmedalertandroidapp.api.service;


import com.feri.redmedalertandroidapp.api.dto.SensorDataDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import java.util.List;
import java.util.Map;

public interface ApiService {
    @GET("api/test-connection")
    Call<String> testConnection();

    @POST("api/sensor-data")
    Call<SensorDataDTO> uploadSensorData(@Body SensorDataDTO sensorData);

    @POST("api/sensor-data/batch")
    Call<List<SensorDataDTO>> uploadBatchSensorData(@Body List<SensorDataDTO> sensorDataList);
}