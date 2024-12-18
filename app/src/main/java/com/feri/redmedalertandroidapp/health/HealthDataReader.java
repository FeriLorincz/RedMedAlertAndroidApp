package com.feri.redmedalertandroidapp.health;

import android.util.Log;

import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthData;
import com.samsung.android.sdk.healthdata.HealthDataResolver;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import com.samsung.android.sdk.healthdata.HealthDataResolver.ReadRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HealthDataReader {

    private static final String TAG = "HealthDataReader";
    private final HealthDataStore healthDataStore;
    private final HealthDataResolver resolver;

    public HealthDataReader(HealthDataStore healthDataStore) {
        this.healthDataStore = healthDataStore;
        this.resolver = new HealthDataResolver(healthDataStore, null);
    }

    public interface HealthDataListener {
        void onDataReceived(Map<String, Double> data);
        void onDataReadError(String message);
    }

    public void readLatestData(HealthDataListener listener) {
        // Timestamp pentru ultimele 5 minute
        long endTime = System.currentTimeMillis();
        long startTime = endTime - TimeUnit.MINUTES.toMillis(5);

        // Map pentru a stoca datele
        Map<String, Double> latestData = new HashMap<>();

        // Citire pentru fiecare tip de date
        readHeartRate(startTime, endTime, latestData, listener);
        readBloodOxygen(startTime, endTime, latestData, listener);
        readBloodPressure(startTime, endTime, latestData, listener);
        readTemperature(startTime, endTime, latestData, listener);
        readSleep(startTime, endTime, latestData, listener);
    }

    private final StressCalculator stressCalculator = new StressCalculator();

        // Citim datele pentru puls (HeartRate)
        private void readHeartRate(long startTime, long endTime, Map<String, Double> latestData, HealthDataListener listener) {
            ReadRequest heartRateRequest = new ReadRequest.Builder()
                    .setDataType(HealthConstants.HeartRate.HEALTH_DATA_TYPE)
                    .setProperties(new String[] {
                            HealthConstants.HeartRate.HEART_RATE
                    })
                    .setLocalTimeRange(HealthConstants.HeartRate.START_TIME,
                            HealthConstants.HeartRate.TIME_OFFSET,
                            startTime, endTime)
                    .build();

            try {
                resolver.read(heartRateRequest).setResultListener(result -> {
                    try {
                        for (HealthData data : result) {
                            double heartRate = data.getDouble(HealthConstants.HeartRate.HEART_RATE);
                            stressCalculator.addHeartRateMeasurement(heartRate);
                        }

                        // Calculăm nivelul de stress
                        double stressLevel = stressCalculator.calculateStressLevel();
                        if (stressLevel >= 0) {
                            latestData.put("stress_level", stressLevel);
                            String stressCategory = stressCalculator.getStressCategory(stressLevel);
                            Log.d(TAG, "Current stress level: " + stressLevel + " (" + stressCategory + ")");
                        }

                        listener.onDataReceived(latestData);
                    } finally {
                        result.close();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error reading heart rate data: " + e.getMessage());
                listener.onDataReadError("Failed to read heart rate data");
            }
        }

        // Citim datele pentru oxigen din sânge
        private void readBloodOxygen(long startTime, long endTime, Map<String, Double> latestData, HealthDataListener listener) {
            ReadRequest spO2Request = new ReadRequest.Builder()
                    .setDataType("com.samsung.health.oxygen_saturation")
                    .setProperties(new String[] {
                            "oxygen_saturation"
                    })
                    .setLocalTimeRange("start_time",
                            "time_offset",
                            startTime, endTime)
                    .build();

            try {
                resolver.read(spO2Request).setResultListener(result -> {
                    try {
                        for (HealthData data : result) {
                            latestData.put("blood_oxygen",
                                    data.getDouble("oxygen_saturation"));
                            break;
                        }
                    } finally {
                        result.close();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error reading blood oxygen data: " + e.getMessage());
                listener.onDataReadError("Failed to read blood oxygen data");
            }
        }

    private void readBloodPressure(long startTime, long endTime, Map<String, Double> latestData, HealthDataListener listener) {
        ReadRequest bpRequest = new ReadRequest.Builder()
                .setDataType("com.samsung.health.blood_pressure")
                .setProperties(new String[] {
                        "systolic",
                        "diastolic"
                })
                .setLocalTimeRange("start_time",
                        "time_offset",
                        startTime, endTime)
                .build();

        try {
            resolver.read(bpRequest).setResultListener(result -> {
                try {
                    for (HealthData data : result) {
                        double systolic = data.getDouble("systolic");
                        double diastolic = data.getDouble("diastolic");
                        latestData.put("blood_pressure_systolic", systolic);
                        latestData.put("blood_pressure_diastolic", diastolic);
                        break; // Luăm doar ultima măsurătoare
                    }
                } finally {
                    result.close();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error reading blood pressure data: " + e.getMessage());
            listener.onDataReadError("Failed to read blood pressure data");
        }
    }

    private void readTemperature(long startTime, long endTime, Map<String, Double> latestData, HealthDataListener listener) {
        ReadRequest tempRequest = new ReadRequest.Builder()
                .setDataType("com.samsung.health.temperature")
                .setProperties(new String[] {
                        "temperature"
                })
                .setLocalTimeRange("start_time",
                        "time_offset",
                        startTime, endTime)
                .build();

        try {
            resolver.read(tempRequest).setResultListener(result -> {
                try {
                    for (HealthData data : result) {
                        latestData.put("temperature", data.getDouble("temperature"));
                        break;
                    }
                } finally {
                    result.close();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error reading temperature data: " + e.getMessage());
            listener.onDataReadError("Failed to read temperature data");
        }
    }

    private void readSleep(long startTime, long endTime, Map<String, Double> latestData, HealthDataListener listener) {
        ReadRequest sleepRequest = new ReadRequest.Builder()
                .setDataType("com.samsung.health.sleep")
                .setProperties(new String[] {
                        "sleep_stage"
                })
                .setLocalTimeRange("start_time",
                        "time_offset",
                        startTime, endTime)
                .build();

        try {
            resolver.read(sleepRequest).setResultListener(result -> {
                try {
                    for (HealthData data : result) {
                        latestData.put("sleep_stage", (double)data.getInt("sleep_stage"));
                        break;
                    }
                } finally {
                    result.close();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error reading sleep data: " + e.getMessage());
            listener.onDataReadError("Failed to read sleep data");
        }
    }


    private void readExerciseData(long startTime, long endTime, Map<String, Double> latestData) {
        ReadRequest exerciseRequest = new ReadRequest.Builder()
                .setDataType("com.samsung.health.exercise")
                .setProperties(new String[] {
                        "exercise_type",
                        "exercise_intensity",
                        "duration"
                })
                .setLocalTimeRange("start_time",
                        "time_offset",
                        startTime, endTime)
                .build();

        try {
            resolver.read(exerciseRequest).setResultListener(result -> {
                try {
                    for (HealthData data : result) {
                        String exerciseType = data.getString("exercise_type");
                        double intensity = data.getFloat("exercise_intensity");
                        latestData.put("exercise_type", (double) exerciseType.hashCode());
                        latestData.put("exercise_intensity", intensity);
                        break;
                    }
                } finally {
                    result.close();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error reading exercise data: " + e.getMessage());
        }
    }
}

