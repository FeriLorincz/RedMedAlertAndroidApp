package com.feri.redmedalertandroidapp.health;


import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.feri.redmedalertandroidapp.api.config.ApiClient;
import com.feri.redmedalertandroidapp.api.model.HealthDataEntity;
import com.feri.redmedalertandroidapp.api.service.ApiCallback;
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

    public HealthDataWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.dataCache = new SensorDataCache(context);
        this.apiClient = ApiClient.getInstance(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Starting health data processing and upload");

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

                    // Convertim datele pentru upload
                    Map<String, Double> apiData = new HashMap<>();
                    List<Long> dataIds = new ArrayList<>();

                    for (HealthDataEntity entity : data) {
                        apiData.put(entity.dataType, entity.value);
                        dataIds.add(entity.id);
                    }

                    // Încercăm să încărcăm datele
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

                @Override
                public void onDataError(String error) {
                    Log.e(TAG, "Error retrieving data: " + error);
                    success.set(false);
                    latch.countDown();
                }
            });

            // Așteptăm max 1 minut pentru completarea operației
            latch.await(1, TimeUnit.MINUTES);
            return success.get() ? Result.success() : Result.retry();

        } catch (Exception e) {
            Log.e(TAG, "Error in worker: " + e.getMessage());
            return Result.failure();
        }
    }
}