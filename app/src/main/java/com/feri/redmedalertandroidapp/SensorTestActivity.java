package com.feri.redmedalertandroidapp;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.feri.redmedalertandroidapp.health.FallDetector;
import com.feri.redmedalertandroidapp.health.HealthDataReader;
import com.feri.redmedalertandroidapp.health.MotionSensorReader;
import com.feri.redmedalertandroidapp.health.SensorDataSimulator;
import com.samsung.android.sdk.healthdata.HealthDataStore;

import java.util.Map;

public class SensorTestActivity extends AppCompatActivity {

    private static final String TAG = "SensorTestActivity";
    private TextView healthDataText;
    private TextView motionDataText;
    private TextView fallDetectionText;
    private TextView stressLevelText;

    private HealthDataStore healthDataStore;
    private HealthDataReader healthDataReader;
    private MotionSensorReader motionSensorReader;
    private SensorDataSimulator simulator; // Added missing declaration

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_test);

        healthDataText = findViewById(R.id.healthDataText);
        motionDataText = findViewById(R.id.motionDataText);
        fallDetectionText = findViewById(R.id.fallDetectionText);
        stressLevelText = findViewById(R.id.stressLevelText);

        initializeSensors();
        initializeSimulator();
    }

    private void initializeSensors() {
        // Initialize Motion Sensors
        motionSensorReader = new MotionSensorReader(this);
        motionSensorReader.startReading(new MotionSensorReader.MotionDataListener() {
            @Override
            public void onMotionDataReceived(Map<String, Double[]> data) {
                updateMotionDataDisplay(data);
            }

            @Override
            public void onMotionError(String message) {
                motionDataText.setText("Error: " + message);
            }
        });

        // Setup Fall Detection
        motionSensorReader.setFallDetectionListener(new FallDetector.FallDetectionListener() {
            @Override
            public void onFallDetected() {
                runOnUiThread(() ->
                        fallDetectionText.setText("FALL DETECTED!")
                );
            }

            @Override
            public void onPotentialFall() {
                runOnUiThread(() ->
                        fallDetectionText.setText("Potential fall detected...")
                );
            }

            @Override
            public void onFallRejected() {
                runOnUiThread(() ->
                        fallDetectionText.setText("Fall rejected")
                );
            }
        });
    }

    private void initializeSimulator() {
        simulator = new SensorDataSimulator();
        simulator.start(data -> {
            runOnUiThread(() -> {
                StringBuilder sb = new StringBuilder();
                sb.append("Simulated Health Data:\n\n");

                // Using safe null checks and type conversion
                Object heartRate = data.get("heart_rate");
                if (heartRate instanceof Double) {
                    sb.append(String.format("Heart Rate: %.1f BPM\n", (Double)heartRate));
                }

                Object bloodOxygen = data.get("blood_oxygen");
                if (bloodOxygen instanceof Double) {
                    sb.append(String.format("Blood Oxygen: %.1f%%\n", (Double)bloodOxygen));
                }

                Object temperature = data.get("temperature");
                if (temperature instanceof Double) {
                    sb.append(String.format("Temperature: %.1f°C\n", (Double)temperature));
                }

                Object systolic = data.get("blood_pressure_systolic");
                Object diastolic = data.get("blood_pressure_diastolic");
                if (systolic instanceof Double && diastolic instanceof Double) {
                    sb.append(String.format("Blood Pressure: %.0f/%.0f mmHg\n",
                            (Double)systolic, (Double)diastolic));
                }

                healthDataText.setText(sb.toString());
            });
        });
    }

    private void updateMotionDataDisplay(Map<String, Double[]> data) {
        runOnUiThread(() -> {
            StringBuilder sb = new StringBuilder();
            sb.append("Motion Sensor Data:\n\n");

            Double[] accData = data.get("accelerometer");
            if (accData != null && accData.length >= 3) {
                sb.append(String.format("Accelerometer:\nX: %.2f\nY: %.2f\nZ: %.2f\n\n",
                        accData[0], accData[1], accData[2]));
            }

            Double[] gyroData = data.get("gyroscope");
            if (gyroData != null && gyroData.length >= 3) {
                sb.append(String.format("Gyroscope:\nX: %.2f\nY: %.2f\nZ: %.2f\n",
                        gyroData[0], gyroData[1], gyroData[2]));
            }

            motionDataText.setText(sb.toString());
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (motionSensorReader != null) {
            motionSensorReader.stopReading();
        }
        if (simulator != null) {
            simulator.stop();
        }
    }
}