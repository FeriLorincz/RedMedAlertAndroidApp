package com.feri.redmedalertandroidapp.data;

import android.content.Context;
import androidx.room.Room;
import com.feri.redmedalertandroidapp.data.dao.SensorDataDao;
import com.feri.redmedalertandroidapp.data.model.SensorDataEntity;
import com.feri.redmedalertandroidapp.data.database.AppDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class DataRepository {

        private static final String DATABASE_NAME = "redmedalert_db";
        private static com.feri.redmedalertandroidapp.data.DataRepository instance;
        private final SensorDataDao sensorDataDao;
        private final ExecutorService executorService;
        private final AppDatabase database;

        private DataRepository(Context context) {
            database = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME)
                    .build();

            this.sensorDataDao = database.sensorDataDao();
            this.executorService = Executors.newFixedThreadPool(4);
        }


        public static synchronized com.feri.redmedalertandroidapp.data.DataRepository getInstance(Context context) {
            if (instance == null) {
                instance = new com.feri.redmedalertandroidapp.data.DataRepository(context);
            }
            return instance;
        }

        public long saveSensorData(final SensorDataEntity sensorData) {
            Future<Long> future = executorService.submit(() -> sensorDataDao.insert(sensorData));
            try {
                return future.get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        }

        public void saveSensorDataBatch(final List<SensorDataEntity> sensorDataList) {
            executorService.execute(() -> sensorDataDao.insertAll(sensorDataList));
        }

        public List<SensorDataEntity> getUnsyncedData() {
            Future<List<SensorDataEntity>> future = executorService.submit(sensorDataDao::getUnsyncedData);
            try {
                return future.get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                e.printStackTrace();
                return List.of();
            }
        }

        public void markAsSynced(List<Long> ids) {
            executorService.execute(() -> sensorDataDao.markAsSynced(ids));
        }

        public void incrementUploadAttempts(List<Long> ids) {
            executorService.execute(() -> sensorDataDao.incrementUploadAttempts(ids));
        }

        public void cleanOldData(long timestamp) {
            executorService.execute(() -> sensorDataDao.deleteOldSyncedData(timestamp));
        }

        public void shutdown() {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
            database.close();
        }

        // Metodă pentru teste
        public void clearAllData() {
            executorService.execute(() -> database.clearAllTables());
        }
    }

