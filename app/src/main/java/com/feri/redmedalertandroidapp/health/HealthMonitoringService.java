package com.feri.redmedalertandroidapp.health;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import androidx.annotation.Nullable;

import com.feri.redmedalertandroidapp.api.ApiTestManager;
import com.feri.redmedalertandroidapp.api.HealthDataApiManager;
import com.samsung.android.sdk.healthdata.HealthDataStore;

import java.util.Map;

public class HealthMonitoringService extends Service{

    private static final String TAG = "HealthMonitoringService";
    private HealthDataReader healthDataReader;
    private HealthDataApiManager apiManager;
    private boolean isMonitoring = false;

    @Override
    public void onCreate() {
        super.onCreate();
       // Inițializăm API Manager cu datele salvate
        SharedPreferences prefs = getApplicationContext()
                .getSharedPreferences("RedMedAlert", Context.MODE_PRIVATE);
        String authToken = prefs.getString("auth_token", "");
        String deviceId = prefs.getString("device_id", "");
        String userId = prefs.getString("user_id", "");

        apiManager = new HealthDataApiManager(authToken, deviceId, userId);

        // Testăm conexiunea la pornirea serviciului
        ApiTestManager testManager = new ApiTestManager(authToken, deviceId, userId);
        testManager.testApiConnection();

    }

    public void startMonitoring(HealthDataStore healthDataStore) {
        if (isMonitoring) return;

        healthDataReader = new HealthDataReader(healthDataStore);
        isMonitoring = true;
        startDataReading();
    }

    private void startDataReading() {
        if (healthDataReader == null) return;

        healthDataReader.readLatestData(new HealthDataReader.HealthDataListener() {
            @Override
            public void onDataReceived(Map<String, Double> data) {
                // Trimitem datele către server
                apiManager.sendHealthData(data, new HealthDataApiManager.ApiCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Data sent successfully to server");
                    }

                    @Override
                    public void onError(String message) {
                        Log.e(TAG, "Error sending data to server: " + message);
                        // Aici putem implementa logica de retry sau salvare locală
                    }
                });
            }

            @Override
            public void onDataReadError(String message) {
                Log.e(TAG, "Error reading health data: " + message);
            }
        });
    }
    public void stopMonitoring() {
        isMonitoring = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
