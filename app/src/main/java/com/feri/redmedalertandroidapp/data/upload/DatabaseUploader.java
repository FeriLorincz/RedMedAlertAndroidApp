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
import java.util.concurrent.TimeUnit;
import retrofit2.Response;
import timber.log.Timber;

public class DatabaseUploader {

    private static final int MAX_BATCH_SIZE = 100;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long UPLOAD_INTERVAL_MINUTES = 15;

    private final Context context;
    private final DataRepository dataRepository;
    private final SensorDataApi sensorDataApi;
    private final ConnectivityManager connectivityManager;

    public DatabaseUploader(Context context, DataRepository dataRepository) {
        this.context = context;
        this.dataRepository = dataRepository;
        //this.sensorDataApi = NetworkModule.getInstance().getSensorDataApi();
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

    public void uploadPendingData() {
        if (!isNetworkAvailable()) {
            Timber.d("No network connection available. Skipping upload.");
            return;
        }

        try {
            List<SensorDataEntity> unsyncedData = dataRepository.getUnsyncedData();
            if (unsyncedData.isEmpty()) {
                Timber.d("No unsynced data to upload");
                return;
            }

            List<List<SensorDataEntity>> batches = createBatches(unsyncedData);
            for (List<SensorDataEntity> batch : batches) {
                uploadBatch(batch);
            }

        } catch (Exception e) {
            Timber.e(e, "Error during data upload");
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

    private void uploadBatch(List<SensorDataEntity> batch) {
        try {
            Response<Void> response = sensorDataApi.uploadSensorData(batch).execute();

            if (response.isSuccessful()) {
                List<Long> uploadedIds = new ArrayList<>();
                for (SensorDataEntity entity : batch) {
                    uploadedIds.add(entity.getId());
                }
                dataRepository.markAsSynced(uploadedIds);
                Timber.d("Successfully uploaded %d records", batch.size());
            } else {
                handleUploadError(batch);
            }
        } catch (Exception e) {
            Timber.e(e, "Error uploading batch");
            handleUploadError(batch);
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
            dataRepository.incrementUploadAttempts(ids);
        }
    }

    private boolean isNetworkAvailable() {
        if (connectivityManager == null) return false;
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public void cleanOldData() {
        // Păstrăm datele pentru ultimele 7 zile
        long cutoffTime = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);
        dataRepository.cleanOldData(cutoffTime);
    }

    //Adăugat metodă protected pentru testare
        protected SensorDataApi createSensorApi() {
            return NetworkModule.getInstance().getSensorDataApi();
        }
}
