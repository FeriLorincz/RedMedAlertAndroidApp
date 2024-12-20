package com.feri.redmedalertandroidapp.health;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

public class EnhancedFallDetector extends FallDetector {

    private final ExerciseDetector exerciseDetector;
    private double sensitivityFactor = 1.0;
    private static final double BASE_IMPACT_THRESHOLD = 3.0;
    private double currentImpactThreshold = BASE_IMPACT_THRESHOLD;
    private double currentOrientationThreshold = 90.0; // valoare implicită
    private Double currentHeartRate = 0.0;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public EnhancedFallDetector() {
        this.exerciseDetector = new ExerciseDetector();
    }

    protected double getCurrentHeartRate() {
        return currentHeartRate;
    }

    public void updateHeartRate(double heartRate) {
        this.currentHeartRate = heartRate;
    }

    protected void setImpactThreshold(double threshold) {
        this.currentImpactThreshold = threshold;
    }

    protected double getImpactThreshold() {
        return currentImpactThreshold;
    }

    protected void setOrientationChangeThreshold(double threshold) {
        this.currentOrientationThreshold = threshold;
    }

    protected double getOrientationChangeThreshold() {
        return currentOrientationThreshold;
    }

    @Override
    public void processMotionData(Double[] accData, Double[] gyroData, long timestamp) {
        // First check exercise state
        Map<String, Double> sensorData = new HashMap<>();
        sensorData.put("heart_rate", getCurrentHeartRate());
        sensorData.put("accelerometer_x", accData[0]);
        sensorData.put("accelerometer_y", accData[1]);
        sensorData.put("accelerometer_z", accData[2]);
        exerciseDetector.processData(sensorData);

        if (exerciseDetector.isExercising()) {
            double exerciseIntensity = exerciseDetector.getExerciseIntensity();
            adjustThresholdsForExercise(exerciseIntensity);
        }

        double adjustedImpactThreshold = currentImpactThreshold * sensitivityFactor;
        super.processMotionData(accData, gyroData, timestamp);
    }

    private void adjustThresholdsForExercise(double exerciseIntensity) {
        setImpactThreshold(BASE_IMPACT_THRESHOLD * (1 + exerciseIntensity));
        setOrientationChangeThreshold(getOrientationChangeThreshold() * (1 + exerciseIntensity));
    }

    public void setSensitivity(double sensitivity) {
        // sensitivity: 0.5 (less sensitive) to 2.0 (more sensitive)
        this.sensitivityFactor = Math.max(0.5, Math.min(2.0, sensitivity));
    }

    public void calibrate() {
        startCalibration();
    }

    private void startCalibration() {
        executor.execute(() -> {
            try {
                // Collect baseline measurements for 30 seconds
                double sensitivity = calculateOptimalSensitivity();

                // Switch to main thread to update UI
                mainHandler.post(() -> setSensitivity(sensitivity));
            } catch (Exception e) {
                Log.e("EnhancedFallDetector", "Calibration failed", e);
            }
        });
    }

    private double calculateOptimalSensitivity() {
        // Logică de calibrare
        // Simulăm o măsurătoare care durează 30 secunde
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return 1.0;
    }

    public void cleanup() {
        executor.shutdown();
    }
}

