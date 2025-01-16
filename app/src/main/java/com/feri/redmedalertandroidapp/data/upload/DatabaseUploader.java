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
import com.feri.redmedalertandroidapp.network.SensorDataApi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import retrofit2.Response;
import timber.log.Timber;

public class DatabaseUploader {

    private static final int MAX_BATCH_SIZE = 100;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long UPLOAD_INTERVAL_MINUTES = 15;
    private static final int TIMEOUT_SECONDS = 30;

    private final Context context;
    private final DataRepository dataRepository;
    private final SensorDataApi sensorDataApi;
    private final ConnectivityManager connectivityManager;

    public DatabaseUploader(Context context, DataRepository dataRepository) {
        this.context = context;
        this.dataRepository = dataRepository;
        this.sensorDataApi = createSensorApi();
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        schedulePeriodicUpload();
    }

    private void schedulePeriodicUpload() {
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
        try {
            Timber.tag("SyncTest").d("Attempting to upload batch of %d records - tag", batch.size());
            Timber.d("Starting upload for batch of %d records", batch.size());
            Response<Void> response = sensorDataApi.uploadSensorData(batch).execute();
            Timber.d("Server response received: %d", response.code());
            Timber.tag("SyncTest").d("Upload response: %d - tag", response.code());

            if (response.isSuccessful()) {
                List<Long> uploadedIds = batch.stream()
                        .map(SensorDataEntity::getId)
                        .collect(Collectors.toList());

                Timber.d("Upload successful, marking IDs as synced: %s", uploadedIds);

                try {
                    // Markăm ca sincronizate și așteaptăm confirmarea
                    Future<Void> markSyncedFuture = dataRepository.markAsSynced(uploadedIds);
                    markSyncedFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

                    // Verificăm explicit că datele au fost marcate
                    Future<List<SensorDataEntity>> verifyFuture = dataRepository.getUnsyncedData();
                    List<SensorDataEntity> remainingUnsynced = verifyFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    boolean allSynced = remainingUnsynced.stream()
                            .noneMatch(data -> uploadedIds.contains(data.getId()));

                    if (allSynced) {
                        Timber.d("Successfully verified sync for IDs: %s", uploadedIds);
                        return true;
                    } else {
                        Timber.e("Failed to verify sync for IDs: %s", uploadedIds);
                        return false;
                    }
                } catch (Exception e) {
                    Timber.e(e, "Error marking data as synced");
                    return false;
                }
            } else {
                Timber.e("Upload failed with code: %d, body: %s",
                        response.code(),
                        response.errorBody() != null ? response.errorBody().string() : "no error body");
                return false;
            }
        } catch (Exception e) {
            Timber.tag("SyncTest").e(e, "Error during batch upload - tag");
            Timber.e(e, "Error during batch upload: %s", e.getMessage());
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
        if (connectivityManager == null) return false;
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
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

    protected SensorDataApi createSensorApi() {
        return NetworkModule.getInstance().getSensorDataApi();
    }
}
