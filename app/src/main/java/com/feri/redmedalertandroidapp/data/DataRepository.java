package com.feri.redmedalertandroidapp.data;

import android.content.Context;
import androidx.room.Room;
import com.feri.redmedalertandroidapp.data.dao.SensorDataDao;
import com.feri.redmedalertandroidapp.data.model.SensorDataEntity;
import com.feri.redmedalertandroidapp.data.database.AppDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import timber.log.Timber;

public class DataRepository {

    private static final String DATABASE_NAME = "redmedalert_db";
    private static DataRepository instance;
    private final SensorDataDao sensorDataDao;
    private final AppDatabase database;
    private ExecutorService executorService;
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);
    private static final Object LOCK = new Object();
    private volatile boolean isClosing = false;
    private static final ReentrantLock instanceLock = new ReentrantLock();


    private DataRepository(Context context) {
        database = AppDatabase.buildDatabase(context);
        this.sensorDataDao = database.sensorDataDao();
        this.executorService = Executors.newFixedThreadPool(4);
    }

    public static synchronized DataRepository getInstance(Context context) {
        instanceLock.lock();
        try {
            if (instance == null || instance.database == null || !instance.database.isOpen()) {
                if (instance != null) {
                    instance.shutdownNow();
                    instance = null;
                }
                instance = new DataRepository(context);
            }
            return instance;
        } finally {
            instanceLock.unlock();
        }
    }


    private synchronized void ensureExecutorService() {
        if (isClosing) {
            throw new IllegalStateException("Repository is shutting down");
        }
        if (executorService == null || executorService.isShutdown()) {
            executorService = Executors.newFixedThreadPool(4);
        }
    }


    public synchronized Future<Long> saveSensorData(final SensorDataEntity sensorData) {
        ensureExecutorService();
        return executorService.submit(() -> {
            try {
                if (database == null || !database.isOpen()) {
                    throw new IllegalStateException("Database is not open");
                }

                long id = sensorDataDao.insertRaw(
                        sensorData.getDeviceId(),
                        sensorData.getUserId(),
                        sensorData.getSensorType(),
                        sensorData.getValue(),
                        sensorData.getUnit(),
                        sensorData.getTimestamp(),
                        false,  // forțăm isSynced = false
                        0      // forțăm uploadAttempts = 0
                );

                // Verificări post-salvare
                List<SensorDataEntity> verification = sensorDataDao.getUnsyncedData();
                if (verification.isEmpty()) {
                    throw new RuntimeException("Data not found in unsynced after save");
                }

                return id;
            } catch (Exception e) {
                Timber.e(e, "Error saving sensor data");
                throw e;
            }
        });
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
        return executorService.submit(() -> {
            try {
                if (database == null || !database.isOpen()) {
                    throw new IllegalStateException("Database is not open");
                }

                List<SensorDataEntity> data = sensorDataDao.getUnsyncedData();
                Timber.d("Retrieved %d unsynced records", data.size());
                return data;
            } catch (Exception e) {
                Timber.e(e, "Error getting unsynced data");
                throw e;
            }
        });
    }


    public Future<Integer> markAsSynced(final List<Long> ids) {
        ensureExecutorService();
        return executorService.submit(() -> {
            try {
                Timber.d("Marking IDs as synced: %s", ids);
                int updatedCount = sensorDataDao.markAsSynced(ids);


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


                return updatedCount;
            } catch (Exception e) {
                Timber.e(e, "Error marking data as synced");
                throw e;
            }
        });
    }


    public Future<List<SensorDataEntity>> getByIds(final List<Long> ids) {
        ensureExecutorService();
        return executorService.submit(() -> {
            List<SensorDataEntity> results = new ArrayList<>();
            for (Long id : ids) {
                // Verifică starea entității
                List<SensorDataEntity> dataList = sensorDataDao.getByIds(ids);
                if (!dataList.isEmpty()) {
                    results.addAll(dataList);
                }
            }
            return results;
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


    public Future<Void> shutdown() {
        instanceLock.lock();
        try {
            if (isShutdown.get()) {
                return CompletableFuture.completedFuture(null);
            }
            isShutdown.set(true);

            return executorService.submit(() -> {
                try {
                    if (executorService != null && !executorService.isShutdown()) {
                        executorService.shutdown();
                        if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                            executorService.shutdownNow();
                        }
                    }

                    if (database != null && database.isOpen()) {
                        database.close();
                    }
                } catch (Exception e) {
                    Timber.e(e, "Error during shutdown");
                    throw e;
                }
                return null;
            });
        } finally {
            instanceLock.unlock();
        }
    }

    private void shutdownNow() {
        try {
            if (executorService != null) {
                executorService.shutdownNow();
            }
            if (database != null) {
                database.close();
            }
        } catch (Exception e) {
            Timber.e(e, "Error in immediate shutdown");
        }
    }


    public static synchronized void resetInstance() {
        synchronized (LOCK) {
            if (instance != null) {
                try {
                    // Mai întâi oprim executorul
                    if (instance.executorService != null) {
                        instance.executorService.shutdown();
                        if (!instance.executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                            instance.executorService.shutdownNow();
                        }
                    }

                    // Așteptăm finalizarea operațiilor în curs
                    Thread.sleep(500);

                    // Închidem explicit baza de date
                    AppDatabase.closeDatabase();

                    instance = null;
                    Timber.d("Repository instance reset successfully");
                } catch (Exception e) {
                    Timber.e(e, "Error during repository reset");
                    throw new RuntimeException("Failed to reset repository", e);
                }
            }
        }
    }
}


