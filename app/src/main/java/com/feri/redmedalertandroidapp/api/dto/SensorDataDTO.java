package com.feri.redmedalertandroidapp.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDateTime;

public class SensorDataDTO {
    private String id;

    @JsonProperty(required = true)
    private String deviceId;

    @JsonProperty(required = true)
    private String userId;

    private String sensorType;
    private Double value;
    private Double secondaryValue;
    private String unit;
    private LocalDateTime timestamp;
    private Boolean isAnomalous;
    private String additionalInfo;

    // Constructor simplu pentru usage
    public SensorDataDTO(String deviceId, String userId, String sensorType, Double value, LocalDateTime timestamp) {
        this.deviceId = deviceId;
        this.userId = userId;
        this.sensorType = sensorType;
        this.value = value;
        this.timestamp = timestamp;
        this.isAnomalous = false;
    }

    // Getters și Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Double getSecondaryValue() {
        return secondaryValue;
    }

    public void setSecondaryValue(Double secondaryValue) {
        this.secondaryValue = secondaryValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean getIsAnomalous() {
        return isAnomalous;
    }

    public void setIsAnomalous(Boolean isAnomalous) {
        this.isAnomalous = isAnomalous;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}