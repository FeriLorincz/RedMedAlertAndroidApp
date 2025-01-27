package com.feri.redmedalertandroidapp.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.feri.redmedalertandroidapp.data.model.SensorDataEntity;

import java.util.List;

@Dao
public interface SensorDataDao {

    @Insert
    long insert(SensorDataEntity sensorData);

    @Insert
    List<Long> insertAll(List<SensorDataEntity> sensorDataList);

    @Update
    void update(SensorDataEntity sensorData);

    @Query("SELECT * FROM sensor_data WHERE isSynced = 0 ORDER BY timestamp ASC")
    List<SensorDataEntity> getUnsyncedData();

    @Query("SELECT * FROM sensor_data WHERE sensorType = :type AND timestamp > :since")
    List<SensorDataEntity> getRecentDataByType(String type, long since);

    @Query("UPDATE sensor_data SET isSynced = 1 WHERE id IN (:ids) AND isSynced = 0")
    int markAsSynced(List<Long> ids); // Returnează numărul de înregistrări actualizate

    @Query("SELECT COUNT(*) FROM sensor_data WHERE id IN (:ids) AND isSynced = 0")
    int countUnsyncedById(List<Long> ids);

    @Query("SELECT * FROM sensor_data WHERE id IN (:ids)")
    List<SensorDataEntity> getByIds(List<Long> ids);


    @Query("UPDATE sensor_data SET uploadAttempts = uploadAttempts + 1 WHERE id IN (:ids)")
    void incrementUploadAttempts(List<Long> ids);

    @Query("DELETE FROM sensor_data WHERE isSynced = 1 AND timestamp < :timestamp")
    void deleteOldSyncedData(long timestamp);

    @Query("INSERT INTO sensor_data (deviceId, userId, sensorType, value, unit, timestamp, isSynced, uploadAttempts) " +
            "VALUES (:deviceId, :userId, :sensorType, :value, :unit, :timestamp, :isSynced, :uploadAttempts)")
    Long insertRaw(String deviceId, String userId, String sensorType, double value,
                   String unit, long timestamp, boolean isSynced, int uploadAttempts);
}
