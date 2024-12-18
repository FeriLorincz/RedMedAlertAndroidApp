package com.feri.redmedalertandroidapp.health;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SensorDataSimulator {

    private static final String TAG = "SensorDataSimulator";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private SimulatorCallback callback;
    private boolean isRunning = false;

    // Parametri de bază
    private static final int UPDATE_INTERVAL_MS = 1000;
    private double baseHeartRate = 70.0;
    private double baseOxygenLevel = 98.0;
    private double baseTemperature = 36.6;
    private double baseSystolic = 120.0;
    private double baseDiastolic = 80.0;

    // Niveluri de activitate
    private static final int ACTIVITY_REST = 0;
    private static final int ACTIVITY_NORMAL = 1;
    private static final int ACTIVITY_EXERCISE = 2;
    private int activityLevel = ACTIVITY_REST;

    public interface SimulatorCallback {
        void onDataGenerated(Map<String, Double> data);
    }

    public void start(SimulatorCallback callback) {
        this.callback = callback;
        isRunning = true;
        Log.d(TAG, "Simulator started");
        scheduleNextUpdate();
    }

    public void stop() {
        isRunning = false;
        handler.removeCallbacksAndMessages(null);
        Log.d(TAG, "Simulator stopped");
    }

    public void setActivityLevel(int level) {
        this.activityLevel = Math.min(Math.max(level, 0), 2);
        Log.d(TAG, "Activity level set to: " + activityLevel);
    }

    private void scheduleNextUpdate() {
        if (!isRunning) return;

        handler.postDelayed(() -> {
            generateAndSendData();
            scheduleNextUpdate();
        }, UPDATE_INTERVAL_MS);
    }

    private void generateAndSendData() {
        Map<String, Double> data = new HashMap<>();

        // Calculăm factorul de activitate bazat pe nivelul curent
        double activityFactor = switch (activityLevel) {
            case ACTIVITY_NORMAL -> 1.2;
            case ACTIVITY_EXERCISE -> 1.5;
            default -> 1.0;
        };

        // Simulare heart rate cu ajustări bazate pe activitate
        double heartRateVariation = random.nextGaussian() * 2.0;
        double activityBonus = activityLevel * 15.0;
        double heartRate = baseHeartRate + heartRateVariation + activityBonus;
        heartRate = Math.min(180, Math.max(50, heartRate));
        data.put("heart_rate", heartRate);

        // Simulare oxygen cu ajustări pentru activitate
        double oxygenVariation = random.nextGaussian() * 0.5;
        double activityPenalty = activityLevel * -0.5;
        double oxygen = baseOxygenLevel + oxygenVariation + activityPenalty;
        oxygen = Math.min(100, Math.max(90, oxygen));
        data.put("blood_oxygen", oxygen);

        // Simulare temperature cu creștere ușoară în timpul activității
        double tempVariation = random.nextGaussian() * 0.1;
        double activityTemp = baseTemperature + ((activityFactor - 1) * 0.5);
        double temperature = activityTemp + tempVariation;
        temperature = Math.min(37.5, Math.max(36.0, temperature));
        data.put("temperature", temperature);

        // Simulare blood pressure cu corelație cu activitatea
        double systolicVariation = random.nextGaussian() * 5;
        double diastolicVariation = random.nextGaussian() * 3;
        double activitySystolic = baseSystolic * activityFactor;
        double activityDiastolic = baseDiastolic * Math.sqrt(activityFactor);

        double systolic = Math.min(180, Math.max(90, activitySystolic + systolicVariation));
        double diastolic = Math.min(100, Math.max(60, activityDiastolic + diastolicVariation));

        data.put("blood_pressure_systolic", systolic);
        data.put("blood_pressure_diastolic", diastolic);

        // Adăugăm nivelul de activitate
        data.put("activity_level", (double) activityLevel);

        if (callback != null) {
            callback.onDataGenerated(data);
            logGeneratedData(data);
        }
    }

    private void logGeneratedData(Map<String, Double> data) {
        StringBuilder log = new StringBuilder("Generated sensor data:\n");
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            log.append(entry.getKey()).append(": ")
                    .append(String.format("%.1f", entry.getValue())).append("\n");
        }
        Log.d(TAG, log.toString());
    }

    // Metode pentru simularea condițiilor anormale
    public void simulateAbnormalCondition(String condition) {
        switch (condition) {
            case "high_heart_rate":
                baseHeartRate = 100.0;
                break;
            case "low_oxygen":
                baseOxygenLevel = 92.0;
                break;
            case "fever":
                baseTemperature = 38.5;
                break;
            case "hypertension":
                baseSystolic = 150.0;
                baseDiastolic = 95.0;
                break;
            default:
                resetToNormal();
        }
        Log.d(TAG, "Simulating condition: " + condition);
    }

    public void resetToNormal() {
        baseHeartRate = 70.0;
        baseOxygenLevel = 98.0;
        baseTemperature = 36.6;
        baseSystolic = 120.0;
        baseDiastolic = 80.0;
        activityLevel = ACTIVITY_REST;
        Log.d(TAG, "Reset to normal values");
    }
}
