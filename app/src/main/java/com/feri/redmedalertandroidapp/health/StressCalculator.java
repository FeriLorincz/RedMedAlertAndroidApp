package com.feri.redmedalertandroidapp.health;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class StressCalculator {

    private static final String TAG = "StressCalculator";
    private final List<Double> heartRateIntervals = new ArrayList<>();
    private static final int MIN_INTERVALS_FOR_HRV = 5;

    // Praguri pentru nivelul de stress
    private static final double LOW_STRESS_THRESHOLD = 30.0;
    private static final double MEDIUM_STRESS_THRESHOLD = 60.0;

    public void addHeartRateMeasurement(double heartRate) {
        if (!heartRateIntervals.isEmpty()) {
            // Calculăm intervalul RR (în millisecunde)
            double interval = 60000.0 / heartRate; // Convertim BPM în interval RR
            heartRateIntervals.add(interval);

            // Păstrăm doar ultimele 10 intervale
            if (heartRateIntervals.size() > 10) {
                heartRateIntervals.remove(0);
            }
        } else {
            // Prima măsurătoare
            double interval = 60000.0 / heartRate;
            heartRateIntervals.add(interval);
        }
    }

    public double calculateStressLevel() {
        if (heartRateIntervals.size() < MIN_INTERVALS_FOR_HRV) {
            Log.d(TAG, "Not enough intervals for HRV calculation");
            return -1;
        }

        // Calculăm RMSSD (Root Mean Square of Successive Differences)
        double sumSquaredDiff = 0;
        for (int i = 0; i < heartRateIntervals.size() - 1; i++) {
            double diff = heartRateIntervals.get(i + 1) - heartRateIntervals.get(i);
            sumSquaredDiff += diff * diff;
        }

        double rmssd = Math.sqrt(sumSquaredDiff / (heartRateIntervals.size() - 1));

        // Convertim RMSSD în nivel de stress (0-100)
        // HRV mic = stress mare, HRV mare = stress mic
        double stressLevel = Math.max(0, Math.min(100, 100 - (rmssd / 100)));

        Log.d(TAG, "Calculated stress level: " + stressLevel);
        return stressLevel;
    }

    public String getStressCategory(double stressLevel) {
        if (stressLevel < LOW_STRESS_THRESHOLD) {
            return "LOW";
        } else if (stressLevel < MEDIUM_STRESS_THRESHOLD) {
            return "MEDIUM";
        } else {
            return "HIGH";
        }
    }

    public void reset() {
        heartRateIntervals.clear();
    }
}
