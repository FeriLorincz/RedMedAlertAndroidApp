package com.feri.redmedalertandroidapp.health.sensor;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.feri.redmedalertandroidapp.api.config.ApiClient;
import com.feri.redmedalertandroidapp.api.model.HealthDataEntity;
import com.feri.redmedalertandroidapp.api.service.ApiCallback;
import com.feri.redmedalertandroidapp.health.HealthDataWorker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DataUploadScheduler {

    private static final String TAG = "DataUploadScheduler";
    private final Context context;
    private final SensorDataCache dataCache;
    private final ApiClient apiClient;
    private final WorkManager workManager;

    // Constante pentru planificare
    private static final long NORMAL_INTERVAL = 15; // minute
    private static final long URGENT_INTERVAL = 1; // minute
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY = 5000; // milisecunde

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
        setupPeriodicUpload();
    }

    private void setupPeriodicUpload() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build();

        PeriodicWorkRequest periodicUpload =
                new PeriodicWorkRequest.Builder(HealthDataWorker.class, NORMAL_INTERVAL, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build();

        workManager.enqueue(periodicUpload);
    }

    public void scheduleUrgentUpload() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        WorkRequest uploadWorkRequest =
                new OneTimeWorkRequest.Builder(HealthDataWorker.class)
                        .setConstraints(constraints)
                        .build();

        workManager.enqueue(uploadWorkRequest);
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

        apiClient.uploadHealthData(convertToApiFormat(data), new ApiCallback() {
            @Override
            public void onSuccess() {
                markDataAsUploaded(data);
                callback.onUploadSuccess(data.size());
            }

            @Override
            public void onError(String error) {
                if (retryAttempt < MAX_RETRY_ATTEMPTS) {
                    // Reîncearcă după un delay
                    new android.os.Handler().postDelayed(() ->
                                    uploadDataBatch(data, callback, retryAttempt + 1),
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
        if (connectivityManager == null) {
            return false;
        }

        android.net.Network network = connectivityManager.getActiveNetwork();

        if (network == null) {
            return false;
        }
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        if (capabilities == null) {
            return false;
        }
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
