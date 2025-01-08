package com.feri.redmedalertandroidapp.health.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.feri.redmedalertandroidapp.api.model.HealthDataPayload;
import com.feri.redmedalertandroidapp.health.EnhancedFallDetector;
import com.samsung.android.sdk.healthdata.HealthDataStore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SensorDataCollector implements SensorEventListener {

    private static final String TAG = "SensorDataCollector";
    private final SensorManager sensorManager;
    private final Map<String, Sensor> activeSensors;
    private final Map<String, Double> latestSensorData;
    private final SensorDataFormatter dataFormatter;
    private final EnhancedFallDetector fallDetector;
    private DataCollectionCallback callback;

    public interface DataCollectionCallback {
        void onDataCollected(Map<String, Double> data);
        void onCollectionError(String error);
    }

    public SensorDataCollector(Context context, HealthDataStore healthDataStore) {
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.activeSensors = new HashMap<>();
        this.latestSensorData = new ConcurrentHashMap<>();
        this.dataFormatter = new SensorDataFormatter();
        this.fallDetector = new EnhancedFallDetector();
        initializeSensors();
    }

    private void initializeSensors() {
        /// Senzori vitali de bază
        registerSensor(Sensor.TYPE_HEART_RATE, "heart_rate");
        registerSensor(Sensor.TYPE_STEP_COUNTER, "step_count");
        registerSensor(Sensor.TYPE_PRESSURE, "blood_pressure");
        registerSensor(Sensor.TYPE_AMBIENT_TEMPERATURE, "body_temperature");

        // Senzori de mișcare și orientare
        registerSensor(Sensor.TYPE_ACCELEROMETER, "accelerometer");
        registerSensor(Sensor.TYPE_GYROSCOPE, "gyroscope");
        registerSensor(Sensor.TYPE_GRAVITY, "gravity");
        registerSensor(Sensor.TYPE_LINEAR_ACCELERATION, "linear_acceleration");
        registerSensor(Sensor.TYPE_ROTATION_VECTOR, "rotation");
        registerSensor(Sensor.TYPE_GAME_ROTATION_VECTOR, "orientation");
        registerSensor(Sensor.TYPE_MAGNETIC_FIELD, "magnetic_field");

        // Senzori de mediu și poziție
        registerSensor(Sensor.TYPE_RELATIVE_HUMIDITY, "humidity");
        registerSensor(Sensor.TYPE_LIGHT, "light");
        registerSensor(Sensor.TYPE_PROXIMITY, "proximity");

        // Senzori specifici Samsung Health
        // Aceștia sunt gestionați prin HealthDataStore
        setupSamsungHealthSensors();
    }

    private void setupSamsungHealthSensors() {
        // Bioelectrical Impedance
        registerSamsungSensor("com.samsung.health.bioelectrical_impedance", "bia");

        // Blood Oxygen
        registerSamsungSensor("com.samsung.health.oxygen_saturation", "blood_oxygen");

        // Stress Level
        registerSamsungSensor("com.samsung.health.stress", "stress");

        // Sleep Monitor
        registerSamsungSensor("com.samsung.health.sleep", "sleep");

        // Fall Detection
        registerSamsungSensor("com.samsung.health.fall_detection", "fall_detection");
    }

    private void registerSamsungSensor(String type, String name) {
        // Implementare specifică pentru senzorii Samsung Health
        try {
            // Logică pentru înregistrare senzori Samsung
            Log.d(TAG, "Registered Samsung sensor: " + name);
        } catch (Exception e) {
            Log.e(TAG, "Error registering Samsung sensor " + name + ": " + e.getMessage());
        }
    }


    private String getSensorName(int sensorType) {
        return switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER -> "accelerometer";
            case Sensor.TYPE_GYROSCOPE -> "gyroscope";
            case Sensor.TYPE_HEART_RATE -> "heart_rate";
            case Sensor.TYPE_STEP_COUNTER -> "step_count";
            case Sensor.TYPE_PRESSURE -> "blood_pressure";
            case Sensor.TYPE_AMBIENT_TEMPERATURE -> "body_temperature";
            case Sensor.TYPE_GRAVITY -> "gravity";
            case Sensor.TYPE_LINEAR_ACCELERATION -> "linear_acceleration";
            case Sensor.TYPE_ROTATION_VECTOR -> "rotation";
            case Sensor.TYPE_GAME_ROTATION_VECTOR -> "orientation";
            case Sensor.TYPE_MAGNETIC_FIELD -> "magnetic_field";
            case Sensor.TYPE_RELATIVE_HUMIDITY -> "humidity";
            case Sensor.TYPE_LIGHT -> "light";
            case Sensor.TYPE_PROXIMITY -> "proximity";
            default -> "unknown";
        };
    }

    private double processSensorData(SensorEvent event) {
        return switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE,
                 Sensor.TYPE_LINEAR_ACCELERATION, Sensor.TYPE_GRAVITY ->
                    Math.sqrt(event.values[0] * event.values[0] +
                            event.values[1] * event.values[1] +
                            event.values[2] * event.values[2]);
            case Sensor.TYPE_HEART_RATE, Sensor.TYPE_STEP_COUNTER,
                 Sensor.TYPE_PRESSURE, Sensor.TYPE_AMBIENT_TEMPERATURE,
                 Sensor.TYPE_RELATIVE_HUMIDITY, Sensor.TYPE_LIGHT,
                 Sensor.TYPE_PROXIMITY -> event.values[0];
            case Sensor.TYPE_ROTATION_VECTOR, Sensor.TYPE_ORIENTATION ->
                    Math.sqrt(event.values[0] * event.values[0] +
                            event.values[1] * event.values[1] +
                            event.values[2] * event.values[2] +
                            (event.values.length > 3 ? event.values[3] * event.values[3] : 0));
            default -> 0.0;
        };
    }



    private void registerSensor(int sensorType, String sensorName) {
        Sensor sensor = sensorManager.getDefaultSensor(sensorType);
        if (sensor != null) {
            activeSensors.put(sensorName, sensor);
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.w(TAG, "Sensor not available: " + sensorName);
        }
    }

    public void startCollecting(DataCollectionCallback callback) {
        this.callback = callback;
        for (Sensor sensor : activeSensors.values()) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void stopCollecting() {
        sensorManager.unregisterListener(this);
        latestSensorData.clear();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            String sensorName = getSensorName(event.sensor.getType());
            double value = processSensorData(event);
            latestSensorData.put(sensorName, value);

            // Verificare fall detection pentru accelerometru și giroscop
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER ||
                    event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                processFallDetection(event);
            }

            // Notificare callback cu datele actualizate
            if (callback != null) {
                Map<String, Double> formattedData = dataFormatter.formatData(latestSensorData);
                callback.onDataCollected(formattedData);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing sensor data: " + e.getMessage());
            if (callback != null) {
                callback.onCollectionError("Error processing sensor data: " + e.getMessage());
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "Accuracy changed for sensor: " + sensor.getName() + " to: " + accuracy);
    }

    private void processFallDetection(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            Double[] accData = {
                    (double) event.values[0],
                    (double) event.values[1],
                    (double) event.values[2]
            };
            fallDetector.processMotionData(accData, null, System.currentTimeMillis());
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            Double[] gyroData = {
                    (double) event.values[0],
                    (double) event.values[1],
                    (double) event.values[2]
            };
            fallDetector.processMotionData(null, gyroData, System.currentTimeMillis());
        }
    }

    public Map<String, Double> getLatestData() {
        return new HashMap<>(latestSensorData);
    }

    public boolean isSensorAvailable(String sensorName) {
        return activeSensors.containsKey(sensorName);
    }
}
