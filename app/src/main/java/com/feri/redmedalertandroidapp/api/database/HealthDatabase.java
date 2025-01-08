package com.feri.redmedalertandroidapp.api.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import com.feri.redmedalertandroidapp.api.dao.HealthDataDao;
import com.feri.redmedalertandroidapp.api.model.HealthDataEntity;

@Database(entities = {HealthDataEntity.class}, version = 1, exportSchema = false)
public abstract class HealthDatabase extends RoomDatabase{
    public abstract HealthDataDao healthDataDao();
}
