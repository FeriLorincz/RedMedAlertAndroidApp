package com.feri.redmedalertandroidapp.health.sensor;

import android.util.Log;
import java.util.HashMap;
import java.util.Map;

public class SensorDataFormatter {

    private static final String TAG = "SensorDataFormatter";

    // Constante pentru validare
    private static final double MIN_HEART_RATE = 30.0;
    private static final double MAX_HEART_RATE = 220.0;
    private static final double MIN_BLOOD_OXYGEN = 70.0;
    private static final double MAX_BLOOD_OXYGEN = 100.0;
    private static final double MIN_TEMPERATURE = 35.0;
    private static final double MAX_TEMPERATURE = 42.0;
    private static final double MIN_BLOOD_PRESSURE_SYSTOLIC = 70.0;
    private static final double MAX_BLOOD_PRESSURE_SYSTOLIC = 200.0;
    private static final double MIN_BLOOD_PRESSURE_DIASTOLIC = 40.0;
    private static final double MAX_BLOOD_PRESSURE_DIASTOLIC = 130.0;

    // Constante pentru senzori de mișcare
    private static final double MAX_ACCELERATION = 20.0; // în g
    private static final double MAX_GYROSCOPE = 2000.0; // în grade/secundă
    private static final double MAX_LINEAR_ACCELERATION = 20.0;
    private static final double MAX_ROTATION = 360.0;

    // Constante pentru alți senzori
    private static final double MIN_STRESS = 0.0;
    private static final double MAX_STRESS = 100.0;
    private static final double MIN_BIA = 0.0;
    private static final double MAX_BIA = 100.0;

    private Double formatValue(String sensorType, Double value) {
        return switch (sensorType.toLowerCase()) {
            // Senzori vitali - o zecimală
            case "heart_rate", "blood_pressure_systolic", "blood_pressure_diastolic" ->
                    Math.round(value * 10.0) / 10.0;

            // Senzori cu două zecimale
            case "blood_oxygen", "temperature", "stress", "bia" ->
                    Math.round(value * 100.0) / 100.0;

            // Senzori de mișcare - trei zecimale
            case "accelerometer", "gyroscope", "linear_acceleration",
                 "gravity", "rotation", "orientation" ->
                    Math.round(value * 1000.0) / 1000.0;

            // Senzori întregi
            case "step_count" -> Math.round(value * 1.0) / 1.0;

            default -> value;
        };
    }

    private boolean isValidValue(String sensorType, Double value) {
        return switch (sensorType.toLowerCase()) {
            case "heart_rate" ->
                    value >= MIN_HEART_RATE && value <= MAX_HEART_RATE;
            case "blood_oxygen" ->
                    value >= MIN_BLOOD_OXYGEN && value <= MAX_BLOOD_OXYGEN;
            case "temperature" ->
                    value >= MIN_TEMPERATURE && value <= MAX_TEMPERATURE;
            case "blood_pressure_systolic" ->
                    value >= MIN_BLOOD_PRESSURE_SYSTOLIC && value <= MAX_BLOOD_PRESSURE_SYSTOLIC;
            case "blood_pressure_diastolic" ->
                    value >= MIN_BLOOD_PRESSURE_DIASTOLIC && value <= MAX_BLOOD_PRESSURE_DIASTOLIC;
            case "accelerometer", "linear_acceleration", "gravity" ->
                    value >= -MAX_ACCELERATION && value <= MAX_ACCELERATION;
            case "gyroscope" ->
                    value >= -MAX_GYROSCOPE && value <= MAX_GYROSCOPE;
            case "rotation", "orientation" ->
                    value >= 0 && value <= MAX_ROTATION;
            case "stress" ->
                    value >= MIN_STRESS && value <= MAX_STRESS;
            case "bia" ->
                    value >= MIN_BIA && value <= MAX_BIA;
            case "step_count" ->
                    value >= 0;
            default -> true;
        };
    }

    public String getFormattedString(String sensorType, Double value) {
        if (value == null) {
            return "N/A";
        }

        return switch (sensorType.toLowerCase()) {
            case "heart_rate" -> String.format("%.1f BPM", value);
            case "blood_oxygen" -> String.format("%.1f%%", value);
            case "temperature" -> String.format("%.1f°C", value);
            case "blood_pressure_systolic", "blood_pressure_diastolic" ->
                    String.format("%.1f mmHg", value);
            case "accelerometer" -> String.format("%.3f g", value);
            case "gyroscope" -> String.format("%.3f °/s", value);
            case "step_count" -> String.format("%.0f steps", value);
            case "stress" -> String.format("%.1f%%", value);
            case "bia" -> String.format("%.1f%%", value);
            case "rotation", "orientation" -> String.format("%.1f°", value);
            case "linear_acceleration" -> String.format("%.3f m/s²", value);
            case "gravity" -> String.format("%.3f m/s²", value);
            default -> String.format("%.2f", value);
        };
    }

    public Map<String, String> getFormattedUnits() {
        Map<String, String> units = new HashMap<>();
        // Senzori vitali
        units.put("heart_rate", "BPM");
        units.put("blood_oxygen", "%");
        units.put("temperature", "°C");
        units.put("blood_pressure_systolic", "mmHg");
        units.put("blood_pressure_diastolic", "mmHg");

        // Senzori de mișcare
        units.put("accelerometer", "g");
        units.put("gyroscope", "°/s");
        units.put("linear_acceleration", "m/s²");
        units.put("gravity", "m/s²");
        units.put("rotation", "°");
        units.put("orientation", "°");

        // Alți senzori
        units.put("step_count", "steps");
        units.put("stress", "%");
        units.put("bia", "%");

        return units;
    }

    public Map<String, Double> formatData(Map<String, Double> rawData) {
        Map<String, Double> formattedData = new HashMap<>();

        try {
            for (Map.Entry<String, Double> entry : rawData.entrySet()) {
                String sensorType = entry.getKey();
                Double value = entry.getValue();

                if (value == null) {
                    continue;
                }

                // Formatare și validare în funcție de tipul senzorului
                Double formattedValue = formatValue(sensorType, value);
                if (formattedValue != null && isValidValue(sensorType, formattedValue)) {
                    formattedData.put(sensorType, formattedValue);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error formatting sensor data: " + e.getMessage());
        }

        return formattedData;
    }
}