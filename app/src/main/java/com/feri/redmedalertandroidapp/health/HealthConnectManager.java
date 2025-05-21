package com.feri.redmedalertandroidapp.health;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.health.connect.client.HealthConnectClient;
import androidx.health.connect.client.PermissionController;
import androidx.health.connect.client.permission.HealthPermission;
import androidx.health.connect.client.records.HeartRateRecord;
import androidx.health.connect.client.records.BloodPressureRecord;
import androidx.health.connect.client.records.OxygenSaturationRecord;
import androidx.health.connect.client.records.BodyTemperatureRecord;
import androidx.health.connect.client.records.StepsRecord;
import androidx.health.connect.client.records.SleepSessionRecord;
import androidx.health.connect.client.request.ReadRecordsRequest;
import androidx.health.connect.client.time.TimeRangeFilter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HealthConnectManager {
    private static final String TAG = "HealthConnectManager";

    private final Context context;
    private HealthConnectClient healthConnectClient;
    private ActivityResultLauncher<Intent> requestPermissionLauncher;
    private HealthConnectListener listener;

    // Lista de permisiuni necesare
    private static final Set<String> PERMISSIONS = Set.of(
            HealthPermission.getReadPermission(HeartRateRecord.class),
            HealthPermission.getReadPermission(BloodPressureRecord.class),
            HealthPermission.getReadPermission(OxygenSaturationRecord.class),
            HealthPermission.getReadPermission(BodyTemperatureRecord.class),
            HealthPermission.getReadPermission(StepsRecord.class),
            HealthPermission.getReadPermission(SleepSessionRecord.class)
    );

    public interface HealthConnectListener {
        void onConnected();
        void onConnectionFailed(String error);
        void onDataReceived(Map<String, Double> data);
    }

    public HealthConnectManager(Context context) {
        this.context = context;
        checkAvailability();
    }

    public void setListener(HealthConnectListener listener) {
        this.listener = listener;
    }

    public void registerForActivityResult(AppCompatActivity activity) {
        requestPermissionLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        checkPermissionsAndConnect();
                    } else {
                        if (listener != null) {
                            listener.onConnectionFailed("Permisiunile Health Connect au fost refuzate");
                        }
                    }
                });
    }

    private void checkAvailability() {
        // Verifică dacă Health Connect este disponibil pe dispozitiv
        if (HealthConnectClient.isProviderAvailable(context)) {
            healthConnectClient = HealthConnectClient.getOrCreate(context);
            Log.d(TAG, "Health Connect este disponibil");
        } else {
            // Health Connect nu este instalat, încearcă să-l instalezi
            Log.e(TAG, "Health Connect nu este disponibil, se încearcă instalarea");
            if (listener != null) {
                listener.onConnectionFailed("Health Connect nu este instalat");
            }
            tryInstallHealthConnect();
        }
    }

    private void tryInstallHealthConnect() {
        try {
            // Deschide Google Play pentru a instala Health Connect
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=com.google.android.apps.healthdata"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Nu s-a putut deschide Google Play pentru Health Connect", e);
            // Încearcă să deschidă browserul în loc
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception ex) {
                Log.e(TAG, "Nu s-a putut deschide browserul pentru Health Connect", ex);
            }
        }
    }

    public void requestPermissions() {
        if (healthConnectClient == null) {
            if (listener != null) {
                listener.onConnectionFailed("Health Connect nu este inițializat");
            }
            return;
        }

        checkPermissionsAndConnect();
    }

    private void checkPermissionsAndConnect() {
        healthConnectClient.getPermissionController()
                .getGrantedPermissions()
                .addOnSuccessListener(grantedPermissions -> {
                    if (grantedPermissions.containsAll(PERMISSIONS)) {
                        // Toate permisiunile sunt acordate
                        Log.d(TAG, "Toate permisiunile sunt acordate");
                        if (listener != null) {
                            listener.onConnected();
                        }
                        readHealthData();
                    } else {
                        // Solicită permisiunile lipsă
                        Log.d(TAG, "Solicitare permisiuni");
                        requestHealthConnectPermissions();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Eroare la verificarea permisiunilor", e);
                    if (listener != null) {
                        listener.onConnectionFailed("Eroare la verificarea permisiunilor: " + e.getMessage());
                    }
                });
    }

    private void requestHealthConnectPermissions() {
        if (requestPermissionLauncher == null) {
            Log.e(TAG, "requestPermissionLauncher nu este inițializat");
            if (listener != null) {
                listener.onConnectionFailed("Eroare la solicitarea permisiunilor: launcher neînregistrat");
            }
            return;
        }

        try {
            Intent intent = PermissionController.createRequestPermissionResultContract()
                    .createIntent(context, PERMISSIONS);
            requestPermissionLauncher.launch(intent);
        } catch (Exception e) {
            Log.e(TAG, "Eroare la lansarea intent-ului de permisiuni", e);
            if (listener != null) {
                listener.onConnectionFailed("Eroare la solicitarea permisiunilor: " + e.getMessage());
            }
        }
    }

    public void readHealthData() {
        if (healthConnectClient == null) {
            if (listener != null) {
                listener.onConnectionFailed("Health Connect nu este inițializat");
            }
            return;
        }

        // Definim intervalul de timp pentru date - ultimele 24 de ore
        Instant endTime = Instant.now();
        Instant startTime = endTime.minus(24, ChronoUnit.HOURS);
        TimeRangeFilter timeRangeFilter = new TimeRangeFilter.Builder()
                .setStartTime(startTime)
                .setEndTime(endTime)
                .build();

        // Citim datele pentru fiecare tip
        Map<String, Double> healthData = new HashMap<>();
        readHeartRateData(timeRangeFilter, healthData);
    }

    private void readHeartRateData(TimeRangeFilter timeRangeFilter, Map<String, Double> healthData) {
        ReadRecordsRequest request = new ReadRecordsRequest.Builder<>(HeartRateRecord.class)
                .setTimeRangeFilter(timeRangeFilter)
                .build();

        healthConnectClient.readRecords(request)
                .addOnSuccessListener(response -> {
                    if (!response.getRecords().isEmpty()) {
                        // Folosim cea mai recentă înregistrare
                        HeartRateRecord record = response.getRecords().get(response.getRecords().size() - 1);
                        if (!record.getSamples().isEmpty()) {
                            double heartRate = record.getSamples().get(record.getSamples().size() - 1).getBeatsPerMinute();
                            healthData.put("heart_rate", heartRate);
                        }
                    }

                    // Continuă cu următorul tip de date
                    readBloodPressureData(timeRangeFilter, healthData);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Eroare la citirea datelor de ritm cardiac", e);
                    // Continuă cu următorul tip, chiar dacă există o eroare
                    readBloodPressureData(timeRangeFilter, healthData);
                });
    }

    private void readBloodPressureData(TimeRangeFilter timeRangeFilter, Map<String, Double> healthData) {
        ReadRecordsRequest request = new ReadRecordsRequest.Builder<>(BloodPressureRecord.class)
                .setTimeRangeFilter(timeRangeFilter)
                .build();

        healthConnectClient.readRecords(request)
                .addOnSuccessListener(response -> {
                    if (!response.getRecords().isEmpty()) {
                        // Folosim cea mai recentă înregistrare
                        BloodPressureRecord record = response.getRecords().get(response.getRecords().size() - 1);
                        healthData.put("blood_pressure_systolic", (double) record.getSystolic().getInMillimetersOfMercury());
                        healthData.put("blood_pressure_diastolic", (double) record.getDiastolic().getInMillimetersOfMercury());
                    }

                    // Continuă cu următorul tip
                    readOxygenSaturationData(timeRangeFilter, healthData);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Eroare la citirea datelor de tensiune arterială", e);
                    readOxygenSaturationData(timeRangeFilter, healthData);
                });
    }

    private void readOxygenSaturationData(TimeRangeFilter timeRangeFilter, Map<String, Double> healthData) {
        ReadRecordsRequest request = new ReadRecordsRequest.Builder<>(OxygenSaturationRecord.class)
                .setTimeRangeFilter(timeRangeFilter)
                .build();

        healthConnectClient.readRecords(request)
                .addOnSuccessListener(response -> {
                    if (!response.getRecords().isEmpty()) {
                        // Folosim cea mai recentă înregistrare
                        OxygenSaturationRecord record = response.getRecords().get(response.getRecords().size() - 1);
                        healthData.put("blood_oxygen", (double) record.getPercentage());
                    }

                    // Continuă cu următorul tip
                    readBodyTemperatureData(timeRangeFilter, healthData);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Eroare la citirea datelor de saturație de oxigen", e);
                    readBodyTemperatureData(timeRangeFilter, healthData);
                });
    }

    private void readBodyTemperatureData(TimeRangeFilter timeRangeFilter, Map<String, Double> healthData) {
        ReadRecordsRequest request = new ReadRecordsRequest.Builder<>(BodyTemperatureRecord.class)
                .setTimeRangeFilter(timeRangeFilter)
                .build();

        healthConnectClient.readRecords(request)
                .addOnSuccessListener(response -> {
                    if (!response.getRecords().isEmpty()) {
                        // Folosim cea mai recentă înregistrare
                        BodyTemperatureRecord record = response.getRecords().get(response.getRecords().size() - 1);
                        healthData.put("temperature", record.getTemperature().getInCelsius());
                    }

                    // Finalizăm și returnăm toate datele colectate
                    finalizeHealthData(healthData);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Eroare la citirea datelor de temperatură corporală", e);
                    finalizeHealthData(healthData);
                });
    }

    private void finalizeHealthData(Map<String, Double> healthData) {
        Log.d(TAG, "Date sănătate: " + healthData.toString());
        new Handler(Looper.getMainLooper()).post(() -> {
            if (listener != null) {
                listener.onDataReceived(healthData);
            }
        });
    }
}