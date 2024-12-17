package com.feri.redmedalertandroidapp.health;

import android.util.Log;

import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthData;
import com.samsung.android.sdk.healthdata.HealthDataResolver;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import com.samsung.android.sdk.healthdata.HealthDataResolver.ReadRequest;
import com.samsung.android.sdk.healthdata.HealthDataResolver.ReadResult;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.Calendar;

public class HealthDataReader {

    private static final String TAG = "HealthDataReader";
    private final HealthDataStore healthDataStore;
    private final HealthDataResolver resolver;

    public HealthDataReader(HealthDataStore healthDataStore) {
        this.healthDataStore = healthDataStore;
        this.resolver = new HealthDataResolver(healthDataStore, null);
    }

    public interface HealthDataListener {
        void onDataReceived(Map<String, Double> data);
        void onDataReadError(String message);
    }

    public void readLatestData(HealthDataListener listener) {
        // Timestamp pentru ultimele 5 minute
        long endTime = System.currentTimeMillis();
        long startTime = endTime - TimeUnit.MINUTES.toMillis(5);

        Map<String, Double> latestData = new HashMap<>();

        // Citim datele pentru puls
        ReadRequest heartRateRequest = new ReadRequest.Builder()
                .setDataType(HealthConstants.HeartRate.HEALTH_DATA_TYPE)
                .setProperties(new String[] {
                        HealthConstants.HeartRate.HEART_RATE
                })
                .setLocalTimeRange(HealthConstants.HeartRate.START_TIME,
                        HealthConstants.HeartRate.TIME_OFFSET,
                        startTime, endTime)
                .build();

        try {
            resolver.read(heartRateRequest).setResultListener(result -> {
                int count = 0;
                double sum = 0;

                try {
                    for (HealthData data : result) {
                        sum += data.getDouble(HealthConstants.HeartRate.HEART_RATE);
                        count++;
                    }

                    if (count > 0) {
                        latestData.put("heart_rate", sum / count);
                    }

                    // După ce am citit toate datele, notificăm listener-ul
                    listener.onDataReceived(latestData);
                } finally {
                    result.close();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error reading heart rate data: " + e.getMessage());
            listener.onDataReadError("Failed to read heart rate data");
        }

        // Citim datele pentru oxigen din sânge
        ReadRequest spO2Request = new ReadRequest.Builder()
                .setDataType("com.samsung.health.oxygen_saturation") // Use string constant
                .setProperties(new String[] {
                        "oxygen_saturation"  // Use string constant
                })
                .setLocalTimeRange("start_time",  // Use string constant
                        "time_offset",      // Use string constant
                        startTime, endTime)
                .build();

        try {
            resolver.read(spO2Request).setResultListener(result -> {
                try {
                    for (HealthData data : result) {
                        latestData.put("blood_oxygen",
                                data.getDouble("oxygen_saturation")); // Use string constant
                        break; // Luăm doar prima valoare
                    }
                    result.close();
                } finally {
                    result.close();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error reading blood oxygen data: " + e.getMessage());
            listener.onDataReadError("Failed to read blood oxygen data");
        }
    }
}
