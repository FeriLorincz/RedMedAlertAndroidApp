package com.feri.redmedalertandroidapp.health.sensor;

import android.content.Context;
import android.util.Log;
import androidx.room.Room;

import com.feri.redmedalertandroidapp.api.database.HealthDatabase;
import com.feri.redmedalertandroidapp.api.model.HealthDataEntity;
import com.feri.redmedalertandroidapp.api.dao.HealthDataDao;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SensorDataCache {

    private static final String TAG = "SensorDataCache";
    private final HealthDatabase database;
    private final HealthDataDao healthDataDao;
    private final ExecutorService executorService;
    private static final int CACHE_THRESHOLD = 100; // Număr maxim de înregistrări cache

    public interface CacheCallback {
        void onDataCached();
        void onCacheError(String error);
        void onCacheThresholdReached(int size);
    }

    public SensorDataCache(Context context) {
        database = Room.databaseBuilder(
                context.getApplicationContext(),
                HealthDatabase.class,
                "sensor-cache-db"
        ).build();

        healthDataDao = database.healthDataDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void cacheData(Map<String, Double> sensorData, CacheCallback callback) {
        executorService.execute(() -> {
            try {
                for (Map.Entry<String, Double> entry : sensorData.entrySet()) {
                    HealthDataEntity entity = new HealthDataEntity();
                    entity.timestamp = System.currentTimeMillis();
                    entity.dataType = entry.getKey();
                    entity.value = entry.getValue();
                    entity.uploaded = false;

                    healthDataDao.insert(entity);
                }

                // Verifică dimensiunea cache-ului
                int cacheSize = getCacheSize();
                if (cacheSize >= CACHE_THRESHOLD) {
                    callback.onCacheThresholdReached(cacheSize);
                }

                callback.onDataCached();
            } catch (Exception e) {
                Log.e(TAG, "Error caching data: " + e.getMessage());
                callback.onCacheError("Failed to cache data: " + e.getMessage());
            }
        });
    }

    public void getUnuploadedData(DataCallback callback) {
        executorService.execute(() -> {
            try {
                List<HealthDataEntity> unuploadedData = healthDataDao.getUnuploadedData();
                callback.onDataRetrieved(unuploadedData);
            } catch (Exception e) {
                Log.e(TAG, "Error retrieving unuploaded data: " + e.getMessage());
                callback.onDataError("Failed to retrieve data: " + e.getMessage());
            }
        });
    }

    public void markDataAsUploaded(List<Long> dataIds) {
        executorService.execute(() -> {
            try {
                healthDataDao.markAsUploaded(dataIds);
            } catch (Exception e) {
                Log.e(TAG, "Error marking data as uploaded: " + e.getMessage());
            }
        });
    }

    private int getCacheSize() {
        try {
            return healthDataDao.getUnuploadedData().size();
        } catch (Exception e) {
            Log.e(TAG, "Error getting cache size: " + e.getMessage());
            return 0;
        }
    }

    public void clearOldData(long cutoffTime) {
        executorService.execute(() -> {
            try {
                healthDataDao.deleteOldData(cutoffTime);
            } catch (Exception e) {
                Log.e(TAG, "Error clearing old data: " + e.getMessage());
            }
        });
    }

    public interface DataCallback {
        void onDataRetrieved(List<HealthDataEntity> data);
        void onDataError(String error);
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
