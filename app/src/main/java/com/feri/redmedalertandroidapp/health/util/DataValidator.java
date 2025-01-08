package com.feri.redmedalertandroidapp.health.util;

import android.util.Log;
import com.feri.redmedalertandroidapp.api.model.HealthDataEntity;
import java.util.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class DataValidator {

    private static final String TAG = "DataValidator";
    private static final long MAX_FUTURE_TIME_MS = 5000; // 5 secunde
    private static final long MAX_AGE_HOURS = 24; // 24 ore
    private static final int MAX_CONSECUTIVE_READINGS = 3;
    private static final Map<String, Range> VALID_RANGES = new HashMap<>();
    private static final Map<String, Double> MAX_CHANGES = new HashMap<>();

    static {
        // Senzori vitali
        VALID_RANGES.put("heart_rate", new Range(30.0, 220.0));
        VALID_RANGES.put("blood_oxygen", new Range(70.0, 100.0));
        VALID_RANGES.put("blood_pressure", new Range(60.0, 200.0));
        VALID_RANGES.put("body_temperature", new Range(35.0, 42.0));
        VALID_RANGES.put("bia", new Range(0.0, 100.0));

        // Senzori de mișcare
        VALID_RANGES.put("accelerometer", new Range(-20.0, 20.0));
        VALID_RANGES.put("gyroscope", new Range(-2000.0, 2000.0));
        VALID_RANGES.put("fall_detection", new Range(0.0, 1.0));

        // Alți senzori
        VALID_RANGES.put("stress", new Range(0.0, 100.0));
        VALID_RANGES.put("sleep", new Range(0.0, 1.0));

        // Schimbări maxime permise între două citiri consecutive
        MAX_CHANGES.put("heart_rate", 40.0);      // BPM
        MAX_CHANGES.put("blood_oxygen", 10.0);    // %
        MAX_CHANGES.put("blood_pressure", 40.0);  // mmHg
        MAX_CHANGES.put("body_temperature", 1.5); // °C
        MAX_CHANGES.put("stress", 30.0);         // %
    }

    private static class Range {
        final double min;
        final double max;
        Range(double min, double max) {
            this.min = min;
            this.max = max;
        }
        boolean isValid(double value) {
            return value >= min && value <= max;
        }
    }

    public static List<HealthDataEntity> validateAndFilter(List<HealthDataEntity> data) {
        List<HealthDataEntity> validData = new ArrayList<>();
        Map<String, Queue<HealthDataEntity>> recentReadings = new HashMap<>();
        Map<String, Long> lastValidTimestamp = new HashMap<>();
        long currentTime = System.currentTimeMillis();

        // Sortare după timestamp
        data.sort(Comparator.comparingLong(e -> e.timestamp));

        for (HealthDataEntity entity : data) {
            if (!isValidTimestamp(entity.timestamp, currentTime)) {
                Log.w(TAG, "Invalid timestamp for " + entity.dataType);
                continue;
            }

            if (!isValidRange(entity)) {
                Log.w(TAG, String.format("Value %f out of range for %s",
                        entity.value, entity.dataType));
                continue;
            }

            if (!isValidChangeRate(entity, recentReadings.get(entity.dataType))) {
                Log.w(TAG, "Invalid change rate for " + entity.dataType);
                continue;
            }

            // Verificare frecvență citiri
            Queue<HealthDataEntity> recent = recentReadings.computeIfAbsent(
                    entity.dataType, k -> new LinkedList<>());

            if (recent.size() >= MAX_CONSECUTIVE_READINGS) {
                recent.poll();
            }
            recent.offer(entity);

            validData.add(entity);
            lastValidTimestamp.put(entity.dataType, entity.timestamp);
        }

        Log.d(TAG, String.format("Validated %d/%d data points",
                validData.size(), data.size()));
        return validData;
    }

    private static boolean isValidTimestamp(long timestamp, long currentTime) {
        if (timestamp > currentTime + MAX_FUTURE_TIME_MS) {
            return false;
        }

        Instant readingTime = Instant.ofEpochMilli(timestamp);
        Instant cutoffTime = Instant.ofEpochMilli(currentTime)
                .minus(MAX_AGE_HOURS, ChronoUnit.HOURS);

        return !readingTime.isBefore(cutoffTime);
    }

    private static boolean isValidRange(HealthDataEntity entity) {
        Range range = VALID_RANGES.get(entity.dataType);
        return range == null || range.isValid(entity.value);
    }

    private static boolean isValidChangeRate(HealthDataEntity entity,
                                             Queue<HealthDataEntity> recentReadings) {
        if (recentReadings == null || recentReadings.isEmpty()) {
            return true;
        }

        Double maxChange = MAX_CHANGES.get(entity.dataType);
        if (maxChange == null) {
            return true;
        }

        HealthDataEntity lastReading = recentReadings.peek();
        double change = Math.abs(entity.value - lastReading.value);
        return change <= maxChange;
    }
}
