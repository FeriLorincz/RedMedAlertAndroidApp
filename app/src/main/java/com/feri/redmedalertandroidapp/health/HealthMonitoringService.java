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
    private MotionSensorReader motionSensorReader;
    private HealthDataApiManager apiManager;
    private boolean isMonitoring = false;
    private SensorDataSimulator simulator;

    @Override
    public void onCreate() {
        super.onCreate();

        // Inițializăm motion sensor reader
        motionSensorReader = new MotionSensorReader(this);
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


        // Inițializăm simulatorul
        simulator = new SensorDataSimulator();
        simulator.start(data -> {
            // Procesăm datele simulate ca și cum ar veni de la un dispozitiv real
            apiManager.sendHealthData(data, new HealthDataApiManager.ApiCallback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Simulated data sent successfully");
                }

                @Override
                public void onError(String message) {
                    Log.e(TAG, "Error sending simulated data: " + message);
                }
            });
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (simulator != null) {
            simulator.stop();
        }
    }


    public void startMonitoring(HealthDataStore healthDataStore) {
        if (isMonitoring) return;

        healthDataReader = new HealthDataReader(healthDataStore);
        isMonitoring = true;

        // Pornim citirea datelor de la senzorii de mișcare
        motionSensorReader.startReading(new MotionSensorReader.MotionDataListener() {
            @Override
            public void onMotionDataReceived(Map<String, Double[]> motionData) {
                // Procesăm datele de la senzori
                processMotionData(motionData);
            }

            @Override
            public void onMotionError(String message) {
                Log.e(TAG, "Motion sensor error: " + message);
            }
        });

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

    private void processMotionData(Map<String, Double[]> motionData) {
        // Aici vom procesa datele de la accelerometru și giroscop
        // Pentru moment doar le logăm
        if (motionData.containsKey("accelerometer")) {
            Double[] accData = motionData.get("accelerometer");
            Log.d(TAG, String.format("Accelerometer - X: %.2f, Y: %.2f, Z: %.2f",
                    accData[0], accData[1], accData[2]));
        }

        if (motionData.containsKey("gyroscope")) {
            Double[] gyroData = motionData.get("gyroscope");
            Log.d(TAG, String.format("Gyroscope - X: %.2f, Y: %.2f, Z: %.2f",
                    gyroData[0], gyroData[1], gyroData[2]));
        }
    }

    public void stopMonitoring() {
        isMonitoring = false;
        if (motionSensorReader != null) {
            motionSensorReader.stopReading();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
