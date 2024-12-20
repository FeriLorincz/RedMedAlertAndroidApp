package com.feri.redmedalertandroidapp.api.validator;

import java.util.Map;

public class HealthDataValidator {

    private static final double MAX_HEART_RATE = 220.0;
    private static final double MIN_HEART_RATE = 30.0;
    private static final double MAX_TEMPERATURE = 43.0;
    private static final double MIN_TEMPERATURE = 35.0;
    private static final double MAX_OXYGEN = 100.0;
    private static final double MIN_OXYGEN = 70.0;

    public static boolean isValidData(Map<String, Double> data) {
        if (data == null || data.isEmpty()) {
            return false;
        }

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            if (!isValidValue(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidValue(String type, Double value) {
        if (value == null) return false;

        return switch (type) {
            case "heart_rate" -> value >= MIN_HEART_RATE && value <= MAX_HEART_RATE;
            case "temperature" -> value >= MIN_TEMPERATURE && value <= MAX_TEMPERATURE;
            case "blood_oxygen" -> value >= MIN_OXYGEN && value <= MAX_OXYGEN;
            default -> true; // Pentru alți senzori permitem orice valoare validă
        };
    }
}
