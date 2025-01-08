package com.feri.redmedalertandroidapp.data.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.feri.redmedalertandroidapp.data.dao.SensorDataDao;
import com.feri.redmedalertandroidapp.data.model.SensorDataEntity;
import com.feri.redmedalertandroidapp.data.utils.DateConverter;

@Database(
        entities = {SensorDataEntity.class},
        version = 1,
        exportSchema = false
)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase{
    public abstract SensorDataDao sensorDataDao();
}
