package com.feri.redmedalertandroidapp.health.sensor;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.*;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.feri.redmedalertandroidapp.api.config.ApiClient;
import com.feri.redmedalertandroidapp.api.model.HealthDataEntity;
import com.feri.redmedalertandroidapp.api.config.ApiClient.ApiCallback;
import com.feri.redmedalertandroidapp.health.HealthDataWorker;
import com.feri.redmedalertandroidapp.health.monitor.BatteryMonitor;
import com.feri.redmedalertandroidapp.health.util.DataCompressor;
import com.feri.redmedalertandroidapp.health.util.DataValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DataUploadScheduler implements BatteryMonitor.BatteryCallback{

    private static final String TAG = "DataUploadScheduler";

    // Stare sistem
    private final Context context;
    private final SensorDataCache dataCache;
    private final ApiClient apiClient;
    private final WorkManager workManager;
    private final BatteryMonitor batteryMonitor;
    private boolean isInAlertMode = false;
    private boolean isLteEnabled = false;
    private boolean isBatteryCritical = false;

    // Configurare intervale măsurare normale (secunde)
    private static final Map<String, Integer> NORMAL_INTERVALS = Map.of(
            "heart_rate", 60,          // 1 minut
            "blood_oxygen", 300,       // 5 minute
            "bia", 300,               // 5 minute
            "blood_pressure", 900,     // 15 minute
            "body_temperature", 900,   // 15 minute
            "accelerometer", 10,       // 10 secunde
            "gyroscope", 10,          // 10 secunde
            "fall_detection", 10,      // 10 secunde
            "stress", 900,            // 15 minute
            "sleep", 300              // 5 minute
    );

    // Configurare intervale măsurare alertă (secunde)
    private static final Map<String, Integer> ALERT_INTERVALS = Map.of(
            "heart_rate", 30,         // 30 secunde
            "blood_oxygen", 30,
            "bia", 30,
            "blood_pressure", 30,
            "body_temperature", 30,
            "accelerometer", 1,
            "gyroscope", 1,
            "fall_detection", 1,
            "stress", 60,
            "sleep", 60
    );

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY = 5000;
    private static final long RETURN_TO_NORMAL_DELAY = 15 * 60; // 15 minute în secunde

    public interface UploadCallback {
        void onUploadSuccess(int count);
        void onUploadError(String error);
        void onNoDataToUpload();
    }

    public DataUploadScheduler(Context context, SensorDataCache dataCache) {
        this.context = context;
        this.dataCache = dataCache;
        this.apiClient = ApiClient.getInstance(context);
        this.workManager = WorkManager.getInstance(context);
        this.batteryMonitor = new BatteryMonitor(context);
        this.batteryMonitor.setBatteryCallback(this);
        setupInitialSchedule();
    }

    @Override
    public void onBatteryCritical() {
        isBatteryCritical = true;
        if (isLteEnabled) {
            enableLteMode(false);
        }
        updateConnectivityStrategy();
    }

    @Override
    public void onBatteryLow() {
        if (isLteEnabled && !isInAlertMode) {
            enableLteMode(false);
        }
    }

    @Override
    public void onBatteryNormal() {
        isBatteryCritical = false;
        updateConnectivityStrategy();
    }

    private void setupInitialSchedule() {
        cancelAllUploads();
        scheduleUploadsForAllSensors(false);
    }

    public void enableLteMode(boolean enabled) {
        if (isBatteryCritical && enabled) {
            Log.w(TAG, "Cannot enable LTE mode when battery is critical");
            return;
        }
        this.isLteEnabled = enabled;
        updateConnectivityStrategy();
    }

    private void updateConnectivityStrategy() {
        cancelAllUploads();
        scheduleUploadsForAllSensors(isInAlertMode);
    }

    private void scheduleUploadsForAllSensors(boolean alertMode) {

        // Verificare baterie înainte de planificare
        batteryMonitor.checkBatteryStatus();

        Constraints.Builder constraintsBuilder = new Constraints.Builder()
                .setRequiredNetworkType(isLteEnabled ? NetworkType.CONNECTED : NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(!alertMode && !isLteEnabled);

        Map<String, Integer> intervals = alertMode ? ALERT_INTERVALS : NORMAL_INTERVALS;

        for (Map.Entry<String, Integer> entry : intervals.entrySet()) {
            // Ajustare interval bazată pe starea bateriei
            int adjustedInterval = isBatteryCritical && !alertMode ?
                    entry.getValue() * 2 : entry.getValue();

            Data inputData = new Data.Builder()
                    .putString("sensor_type", entry.getKey())
                    .putBoolean("is_lte", isLteEnabled)
                    .putBoolean("is_alert_mode", alertMode)
                    .build();

            PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                    HealthDataWorker.class,
                    adjustedInterval,
                    TimeUnit.SECONDS)
                    .setConstraints(constraintsBuilder.build())
                    .setInputData(inputData)
                    .build();

            workManager.enqueueUniquePeriodicWork(
                    "sensor_" + entry.getKey(),
                    ExistingPeriodicWorkPolicy.UPDATE,
                    workRequest
            );
        }
    }

    public void setAlertMode(boolean enabled) {
        if (this.isInAlertMode == enabled) return;

        this.isInAlertMode = enabled;
        Log.d(TAG, "Alert mode changed to: " + enabled);

        updateConnectivityStrategy();
    }

    public void uploadCachedData(UploadCallback callback) {
        if (!isNetworkAvailable()) {
            callback.onUploadError("No network connection available");
            return;
        }

        dataCache.getUnuploadedData(new SensorDataCache.DataCallback() {
            @Override
            public void onDataRetrieved(List<HealthDataEntity> data) {
                if (data.isEmpty()) {
                    callback.onNoDataToUpload();
                    return;
                }
                uploadDataBatch(data, callback, 0);
            }

            @Override
            public void onDataError(String error) {
                callback.onUploadError("Failed to retrieve cached data: " + error);
            }
        });
    }



    private void uploadDataBatch(List<HealthDataEntity> data, UploadCallback callback, int retryAttempt) {
        if (retryAttempt >= MAX_RETRY_ATTEMPTS) {
            callback.onUploadError("Max retry attempts reached");
            return;
        }

        // Validare date
        List<HealthDataEntity> validData = DataValidator.validateAndFilter(data);
        if (validData.isEmpty()) {
            callback.onNoDataToUpload();
            return;
        }

        // Compresie date
        byte[] compressedData = DataCompressor.compressData(validData);
        if (compressedData == null) {
            callback.onUploadError("Data compression failed");
            return;
        }

        // Upload date comprimate
        apiClient.uploadHealthData(convertToApiFormat(validData), new ApiCallback() {
            @Override
            public void onSuccess() {
                markDataAsUploaded(validData);
                callback.onUploadSuccess(validData.size());

                // Verificare baterie după upload
                batteryMonitor.checkBatteryStatus();
            }

            @Override
            public void onError(String error) {
                if (retryAttempt < MAX_RETRY_ATTEMPTS) {
                    new android.os.Handler().postDelayed(
                            () -> uploadDataBatch(data, callback, retryAttempt + 1),
                            RETRY_DELAY
                    );
                } else {
                    callback.onUploadError("Upload failed after " + MAX_RETRY_ATTEMPTS + " attempts");
                }
            }
        });
    }

    private void markDataAsUploaded(List<HealthDataEntity> data) {
        List<Long> uploadedIds = new ArrayList<>();
        for (HealthDataEntity entity : data) {
            uploadedIds.add(entity.id);
        }
        dataCache.markDataAsUploaded(uploadedIds);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;

        android.net.Network network = connectivityManager.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        if (capabilities == null) return false;

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
    }

    private Map<String, Double> convertToApiFormat(List<HealthDataEntity> data) {
        Map<String, Double> apiData = new HashMap<>();
        for (HealthDataEntity entity : data) {
            apiData.put(entity.dataType, entity.value);
        }
        return apiData;
    }

    public void cancelAllUploads() {
        workManager.cancelAllWork();
    }
}