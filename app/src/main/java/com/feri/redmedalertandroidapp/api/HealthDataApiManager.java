package com.feri.redmedalertandroidapp.api;

import android.util.Log;
import com.feri.redmedalertandroidapp.api.config.ApiClient;
import com.feri.redmedalertandroidapp.api.model.HealthDataPayload;
import com.feri.redmedalertandroidapp.api.service.HealthDataApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.Map;

public class HealthDataApiManager {

    private static final String TAG = "HealthDataApiManager";
    private final HealthDataApiService apiService;
    private final String authToken;
    private final String deviceId;
    private final String userId;

    public interface ApiCallback {
        void onSuccess();
        void onError(String message);
    }

    public HealthDataApiManager(String authToken, String deviceId, String userId) {
        this.apiService = ApiClient.getClient().create(HealthDataApiService.class);
        this.authToken = "Bearer " + authToken;
        this.deviceId = deviceId;
        this.userId = userId;
    }

    public void sendHealthData(Map<String, Double> sensorData, ApiCallback callback) {
        HealthDataPayload payload = new HealthDataPayload(deviceId, userId, sensorData);

        // Folosim endpoint-ul de test pentru ApiTestManager
        apiService.testHealthData(payload).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Health data sent successfully");
                    callback.onSuccess();
                } else {
                    String error = "Error sending health data: " + response.code();
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                String error = "Failed to send health data: " + t.getMessage();
                Log.e(TAG, error, t);
                callback.onError(error);
            }
        });
    }
}
