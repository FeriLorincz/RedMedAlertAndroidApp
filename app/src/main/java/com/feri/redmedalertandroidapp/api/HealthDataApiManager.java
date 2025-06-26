package com.feri.redmedalertandroidapp.api;

import android.util.Log;
import com.feri.redmedalertandroidapp.api.config.ApiClient;
import com.feri.redmedalertandroidapp.api.service.HealthDataApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.HashMap;
import java.util.Map;

public class HealthDataApiManager {

    private static final String TAG = "HealthDataApiManager";
    private final HealthDataApiService apiService;
    private final String deviceId;
    private final String userId;

    public interface ApiCallback {
        void onSuccess();
        void onError(String message);
    }

    public HealthDataApiManager(String authToken, String deviceId, String userId) {
        // FOLOSEȘTE ApiClient în loc de RetrofitClient!
        this.apiService = RetrofitClient.getClient().create(HealthDataApiService.class);
        this.deviceId = deviceId;
        this.userId = userId;
    }

    public void sendHealthData(Map<String, Double> sensorData, ApiCallback callback) {
        Log.d(TAG, "Pregătim trimiterea datelor pentru device: " + deviceId);
        Log.d(TAG, "Numărul de senzori: " + sensorData.size());

        // Creează Map-ul curat (fără metadata)
        Map<String, Double> cleanSensorData = new HashMap<>();
        for (Map.Entry<String, Double> entry : sensorData.entrySet()) {
            // Excludem metadata tehnică
            if (!entry.getKey().equals("data_source") && !entry.getKey().equals("timestamp")) {
                cleanSensorData.put(entry.getKey(), entry.getValue());
                Log.d(TAG, "  " + entry.getKey() + " = " + entry.getValue());
            }
        }

        // FOLOSEȘTE ENDPOINT-UL CORECT!
        apiService.sendHealthData(deviceId, cleanSensorData).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Health data sent successfully (HTTP " + response.code() + ")");
                    Log.d(TAG, "✅ Server response: " + response.body());
                    callback.onSuccess();
                } else {
                    String error = "HTTP " + response.code() + ": " + response.message();
                    Log.e(TAG, "❌ Error sending health data: " + error);
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error details: " + errorBody);
                            error += " - " + errorBody;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Could not read error body", e);
                    }
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                String error = "Failed to send health data: " + t.getMessage();
                Log.e(TAG, "❌ Connection error: " + error, t);
                callback.onError(error);
            }
        });
    }

    // Metodă pentru test de conectivitate
    public void testConnection(ApiCallback callback) {
        apiService.healthCheck().enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Health check successful");
                    callback.onSuccess();
                } else {
                    callback.onError("Health check failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                callback.onError("Health check failed: " + t.getMessage());
            }
        });
    }
}