package com.feri.redmedalertandroidapp.api.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.feri.redmedalertandroidapp.api.model.HealthDataEntity;

import java.util.List;

@Dao
public interface HealthDataDao {

    @Insert
    void insert(HealthDataEntity data);

    @Query("SELECT * FROM health_data WHERE uploaded = 0")
    List<HealthDataEntity> getUnuploadedData();

    @Query("UPDATE health_data SET uploaded = 1 WHERE id IN (:ids)")
    void markAsUploaded(List<Long> ids);
}
