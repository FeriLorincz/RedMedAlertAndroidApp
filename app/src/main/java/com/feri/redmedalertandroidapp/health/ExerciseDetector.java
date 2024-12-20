package com.feri.redmedalertandroidapp.health;

import java.util.Map;

public class ExerciseDetector {

    private static final String TAG = "ExerciseDetector";
    private static final double EXERCISE_HEART_RATE_THRESHOLD = 100.0;
    private static final double EXERCISE_MOVEMENT_THRESHOLD = 2.0;

    private boolean isInExercise = false;
    private ExerciseType currentExerciseType = ExerciseType.NONE;

    public enum ExerciseType {
        NONE,
        WALKING,
        RUNNING,
        WORKOUT
    }

    public void processData(Map<String, Double> sensorData) {
        double heartRate = sensorData.getOrDefault("heart_rate", 0.0);
        double movement = calculateMovementIntensity(sensorData);

        // Detect exercise state
        if (heartRate > EXERCISE_HEART_RATE_THRESHOLD && movement > EXERCISE_MOVEMENT_THRESHOLD) {
            isInExercise = true;
            currentExerciseType = determineExerciseType(heartRate, movement);
        } else if (heartRate < EXERCISE_HEART_RATE_THRESHOLD - 10) { // Hysteresis
            isInExercise = false;
            currentExerciseType = ExerciseType.NONE;
        }
    }

    private double calculateMovementIntensity(Map<String, Double> sensorData) {
        // Verificăm dacă avem componentele individuale ale accelerometrului
        Double accX = sensorData.getOrDefault("accelerometer_x", 0.0);
        Double accY = sensorData.getOrDefault("accelerometer_y", 0.0);
        Double accZ = sensorData.getOrDefault("accelerometer_z", 0.0);

        return Math.sqrt(accX * accX + accY * accY + accZ * accZ);
    }

    private ExerciseType determineExerciseType(double heartRate, double movement) {
        if (movement > 4.0) return ExerciseType.RUNNING;
        if (movement > 2.0) return ExerciseType.WALKING;
        return ExerciseType.WORKOUT;
    }

    public boolean isExercising() {
        return isInExercise;
    }

    public ExerciseType getCurrentExerciseType() {
        return currentExerciseType;
    }

    public double getExerciseIntensity() {
        return isInExercise ?
                (currentExerciseType == ExerciseType.RUNNING ? 1.0 :
                        currentExerciseType == ExerciseType.WALKING ? 0.6 : 0.8)
                : 0.0;
    }
}
