package com.feri.redmedalertandroidapp.network;

import com.feri.redmedalertandroidapp.data.model.SensorDataEntity;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface SensorDataApi {
    @POST("api/sensor-data/batch")
    Call<Void> uploadSensorData(@Body List<SensorDataEntity> data);
}
