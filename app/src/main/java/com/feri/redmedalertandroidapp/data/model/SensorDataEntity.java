package com.feri.redmedalertandroidapp.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.feri.redmedalertandroidapp.data.utils.DateConverter;

@Entity(tableName = "sensor_data")
@TypeConverters(DateConverter.class)
public class SensorDataEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String deviceId;
    private String userId;
    private String sensorType;
    private double value;
    private Double secondaryValue;  // Optional, pentru senzori cu valori multiple
    private String unit;
    private long timestamp;
    private boolean isAnomalous;
    private String additionalInfo;  // JSON pentru date extra
    private boolean isSynced;       // Flag pentru status sincronizare
    private int uploadAttempts;     // Număr încercări upload

    // Constructori
    public SensorDataEntity() {}

    public SensorDataEntity(String deviceId, String userId, String sensorType,
                            double value, String unit, long timestamp) {
        this.deviceId = deviceId;
        this.userId = userId;
        this.sensorType = sensorType;
        this.value = value;
        this.unit = unit;
        this.timestamp = timestamp;
        this.isSynced = false;
        this.uploadAttempts = 0;
    }

    // Getters și Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSensorType() { return sensorType; }
    public void setSensorType(String sensorType) { this.sensorType = sensorType; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }

    public Double getSecondaryValue() { return secondaryValue; }
    public void setSecondaryValue(Double secondaryValue) { this.secondaryValue = secondaryValue; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isAnomalous() { return isAnomalous; }
    public void setAnomalous(boolean anomalous) { isAnomalous = anomalous; }

    public String getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }

    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { isSynced = synced; }

    public int getUploadAttempts() { return uploadAttempts; }
    public void setUploadAttempts(int uploadAttempts) { this.uploadAttempts = uploadAttempts; }
}
