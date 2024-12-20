package com.feri.redmedalertandroidapp.api.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "health_data")
public class HealthDataEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "timestamp")
    public long timestamp;

    @ColumnInfo(name = "data_type")
    public String dataType;

    @ColumnInfo(name = "value")
    public double value;

    @ColumnInfo(name = "uploaded")
    public boolean uploaded;
}
