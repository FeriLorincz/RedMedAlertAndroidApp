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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_test);

        healthDataText = findViewById(R.id.healthDataText);
        motionDataText = findViewById(R.id.motionDataText);
        fallDetectionText = findViewById(R.id.fallDetectionText);
        stressLevelText = findViewById(R.id.stressLevelText);

        initializeSensors();
    }

    private void initializeSensors() {
        // Inițializare Motion Sensors
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

    private void updateMotionDataDisplay(Map<String, Double[]> data) {
        runOnUiThread(() -> {
            StringBuilder sb = new StringBuilder();
            sb.append("Motion Sensor Data:\n\n");

            if (data.containsKey("accelerometer")) {
                Double[] accData = data.get("accelerometer");
                sb.append(String.format("Accelerometer:\nX: %.2f\nY: %.2f\nZ: %.2f\n\n",
                        accData[0], accData[1], accData[2]));
            }

            if (data.containsKey("gyroscope")) {
                Double[] gyroData = data.get("gyroscope");
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
    }
}
