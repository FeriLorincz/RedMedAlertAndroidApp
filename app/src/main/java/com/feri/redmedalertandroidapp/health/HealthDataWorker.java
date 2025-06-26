package com.feri.redmedalertandroidapp.health;


import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.feri.redmedalertandroidapp.api.config.ApiClient;
import com.feri.redmedalertandroidapp.api.model.HealthDataEntity;
import com.feri.redmedalertandroidapp.api.config.ApiClient.ApiCallback;
import com.feri.redmedalertandroidapp.health.sensor.SensorDataCache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class HealthDataWorker extends Worker {

    private static final String TAG = "HealthDataWorker";
    private final SensorDataCache dataCache;
    private final ApiClient apiClient;
    private final String sensorType;
    private final boolean isLteMode;
    private final boolean isAlertMode;

    public HealthDataWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.dataCache = new SensorDataCache(context);
        this.apiClient = ApiClient.getInstance(context);

        Data inputData = params.getInputData();
        this.sensorType = inputData.getString("sensor_type");
        this.isLteMode = inputData.getBoolean("is_lte", false);
        this.isAlertMode = inputData.getBoolean("is_alert_mode", false);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, String.format("Starting work for sensor: %s, LTE: %b, Alert: %b",
                sensorType, isLteMode, isAlertMode));

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean success = new AtomicBoolean(false);

        try {
            dataCache.getUnuploadedData(new SensorDataCache.DataCallback() {
                @Override
                public void onDataRetrieved(List<HealthDataEntity> data) {
                    if (data.isEmpty()) {
                        success.set(true);
                        latch.countDown();
                        return;
                    }

                    List<HealthDataEntity> filteredData = filterDataForSensor(data);
                    if (filteredData.isEmpty()) {
                        success.set(true);
                        latch.countDown();
                        return;
                    }

                    uploadData(filteredData, success, latch);
                }

                @Override
                public void onDataError(String error) {
                    Log.e(TAG, "Error retrieving data: " + error);
                    success.set(false);
                    latch.countDown();
                }
            });

            latch.await(getTimeoutForMode(), TimeUnit.SECONDS);
            return success.get() ? Result.success() : Result.retry();

        } catch (Exception e) {
            Log.e(TAG, "Error in worker: " + e.getMessage());
            return Result.failure();
        }
    }

    private List<HealthDataEntity> filterDataForSensor(List<HealthDataEntity> allData) {
        if (sensorType == null) return allData;

        List<HealthDataEntity> filtered = new ArrayList<>();
        for (HealthDataEntity entity : allData) {
            if (sensorType.equals(entity.dataType)) {
                filtered.add(entity);
            }
        }
        return filtered;
    }

    private void uploadData(List<HealthDataEntity> data, AtomicBoolean success, CountDownLatch latch) {
        Map<String, Double> apiData = new HashMap<>();
        List<Long> dataIds = new ArrayList<>();

        for (HealthDataEntity entity : data) {
            apiData.put(entity.dataType, entity.value);
            dataIds.add(entity.id);
        }

        apiClient.uploadHealthData(apiData, new ApiCallback() {
            @Override
            public void onSuccess() {
                dataCache.markDataAsUploaded(dataIds);
                success.set(true);
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Upload failed: " + error);
                success.set(false);
                latch.countDown();
            }
        });
    }

    private int getTimeoutForMode() {
        if (isAlertMode) return 30; // 30 secunde în mod alertă
        if (isLteMode) return 120;  // 2 minute pentru LTE
        return 60;                  // 1 minut mod normal
    }
}