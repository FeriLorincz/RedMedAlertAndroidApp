package com.feri.redmedalertandroidapp.api.model;

import com.google.gson.annotations.SerializedName;
import java.time.LocalDateTime;
import java.util.Map;

public class HealthDataPayload {

    @SerializedName("userId")
    private String userId;

    @SerializedName("deviceId")
    private String deviceId;

    @SerializedName("sensorValues")  // Schimbat din sensor_data pentru a se potrivi cu serverul
    private Map<String, Double> sensorValues;

    @SerializedName("alertType")
    private String alertType = "TEST";

    @SerializedName("timestamp")
    private String timestamp;

    // Constructor pentru test
    public HealthDataPayload(Map<String, Double> data) {
        this("test-device", "test-user", data);
    }

    public HealthDataPayload(String deviceId, String userId, Map<String, Double> sensorData) {
        this.deviceId = deviceId;
        this.userId = userId;
        this.sensorValues = sensorData;
        this.timestamp = LocalDateTime.now().toString();
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public Map<String, Double> getSensorValues() { return sensorValues; }
    public void setSensorValues(Map<String, Double> sensorValues) { this.sensorValues = sensorValues; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }
}
