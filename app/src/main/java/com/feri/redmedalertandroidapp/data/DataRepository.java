package com.feri.redmedalertandroidapp.data;

import android.content.Context;
import androidx.room.Room;
import com.feri.redmedalertandroidapp.data.dao.SensorDataDao;
import com.feri.redmedalertandroidapp.data.model.SensorDataEntity;
import com.feri.redmedalertandroidapp.data.database.AppDatabase;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import timber.log.Timber;

public class DataRepository {

    private static final String DATABASE_NAME = "redmedalert_db";
    private static DataRepository instance;
    private final SensorDataDao sensorDataDao;
    private final AppDatabase database;
    private ExecutorService executorService;
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);

    private DataRepository(Context context) {
        database = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        DATABASE_NAME)
                .build();

        this.sensorDataDao = database.sensorDataDao();
        this.executorService = Executors.newFixedThreadPool(4);
    }

    public static synchronized DataRepository getInstance(Context context) {
        if (instance == null) {
            instance = new DataRepository(context);
        }
        return instance;
    }

    private synchronized void ensureExecutorService() {
        if (executorService == null || executorService.isShutdown()) {
            executorService = Executors.newFixedThreadPool(4);
            isShutdown.set(false);
        }
    }

    public Future<Long> saveSensorData(final SensorDataEntity sensorData) {
        ensureExecutorService();
        return executorService.submit(() -> sensorDataDao.insert(sensorData));
    }

    public Future<Void> saveSensorDataBatch(final List<SensorDataEntity> sensorDataList) {
        ensureExecutorService();
        return executorService.submit(() -> {
            sensorDataDao.insertAll(sensorDataList);
            return null;
        });
    }

    public Future<List<SensorDataEntity>> getUnsyncedData() {
        ensureExecutorService();
        return executorService.submit(sensorDataDao::getUnsyncedData);
    }

    public Future<Void> markAsSynced(final List<Long> ids) {
        ensureExecutorService();
        return executorService.submit(() -> {
            try {
                Timber.d("Marking IDs as synced: %s", ids);
                sensorDataDao.markAsSynced(ids);

                // Verificare imediată
                List<SensorDataEntity> stillUnsynced = sensorDataDao.getUnsyncedData();
                Timber.d("After marking as synced, found %d unsynced records",
                        stillUnsynced.size());

                // Verificare specifică pentru ID-urile marcate
                List<Long> stillUnsyncedIds = stillUnsynced.stream()
                        .map(SensorDataEntity::getId)
                        .filter(ids::contains)
                        .collect(Collectors.toList());

                if (!stillUnsyncedIds.isEmpty()) {
                    Timber.e("Failed to mark IDs as synced: %s", stillUnsyncedIds);
                    throw new RuntimeException("Failed to mark all data as synced");
                }

                return null;
            } catch (Exception e) {
                Timber.e(e, "Error marking data as synced");
                throw e;
            }
        });
    }

    public Future<Void> incrementUploadAttempts(final List<Long> ids) {
        ensureExecutorService();
        return executorService.submit(() -> {
            sensorDataDao.incrementUploadAttempts(ids);
            return null;
        });
    }

    public Future<Void> cleanOldData(final long timestamp) {
        ensureExecutorService();
        return executorService.submit(() -> {
            sensorDataDao.deleteOldSyncedData(timestamp);
            return null;
        });
    }

    public Future<Void> clearAllData() {
        ensureExecutorService();
        return executorService.submit(() -> {
            database.clearAllTables();
            return null;
        });
    }

    public synchronized Future<Void> shutdown() {
        if (isShutdown.get()) {
            return executorService.submit(() -> null);
        }

        return executorService.submit(() -> {
            try {
                isShutdown.set(true);
                executorService.shutdown();
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
                database.close();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Shutdown interrupted", e);
            }
            return null;
        });
    }

    public static synchronized void resetInstance() {
        if (instance != null) {
            try {
                instance.shutdown().get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                e.printStackTrace();
            }
            instance = null;
        }
    }
}