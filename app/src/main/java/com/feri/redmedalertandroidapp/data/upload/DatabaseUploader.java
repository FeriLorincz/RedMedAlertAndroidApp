package com.feri.redmedalertandroidapp.data.upload;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.feri.redmedalertandroidapp.api.config.ApiClient;
import com.feri.redmedalertandroidapp.data.DataRepository;
import com.feri.redmedalertandroidapp.data.model.SensorDataEntity;

import com.feri.redmedalertandroidapp.network.NetworkModule;
import com.feri.redmedalertandroidapp.network.NetworkStateMonitor;
import com.feri.redmedalertandroidapp.network.SensorDataApi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

public class DatabaseUploader {

    private static final int MAX_BATCH_SIZE = 100;
    private static final int MAX_RETRY_ATTEMPTS = 5;
    private static final long UPLOAD_INTERVAL_MINUTES = 15;
    private static final int TIMEOUT_SECONDS = 30;
    private static final int SYNC_TIMEOUT_SECONDS = 60;


    private final Context context;
    private final DataRepository dataRepository;
    private final SensorDataApi sensorDataApi;
    private final NetworkStateMonitor networkMonitor;

    public DatabaseUploader(Context context, DataRepository dataRepository) {
        this.context = context;
        this.dataRepository = dataRepository;
        this.networkMonitor = createNetworkMonitor();
        this.sensorDataApi = createSensorApi();

        schedulePeriodicUpload();
    }

    protected NetworkStateMonitor createNetworkMonitor() {
        return new NetworkStateMonitor(context);
    }

    protected SensorDataApi createSensorApi() {
        return NetworkModule.getInstance().getSensorDataApi();
    }

    protected void schedulePeriodicUpload() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build();

        PeriodicWorkRequest uploadWorkRequest = new PeriodicWorkRequest.Builder(
                DatabaseUploadWorker.class,
                UPLOAD_INTERVAL_MINUTES,
                TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueue(uploadWorkRequest);
    }

    public boolean uploadPendingData() {
        if (!isNetworkAvailable()) {
            Timber.d("No network connection available. Skipping upload.");
            return false;
        }

        try {
            Future<List<SensorDataEntity>> futureSensorData = dataRepository.getUnsyncedData();
            List<SensorDataEntity> unsyncedData = futureSensorData.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (unsyncedData.isEmpty()) {
                Timber.d("No unsynced data to upload");
                return true;
            }

            List<List<SensorDataEntity>> batches = createBatches(unsyncedData);
            boolean allSuccess = true;
            for (List<SensorDataEntity> batch : batches) {
                if (!uploadBatch(batch)) {
                    allSuccess = false;
                    break; // Important: ieșim din loop la prima eroare
                }
            }
            return allSuccess;
        } catch (Exception e) {
            Timber.e(e, "Error during data upload");
            return false;
        }
    }

    private List<List<SensorDataEntity>> createBatches(List<SensorDataEntity> data) {
        List<List<SensorDataEntity>> batches = new ArrayList<>();
        for (int i = 0; i < data.size(); i += MAX_BATCH_SIZE) {
            int end = Math.min(i + MAX_BATCH_SIZE, data.size());
            batches.add(data.subList(i, end));
        }
        return batches;
    }

    private boolean uploadBatch(List<SensorDataEntity> batch) {
        if (batch == null || batch.isEmpty()) {
            return true;
        }

        try {
            Call<Void> call = sensorDataApi.uploadSensorData(batch);
            Response<Void> response;

            try {
                response = call.execute();
            } catch (IOException e) {
                Timber.e(e, "Network error during batch upload");
                handleUploadError(batch);
                return false;
            }

            if (!response.isSuccessful()) {
                Timber.e("Upload failed with code: %d", response.code());
                handleUploadError(batch);
                return false;
            }

            try {
                List<Long> uploadedIds = batch.stream()
                        .map(SensorDataEntity::getId)
                        .collect(Collectors.toList());

                int markedCount = dataRepository.markAsSynced(uploadedIds)
                        .get(SYNC_TIMEOUT_SECONDS, TimeUnit.SECONDS);

                if (markedCount != uploadedIds.size()) {
                    handleUploadError(batch);
                    return false;
                }
            } catch (Exception e) {
                Timber.e(e, "Error marking data as synced");
                handleUploadError(batch);
                return false;
            }

            return true;
        } catch (Exception e) {
            Timber.e(e, "Error during batch upload");
            handleUploadError(batch);
            return false;
        }
    }

    private boolean markAsSynced(List<SensorDataEntity> batch) {
        List<Long> uploadedIds = batch.stream()
                .map(SensorDataEntity::getId)
                .collect(Collectors.toList());

        try {
            int markedCount = dataRepository.markAsSynced(uploadedIds)
                    .get(SYNC_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (markedCount != uploadedIds.size()) {
                handleUploadError(batch);
                return false;
            }

            return true;
        } catch (Exception e) {
            Timber.e(e, "Error marking data as synced");
            handleUploadError(batch);
            return false;
        }
    }

    private void handleUploadError(List<SensorDataEntity> batch) {
        List<Long> ids = new ArrayList<>();
        for (SensorDataEntity entity : batch) {
            if (entity.getUploadAttempts() < MAX_RETRY_ATTEMPTS) {
                ids.add(entity.getId());
            }
        }
        if (!ids.isEmpty()) {
            try {
                Future<Void> incrementFuture = dataRepository.incrementUploadAttempts(ids);
                incrementFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (Exception e) {
                Timber.e(e, "Error incrementing upload attempts");
            }
        }
    }

    private boolean isNetworkAvailable() {
        return networkMonitor.isNetworkAvailable();
    }

    public void cleanOldData() {
        try {
            // Păstrăm datele pentru ultimele 7 zile
            long cutoffTime = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);
            Future<Void> cleanupFuture = dataRepository.cleanOldData(cutoffTime);
            cleanupFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            Timber.e(e, "Error cleaning old data");
        }
    }
}

