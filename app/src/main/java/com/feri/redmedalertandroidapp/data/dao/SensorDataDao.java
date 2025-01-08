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

    @Query("UPDATE sensor_data SET isSynced = 1 WHERE id IN (:ids)")
    void markAsSynced(List<Long> ids);

    @Query("UPDATE sensor_data SET uploadAttempts = uploadAttempts + 1 WHERE id IN (:ids)")
    void incrementUploadAttempts(List<Long> ids);

    @Query("DELETE FROM sensor_data WHERE isSynced = 1 AND timestamp < :timestamp")
    void deleteOldSyncedData(long timestamp);
}
