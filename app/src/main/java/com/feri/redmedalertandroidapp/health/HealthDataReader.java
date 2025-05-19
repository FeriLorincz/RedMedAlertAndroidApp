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
        readBioactiveData(startTime, endTime, latestData, listener);
        readBiaData(startTime, endTime, latestData, listener);
        readSleep(startTime, endTime, latestData, listener);
        readBarometerData(startTime, endTime, latestData, listener);
    }

    private final StressCalculator stressCalculator = new StressCalculator();

    // Citim datele pentru puls (HeartRate)
    private void readHeartRate(long startTime, long endTime, Map<String, Double> latestData, HealthDataListener listener) {
        try {
            // Încercăm cu pachetul standard Samsung Health
            ReadRequest heartRateRequest = new ReadRequest.Builder()
                    .setDataType(HealthConstants.HeartRate.HEALTH_DATA_TYPE)
                    .setProperties(new String[] {
                            HealthConstants.HeartRate.HEART_RATE
                    })
                    .setLocalTimeRange(HealthConstants.HeartRate.START_TIME,
                            HealthConstants.HeartRate.TIME_OFFSET,
                            startTime, endTime)
                    .build();

            // Alternativ, încercăm și pachetul specific pentru telefonul tău
            ReadRequest altHeartRateRequest = new ReadRequest.Builder()
                    .setDataType("com.sec.android.app.shealth.heart_rate")
                    .setProperties(new String[] {
                            "heart_rate"
                    })
                    .setLocalTimeRange("start_time",
                            "time_offset",
                            startTime, endTime)
                    .build();

            try {
                resolver.read(heartRateRequest).setResultListener(result -> {
                    try {
                        boolean hasData = false;
                        for (HealthData data : result) {
                            double heartRate = data.getDouble(HealthConstants.HeartRate.HEART_RATE);
                            stressCalculator.addHeartRateMeasurement(heartRate);
                            latestData.put("heart_rate", heartRate);
                            hasData = true;
                        }

                        if (!hasData) {
                            // Dacă nu avem date, încercăm metoda alternativă
                            tryAlternativeHeartRate(altHeartRateRequest, latestData);
                        }

                        // Calculăm nivelul de stress
                        double stressLevel = stressCalculator.calculateStressLevel();
                        if (stressLevel >= 0) {
                            latestData.put("stress_level", stressLevel);
                            String stressCategory = stressCalculator.getStressCategory(stressLevel);
                            Log.d(TAG, "Current stress level: " + stressLevel + " (" + stressCategory + ")");
                        }

                        listener.onDataReceived(latestData);
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing heart rate data: " + e.getMessage());
                    } finally {
                        result.close();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error with primary heart rate request, trying alternative: " + e.getMessage());
                tryAlternativeHeartRate(altHeartRateRequest, latestData);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading heart rate data: " + e.getMessage());
            listener.onDataReadError("Failed to read heart rate data");
        }
    }

    private void tryAlternativeHeartRate(ReadRequest request, Map<String, Double> latestData) {
        try {
            resolver.read(request).setResultListener(result -> {
                try {
                    for (HealthData data : result) {
                        double heartRate = data.getDouble("heart_rate");
                        stressCalculator.addHeartRateMeasurement(heartRate);
                        latestData.put("heart_rate", heartRate);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing alternative heart rate data: " + e.getMessage());
                } finally {
                    result.close();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error with alternative heart rate request: " + e.getMessage());
        }
    }

    // Citim datele pentru oxigen din sânge
    private void readBloodOxygen(long startTime, long endTime, Map<String, Double> latestData, HealthDataListener listener) {
        try {
            // Încercăm pachetul standard Samsung Health
            ReadRequest spO2Request = new ReadRequest.Builder()
                    .setDataType("com.samsung.health.oxygen_saturation")
                    .setProperties(new String[] {
                            "oxygen_saturation"
                    })
                    .setLocalTimeRange("start_time",
                            "time_offset",
                            startTime, endTime)
                    .build();

            // Alternativ, încercăm și pachetul specific pentru telefonul tău
            ReadRequest altSpO2Request = new ReadRequest.Builder()
                    .setDataType("com.sec.android.app.shealth.oxygen_saturation")
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
                        boolean hasData = false;
                        for (HealthData data : result) {
                            latestData.put("blood_oxygen",
                                    data.getDouble("oxygen_saturation"));
                            hasData = true;
                            break;
                        }

                        if (!hasData) {
                            // Dacă nu avem date, încercăm metoda alternativă
                            tryAlternativeBloodOxygen(altSpO2Request, latestData);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing blood oxygen data: " + e.getMessage());
                    } finally {
                        result.close();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error with primary blood oxygen request, trying alternative: " + e.getMessage());
                tryAlternativeBloodOxygen(altSpO2Request, latestData);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading blood oxygen data: " + e.getMessage());
            listener.onDataReadError("Failed to read blood oxygen data");
        }
    }

    private void tryAlternativeBloodOxygen(ReadRequest request, Map<String, Double> latestData) {
        try {
            resolver.read(request).setResultListener(result -> {
                try {
                    for (HealthData data : result) {
                        latestData.put("blood_oxygen",
                                data.getDouble("oxygen_saturation"));
                        break;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing alternative blood oxygen data: " + e.getMessage());
                } finally {
                    result.close();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error with alternative blood oxygen request: " + e.getMessage());
        }
    }

    private void readBloodPressure(long startTime, long endTime, Map<String, Double> latestData, HealthDataListener listener) {
        try {
            // Încercăm pachetul standard Samsung Health
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

            // Alternativ, încercăm și pachetul specific pentru telefonul tău
            ReadRequest altBpRequest = new ReadRequest.Builder()
                    .setDataType("com.sec.android.app.shealth.blood_pressure")
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
                        boolean hasData = false;
                        for (HealthData data : result) {
                            double systolic = data.getDouble("systolic");
                            double diastolic = data.getDouble("diastolic");
                            latestData.put("blood_pressure_systolic", systolic);
                            latestData.put("blood_pressure_diastolic", diastolic);
                            hasData = true;
                            break; // Luăm doar ultima măsurătoare
                        }

                        if (!hasData) {
                            // Dacă nu avem date, încercăm metoda alternativă
                            tryAlternativeBloodPressure(altBpRequest, latestData);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing blood pressure data: " + e.getMessage());
                    } finally {
                        result.close();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error with primary blood pressure request, trying alternative: " + e.getMessage());
                tryAlternativeBloodPressure(altBpRequest, latestData);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading blood pressure data: " + e.getMessage());
            listener.onDataReadError("Failed to read blood pressure data");
        }
    }

    private void tryAlternativeBloodPressure(ReadRequest request, Map<String, Double> latestData) {
        try {
            resolver.read(request).setResultListener(result -> {
                try {
                    for (HealthData data : result) {
                        double systolic = data.getDouble("systolic");
                        double diastolic = data.getDouble("diastolic");
                        latestData.put("blood_pressure_systolic", systolic);
                        latestData.put("blood_pressure_diastolic", diastolic);
                        break;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing alternative blood pressure data: " + e.getMessage());
                } finally {
                    result.close();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error with alternative blood pressure request: " + e.getMessage());
        }
    }

    private void readTemperature(long startTime, long endTime, Map<String, Double> latestData, HealthDataListener listener) {
        try {
            // Încercăm pachetul standard Samsung Health
            ReadRequest tempRequest = new ReadRequest.Builder()
                    .setDataType("com.samsung.health.temperature")
                    .setProperties(new String[] {
                            "temperature"
                    })
                    .setLocalTimeRange("start_time",
                            "time_offset",
                            startTime, endTime)
                    .build();

            // Alternativ, încercăm și pachetul specific pentru telefonul tău
            ReadRequest altTempRequest = new ReadRequest.Builder()
                    .setDataType("com.sec.android.app.shealth.temperature")
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
                        boolean hasData = false;
                        for (HealthData data : result) {
                            latestData.put("temperature", data.getDouble("temperature"));
                            hasData = true;
                            break;
                        }

                        if (!hasData) {
                            // Dacă nu avem date, încercăm metoda alternativă
                            tryAlternativeTemperature(altTempRequest, latestData);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing temperature data: " + e.getMessage());
                    } finally {
                        result.close();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error with primary temperature request, trying alternative: " + e.getMessage());
                tryAlternativeTemperature(altTempRequest, latestData);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading temperature data: " + e.getMessage());
            listener.onDataReadError("Failed to read temperature data");
        }
    }

    private void tryAlternativeTemperature(ReadRequest request, Map<String, Double> latestData) {
        try {
            resolver.read(request).setResultListener(result -> {
                try {
                    for (HealthData data : result) {
                        latestData.put("temperature", data.getDouble("temperature"));
                        break;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing alternative temperature data: " + e.getMessage());
                } finally {
                    result.close();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error with alternative temperature request: " + e.getMessage());
        }
    }

    private void readSleep(long startTime, long endTime, Map<String, Double> latestData, HealthDataListener listener) {
        try {
            // Încercăm pachetul standard Samsung Health
            ReadRequest sleepRequest = new ReadRequest.Builder()
                    .setDataType("com.samsung.health.sleep")
                    .setProperties(new String[] {
                            "sleep_stage"
                    })
                    .setLocalTimeRange("start_time",
                            "time_offset",
                            startTime, endTime)
                    .build();

            // Alternativ, încercăm și pachetul specific pentru telefonul tău
            ReadRequest altSleepRequest = new ReadRequest.Builder()
                    .setDataType("com.sec.android.app.shealth.sleep")
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
                        boolean hasData = false;
                        for (HealthData data : result) {
                            latestData.put("sleep_stage", (double)data.getInt("sleep_stage"));
                            hasData = true;
                            break;
                        }

                        if (!hasData) {
                            // Dacă nu avem date, încercăm metoda alternativă
                            tryAlternativeSleep(altSleepRequest, latestData);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing sleep data: " + e.getMessage());
                    } finally {
                        result.close();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error with primary sleep request, trying alternative: " + e.getMessage());
                tryAlternativeSleep(altSleepRequest, latestData);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading sleep data: " + e.getMessage());
            listener.onDataReadError("Failed to read sleep data");
        }
    }

    private void tryAlternativeSleep(ReadRequest request, Map<String, Double> latestData) {
        try {
            resolver.read(request).setResultListener(result -> {
                try {
                    for (HealthData data : result) {
                        latestData.put("sleep_stage", (double)data.getInt("sleep_stage"));
                        break;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing alternative sleep data: " + e.getMessage());
                } finally {
                    result.close();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error with alternative sleep request: " + e.getMessage());
        }
    }

    private void readBioactiveData(long startTime, long endTime, Map<String, Double> latestData, HealthDataListener listener) {
        try {
            // Încercăm pachetul standard Samsung Health
            ReadRequest bioactiveRequest = new ReadRequest.Builder()
                    .setDataType("com.samsung.health.bioactive_data")
                    .setProperties(new String[] {
                            "bioactive_type",
                            "bioactive_value"
                    })
                    .setLocalTimeRange("start_time", "time_offset", startTime, endTime)
                    .build();

            // Alternativ, încercăm și pachetul specific pentru telefonul tău
            ReadRequest altBioactiveRequest = new ReadRequest.Builder()
                    .setDataType("com.sec.android.app.shealth.bioactive_data")
                    .setProperties(new String[] {
                            "bioactive_type",
                            "bioactive_value"
                    })
                    .setLocalTimeRange("start_time", "time_offset", startTime, endTime)
                    .build();

            try {
                resolver.read(bioactiveRequest).setResultListener(result -> {
                    try {
                        boolean hasData = false;
                        for (HealthData data : result) {
                            latestData.put("bioactive_" + data.getString("bioactive_type"),
                                    data.getDouble("bioactive_value"));
                            hasData = true;
                        }

                        if (!hasData) {
                            // Dacă nu avem date, încercăm metoda alternativă
                            tryAlternativeBioactive(altBioactiveRequest, latestData);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing bioactive data: " + e.getMessage());
                    } finally {
                        result.close();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error with primary bioactive request, trying alternative: " + e.getMessage());
                tryAlternativeBioactive(altBioactiveRequest, latestData);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading bioactive data: " + e.getMessage());
            listener.onDataReadError("Failed to read bioactive data");
        }
    }

    private void tryAlternativeBioactive(ReadRequest request, Map<String, Double> latestData) {
        try {
            resolver.read(request).setResultListener(result -> {
                try {
                    for (HealthData data : result) {
                        latestData.put("bioactive_" + data.getString("bioactive_type"),
                                data.getDouble("bioactive_value"));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing alternative bioactive data: " + e.getMessage());
                } finally {
                    result.close();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error with alternative bioactive request: " + e.getMessage());
        }
    }

    private void readBiaData(long startTime, long endTime, Map<String, Double> latestData, HealthDataListener listener) {
        try {
            // Încercăm pachetul standard Samsung Health
            ReadRequest biaRequest = new ReadRequest.Builder()
                    .setDataType("com.samsung.health.body_composition")
                    .setProperties(new String[] {
                            "body_fat",
                            "skeletal_muscle",
                            "body_water"
                    })
                    .setLocalTimeRange("start_time", "time_offset", startTime, endTime)
                    .build();

            // Alternativ, încercăm și pachetul specific pentru telefonul tău
            ReadRequest altBiaRequest = new ReadRequest.Builder()
                    .setDataType("com.sec.android.app.shealth.body_composition")
                    .setProperties(new String[] {
                            "body_fat",
                            "skeletal_muscle",
                            "body_water"
                    })
                    .setLocalTimeRange("start_time", "time_offset", startTime, endTime)
                    .build();

            try {
                resolver.read(biaRequest).setResultListener(result -> {
                    try {
                        boolean hasData = false;
                        for (HealthData data : result) {
                            latestData.put("body_fat", data.getDouble("body_fat"));
                            latestData.put("skeletal_muscle", data.getDouble("skeletal_muscle"));
                            latestData.put("body_water", data.getDouble("body_water"));
                            hasData = true;
                        }

                        if (!hasData) {
                            // Dacă nu avem date, încercăm metoda alternativă
                            tryAlternativeBia(altBiaRequest, latestData);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing BIA data: " + e.getMessage());
                    } finally {
                        result.close();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error with primary BIA request, trying alternative: " + e.getMessage());
                tryAlternativeBia(altBiaRequest, latestData);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading BIA data: " + e.getMessage());
            listener.onDataReadError("Failed to read BIA data");
        }
    }

    private void tryAlternativeBia(ReadRequest request, Map<String, Double> latestData) {
        try {
            resolver.read(request).setResultListener(result -> {
                try {
                    for (HealthData data : result) {
                        latestData.put("body_fat", data.getDouble("body_fat"));
                        latestData.put("skeletal_muscle", data.getDouble("skeletal_muscle"));
                        latestData.put("body_water", data.getDouble("body_water"));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing alternative BIA data: " + e.getMessage());
                } finally {
                    result.close();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error with alternative BIA request: " + e.getMessage());
        }
    }

    private void readBarometerData(long startTime, long endTime, Map<String, Double> latestData, HealthDataListener listener) {
        try {
            // Încercăm pachetul standard Samsung Health
            ReadRequest barometerRequest = new ReadRequest.Builder()
                    .setDataType("com.samsung.health.ambient_pressure")
                    .setProperties(new String[] {
                            "pressure"
                    })
                    .setLocalTimeRange("start_time", "time_offset", startTime, endTime)
                    .build();

            // Alternativ, încercăm și pachetul specific pentru telefonul tău
            ReadRequest altBarometerRequest = new ReadRequest.Builder()
                    .setDataType("com.sec.android.app.shealth.ambient_pressure")
                    .setProperties(new String[] {
                            "pressure"
                    })
                    .setLocalTimeRange("start_time", "time_offset", startTime, endTime)
                    .build();

            try {
                resolver.read(barometerRequest).setResultListener(result -> {
                    try {
                        boolean hasData = false;
                        for (HealthData data : result) {
                            latestData.put("pressure", data.getDouble("pressure"));
                            hasData = true;
                        }

                        if (!hasData) {
                            // Dacă nu avem date, încercăm metoda alternativă
                            tryAlternativeBarometer(altBarometerRequest, latestData);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing barometer data: " + e.getMessage());
                    } finally {
                        result.close();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error with primary barometer request, trying alternative: " + e.getMessage());
                tryAlternativeBarometer(altBarometerRequest, latestData);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading barometer data: " + e.getMessage());
            listener.onDataReadError("Failed to read barometer data");
        }
    }

    private void tryAlternativeBarometer(ReadRequest request, Map<String, Double> latestData) {
        try {
            resolver.read(request).setResultListener(result -> {
                try {
                    for (HealthData data : result) {
                        latestData.put("pressure", data.getDouble("pressure"));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing alternative barometer data: " + e.getMessage());
                } finally {
                    result.close();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error with alternative barometer request: " + e.getMessage());
        }
    }

    private void readExerciseData(long startTime, long endTime, Map<String, Double> latestData) {
        try {
            // Încercăm pachetul standard Samsung Health
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

            // Alternativ, încercăm și pachetul specific pentru telefonul tău
            ReadRequest altExerciseRequest = new ReadRequest.Builder()
                    .setDataType("com.sec.android.app.shealth.exercise")
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
                        boolean hasData = false;
                        for (HealthData data : result) {
                            String exerciseType = data.getString("exercise_type");
                            double intensity = data.getFloat("exercise_intensity");
                            latestData.put("exercise_type", (double) exerciseType.hashCode());
                            latestData.put("exercise_intensity", intensity);
                            hasData = true;
                            break;
                        }

                        if (!hasData) {
                            // Dacă nu avem date, încercăm metoda alternativă
                            tryAlternativeExercise(altExerciseRequest, latestData);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing exercise data: " + e.getMessage());
                    } finally {
                        result.close();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error with primary exercise request, trying alternative: " + e.getMessage());
                tryAlternativeExercise(altExerciseRequest, latestData);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading exercise data: " + e.getMessage());
        }
    }

    private void tryAlternativeExercise(ReadRequest request, Map<String, Double> latestData) {
        try {
            resolver.read(request).setResultListener(result -> {
                try {
                    for (HealthData data : result) {
                        String exerciseType = data.getString("exercise_type");
                        double intensity = data.getFloat("exercise_intensity");
                        latestData.put("exercise_type", (double) exerciseType.hashCode());
                        latestData.put("exercise_intensity", intensity);
                        break;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing alternative exercise data: " + e.getMessage());
                } finally {
                    result.close();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error with alternative exercise request: " + e.getMessage());
        }
    }
}