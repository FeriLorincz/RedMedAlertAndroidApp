package com.feri.redmedalertandroidapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.Map;

public class SensorDataViewModel extends AndroidViewModel{

    // Vital Signs
    private final MutableLiveData<Double> heartRate = new MutableLiveData<>();
    private final MutableLiveData<Double> bloodOxygen = new MutableLiveData<>();
    private final MutableLiveData<Double> bloodPressure = new MutableLiveData<>();
    private final MutableLiveData<Double> temperature = new MutableLiveData<>();

    // Motion Sensors
    private final MutableLiveData<Double> accelerometer = new MutableLiveData<>();
    private final MutableLiveData<Double> gyroscope = new MutableLiveData<>();
    private final MutableLiveData<Double> stepCount = new MutableLiveData<>();
    private final MutableLiveData<Double> gravity = new MutableLiveData<>();
    private final MutableLiveData<Double> linearAcceleration = new MutableLiveData<>();
    private final MutableLiveData<Double> rotation = new MutableLiveData<>();
    private final MutableLiveData<Double> orientation = new MutableLiveData<>();
    private final MutableLiveData<Double> magneticField = new MutableLiveData<>();

    // Environment
    private final MutableLiveData<Double> humidity = new MutableLiveData<>();
    private final MutableLiveData<Double> light = new MutableLiveData<>();
    private final MutableLiveData<Double> proximity = new MutableLiveData<>();

    // Samsung Health
    private final MutableLiveData<Double> bia = new MutableLiveData<>();
    private final MutableLiveData<Double> stress = new MutableLiveData<>();
    private final MutableLiveData<Double> sleep = new MutableLiveData<>();
    private final MutableLiveData<Double> fallDetection = new MutableLiveData<>();

    private final MutableLiveData<String> error = new MutableLiveData<>();

    public SensorDataViewModel(@NonNull Application application) {
        super(application);
    }

    public void updateSensorData(Map<String, Double> newData) {
        for (Map.Entry<String, Double> entry : newData.entrySet()) {
            updateIndividualSensor(entry.getKey(), entry.getValue());
        }
    }

    private void updateIndividualSensor(String sensorType, Double value) {
        try {
            switch (sensorType) {
                // Vital Signs
                case "heart_rate" -> heartRate.setValue(value);
                case "blood_oxygen" -> bloodOxygen.setValue(value);
                case "blood_pressure" -> bloodPressure.setValue(value);
                case "body_temperature" -> temperature.setValue(value);

                // Motion Sensors
                case "accelerometer" -> accelerometer.setValue(value);
                case "gyroscope" -> gyroscope.setValue(value);
                case "step_count" -> stepCount.setValue(value);
                case "gravity" -> gravity.setValue(value);
                case "linear_acceleration" -> linearAcceleration.setValue(value);
                case "rotation" -> rotation.setValue(value);
                case "orientation" -> orientation.setValue(value);
                case "magnetic_field" -> magneticField.setValue(value);

                // Environment
                case "humidity" -> humidity.setValue(value);
                case "light" -> light.setValue(value);
                case "proximity" -> proximity.setValue(value);

                // Samsung Health
                case "bia" -> bia.setValue(value);
                case "stress" -> stress.setValue(value);
                case "sleep" -> sleep.setValue(value);
                case "fall_detection" -> fallDetection.setValue(value);

                default -> error.setValue("Unknown sensor type: " + sensorType);
            }
        } catch (Exception e) {
            error.setValue("Error updating sensor " + sensorType + ": " + e.getMessage());
        }
    }
    // Getters for Vital Signs
    public LiveData<Double> getHeartRate() { return heartRate; }
    public LiveData<Double> getBloodOxygen() { return bloodOxygen; }
    public LiveData<Double> getBloodPressure() { return bloodPressure; }
    public LiveData<Double> getTemperature() { return temperature; }

    // Getters for Motion Sensors
    public LiveData<Double> getAccelerometer() { return accelerometer; }
    public LiveData<Double> getGyroscope() { return gyroscope; }
    public LiveData<Double> getStepCount() { return stepCount; }
    public LiveData<Double> getGravity() { return gravity; }
    public LiveData<Double> getLinearAcceleration() { return linearAcceleration; }
    public LiveData<Double> getRotation() { return rotation; }
    public LiveData<Double> getOrientation() { return orientation; }
    public LiveData<Double> getMagneticField() { return magneticField; }

    // Getters for Environment
    public LiveData<Double> getHumidity() { return humidity; }
    public LiveData<Double> getLight() { return light; }
    public LiveData<Double> getProximity() { return proximity; }

    // Getters for Samsung Health
    public LiveData<Double> getBia() { return bia; }
    public LiveData<Double> getStress() { return stress; }
    public LiveData<Double> getSleep() { return sleep; }
    public LiveData<Double> getFallDetection() { return fallDetection; }

    // Error Handling
    public LiveData<String> getError() { return error; }
}
