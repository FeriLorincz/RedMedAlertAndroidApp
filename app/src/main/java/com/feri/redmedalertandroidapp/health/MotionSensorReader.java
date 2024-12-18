package com.feri.redmedalertandroidapp.health;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class MotionSensorReader implements SensorEventListener {

    private static final String TAG = "MotionSensorReader";
    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private final Sensor gyroscope;
    private final Map<String, Double[]> latestMotionData = new HashMap<>();
    private MotionDataListener listener;
    private final FallDetector fallDetector;
    private long lastUpdateTime = 0;

    public interface MotionDataListener {
        void onMotionDataReceived(Map<String, Double[]> data);
        void onMotionError(String message);
    }

    public MotionSensorReader(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        fallDetector = new FallDetector();

        if (accelerometer == null || gyroscope == null) {
            Log.e(TAG, "Motion sensors not available on this device");
        }
    }

    public void setFallDetectionListener(FallDetector.FallDetectionListener listener) {
        fallDetector.setListener(listener);
    }

    public void startReading(MotionDataListener listener) {
        this.listener = listener;
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (gyroscope != null) {
            sensorManager.registerListener(this, gyroscope,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void stopReading() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        long currentTime = System.currentTimeMillis();

        // Limităm rata de procesare la ~10Hz
        if (currentTime - lastUpdateTime < 100) {
            return;
        }
        lastUpdateTime = currentTime;

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            Double[] values = new Double[3];
            values[0] = (double) event.values[0]; // X axis
            values[1] = (double) event.values[1]; // Y axis
            values[2] = (double) event.values[2]; // Z axis
            latestMotionData.put("accelerometer", values);
        }
        else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            Double[] values = new Double[3];
            values[0] = (double) event.values[0]; // Angular speed around X
            values[1] = (double) event.values[1]; // Angular speed around Y
            values[2] = (double) event.values[2]; // Angular speed around Z
            latestMotionData.put("gyroscope", values);
        }

        if (latestMotionData.containsKey("accelerometer") &&
                latestMotionData.containsKey("gyroscope")) {
            // Procesăm datele pentru detecția căderilor
            fallDetector.processMotionData(
                    latestMotionData.get("accelerometer"),
                    latestMotionData.get("gyroscope"),
                    currentTime
            );

            if (listener != null) {
                listener.onMotionDataReceived(new HashMap<>(latestMotionData));
            }
            latestMotionData.clear();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "Accuracy changed for sensor: " + sensor.getName() + " to: " + accuracy);
    }
}
