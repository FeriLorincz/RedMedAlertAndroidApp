package com.feri.redmedalertandroidapp.health;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class FallDetector {

    private static final String TAG = "FallDetector";

    // Praguri pentru detecția căderilor
    private static final double IMPACT_THRESHOLD = 4.0;   // Accelerație la impact
    private static final double ORIENTATION_CHANGE_THRESHOLD = 45.0; // Grade
    private static final long IMPACT_WINDOW_MS = 500;     // Fereastră de timp pentru impact
    private static final long ORIENTATION_WINDOW_MS = 1000; // Fereastră de timp pentru schimbare orientare
    private static final double MIN_INACTIVITY_THRESHOLD = 0.8; // Prag minim pentru inactivitate
    private static final double MAX_INACTIVITY_THRESHOLD = 1.5; // Prag maxim pentru inactivitate
    private static final int MIN_SAMPLES_FOR_VALIDATION = 3; // Minim de sample-uri pentru validare

    // Constante pentru exercise
    private static final String EXERCISE_TYPE_RUNNING = "running";
    private static final String EXERCISE_TYPE_WALKING = "walking";
    private static final String EXERCISE_TYPE_WORKOUT = "workout";

    // Contoare pentru validare
    private int falsePositiveCount = 0;
    private long lastFalsePositiveTime = 0;
    private static final long FALSE_POSITIVE_RESET_TIME = 10000; // 10 secunde

    // Stare exercițiu
    private boolean isExercising = false;
    private String currentExerciseType = null;
    private double exerciseIntensity = 0.0;

    // Stare curentă
    private double lastAccMagnitude = 0;
    private long lastImpactTime = 0;
    private final List<Double[]> recentAccData = new ArrayList<>();
    private final List<Double[]> recentGyroData = new ArrayList<>();
    private boolean potentialFallDetected = false;

    public interface FallDetectionListener {
        void onFallDetected();
        void onPotentialFall();
        void onFallRejected();
    }

    private FallDetectionListener listener;

    public void setListener(FallDetectionListener listener) {
        this.listener = listener;
    }

    // Metoda pentru actualizarea stării exercițiului
    public void updateExerciseState(String exerciseType, double intensity) {
        this.isExercising = exerciseType != null;
        this.currentExerciseType = exerciseType;
        this.exerciseIntensity = intensity;
    }

    public void processMotionData(Double[] accData, Double[] gyroData, long timestamp) {
        // Reset false positive counter după timeout
        if (timestamp - lastFalsePositiveTime > FALSE_POSITIVE_RESET_TIME) {
            falsePositiveCount = 0;
        }

        // Calculăm magnitudinile
        double accMagnitude = calculateMagnitude(accData);
        double gyroMagnitude = calculateMagnitude(gyroData);

        // Actualizăm bufferele
        updateDataBuffers(accData, gyroData);

        // Detectăm impactul
        if (detectImpact(accMagnitude, timestamp)) {
            processImpactDetection(gyroMagnitude, timestamp);
        }
    }

    private double calculateMagnitude(Double[] data) {
        return Math.sqrt(data[0] * data[0] + data[1] * data[1] + data[2] * data[2]);
    }

    private boolean detectImpact(double accMagnitude, long timestamp) {
        if (accMagnitude > IMPACT_THRESHOLD && !potentialFallDetected) {
            // Verificăm exercițiul
            if (isExercising) {
                double adjustedThreshold = IMPACT_THRESHOLD * (1 + exerciseIntensity);
                if (accMagnitude <= adjustedThreshold) {
                    return false;
                }

                if (EXERCISE_TYPE_RUNNING.equals(currentExerciseType) ||
                        EXERCISE_TYPE_WORKOUT.equals(currentExerciseType)) {
                    return validateExerciseImpact(accMagnitude);
                }
            }

            // Verificăm false positives
            if (falsePositiveCount > 3) {
                return false;
            }

            lastImpactTime = timestamp;
            potentialFallDetected = true;
            if (listener != null) {
                listener.onPotentialFall();
            }
            return true;
        }
        return false;
    }

    private void processImpactDetection(double gyroMagnitude, long timestamp) {
        if (potentialFallDetected && (timestamp - lastImpactTime) < ORIENTATION_WINDOW_MS) {
            if (gyroMagnitude > ORIENTATION_CHANGE_THRESHOLD) {
                if (validateFallPattern()) {
                    if (listener != null) {
                        listener.onFallDetected();
                        falsePositiveCount = 0;
                    }
                    resetDetection();
                }
            }
        } else if (potentialFallDetected) {
            if (listener != null) {
                listener.onFallRejected();
                falsePositiveCount++;
                lastFalsePositiveTime = timestamp;
            }
            resetDetection();
        }
    }

    private boolean validateFallPattern() {
        if (recentAccData.size() < MIN_SAMPLES_FOR_VALIDATION) return false;

        boolean hasImpact = validateImpact();
        boolean hasRotation = validateRotation();
        boolean hasInactivity = checkPostFallInactivity();

        if (isExercising) {
            return hasImpact && hasRotation && hasInactivity &&
                    validateExerciseFallPattern();
        }

        return hasImpact && hasRotation && hasInactivity;
    }

    private boolean validateImpact() {
        int impactCount = 0;
        for (Double[] acc : recentAccData) {
            if (calculateMagnitude(acc) > IMPACT_THRESHOLD) {
                impactCount++;
            }
        }
        return impactCount >= MIN_SAMPLES_FOR_VALIDATION;
    }

    private boolean validateRotation() {
        int rotationCount = 0;
        for (Double[] gyro : recentGyroData) {
            if (calculateMagnitude(gyro) > ORIENTATION_CHANGE_THRESHOLD) {
                rotationCount++;
            }
        }
        return rotationCount >= MIN_SAMPLES_FOR_VALIDATION;
    }

    private boolean validateExerciseImpact(double accMagnitude) {
        int recentHighImpacts = 0;
        for (Double[] acc : recentAccData) {
            if (calculateMagnitude(acc) > IMPACT_THRESHOLD) {
                recentHighImpacts++;
            }
        }
        return recentHighImpacts < 3;
    }

    private boolean validateExerciseFallPattern() {
        double maxRotation = recentGyroData.stream()
                .mapToDouble(this::calculateMagnitude)
                .max()
                .orElse(0.0);

        double postImpactActivity = calculateAverageMagnitude(
                recentAccData.subList(
                        Math.max(0, recentAccData.size() - 5),
                        recentAccData.size()
                )
        );

        return maxRotation > ORIENTATION_CHANGE_THRESHOLD * 1.5 &&
                postImpactActivity < MIN_INACTIVITY_THRESHOLD;
    }

    private boolean checkPostFallInactivity() {
        if (recentAccData.size() < MIN_SAMPLES_FOR_VALIDATION) return false;

        double avgMagnitude = calculateAverageMagnitude(
                recentAccData.subList(recentAccData.size() - MIN_SAMPLES_FOR_VALIDATION,
                        recentAccData.size())
        );

        return avgMagnitude >= MIN_INACTIVITY_THRESHOLD &&
                avgMagnitude <= MAX_INACTIVITY_THRESHOLD;
    }

    private double calculateAverageMagnitude(List<Double[]> data) {
        return data.stream()
                .mapToDouble(this::calculateMagnitude)
                .average()
                .orElse(0.0);
    }

    private void updateDataBuffers(Double[] accData, Double[] gyroData) {
        recentAccData.add(accData);
        recentGyroData.add(gyroData);

        while (recentAccData.size() > 20) {
            recentAccData.remove(0);
        }
        while (recentGyroData.size() > 20) {
            recentGyroData.remove(0);
        }
    }

    private void resetDetection() {
        potentialFallDetected = false;
        lastImpactTime = 0;
        recentAccData.clear();
        recentGyroData.clear();
    }
}
