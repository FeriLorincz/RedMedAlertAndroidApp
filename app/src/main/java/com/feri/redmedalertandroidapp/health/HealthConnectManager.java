package com.feri.redmedalertandroidapp.health;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class HealthConnectManager {
    private static final String TAG = "HealthConnectManager";

    private final Context context;
    private ActivityResultLauncher<Intent> requestPermissionLauncher;
    private HealthConnectListener listener;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Random random = new Random();
    private boolean isConnected = false;
    private Handler dataUpdateHandler;
    private Runnable dataUpdateRunnable;

    public interface HealthConnectListener {
        void onConnected();
        void onConnectionFailed(String error);
        void onDataReceived(Map<String, Double> data);
    }

    public HealthConnectManager(Context context) {
        this.context = context;
        this.dataUpdateHandler = new Handler(Looper.getMainLooper());
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
                        Log.d(TAG, "Permisiuni acordate");
                        isConnected = true;
                        if (listener != null) {
                            listener.onConnected();
                        }
                        startDataCollection();
                    } else {
                        Log.d(TAG, "Permisiuni refuzate - continuăm cu date simulate");
                        // Chiar dacă permisiunile sunt refuzate, continuăm cu date simulate pentru testare
                        isConnected = true;
                        if (listener != null) {
                            listener.onConnected();
                        }
                        startDataCollection();
                    }
                });
    }

    private void checkAvailability() {
        try {
            // Verifică dacă Health Connect este instalat
            if (isHealthConnectInstalled()) {
                Log.d(TAG, "Health Connect este disponibil");
            } else {
                Log.w(TAG, "Health Connect nu este instalat - folosim date simulate");
                // Nu oprim aplicația, continuăm cu date simulate
            }
        } catch (Exception e) {
            Log.e(TAG, "Eroare la verificarea Health Connect", e);
        }
    }

    private boolean isHealthConnectInstalled() {
        try {
            context.getPackageManager().getPackageInfo("com.google.android.apps.healthdata", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void tryInstallHealthConnect() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=com.google.android.apps.healthdata"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Nu s-a putut deschide Google Play pentru Health Connect", e);
        }
    }

    public void requestPermissions() {
        Log.d(TAG, "Solicitare permisiuni Health Connect");

        if (!isHealthConnectInstalled()) {
            Log.w(TAG, "Health Connect nu este instalat - oferim opțiunea de instalare");
            if (listener != null) {
                listener.onConnectionFailed("Health Connect nu este instalat. Dorești să-l instalezi?");
            }
            tryInstallHealthConnect();

            // Pentru testare, continuăm cu date simulate
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                isConnected = true;
                if (listener != null) {
                    listener.onConnected();
                }
                startDataCollection();
            }, 2000);
            return;
        }

        try {
            // Încercăm să deschidem setările Health Connect
            Intent intent = new Intent();
            intent.setAction("android.settings.HEALTH_CONNECT_SETTINGS");
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                if (requestPermissionLauncher != null) {
                    requestPermissionLauncher.launch(intent);
                } else {
                    // Fallback - presupunem că avem permisiunile
                    isConnected = true;
                    if (listener != null) {
                        listener.onConnected();
                    }
                    startDataCollection();
                }
            } else {
                // Health Connect settings nu sunt disponibile - continuăm cu date simulate
                Log.w(TAG, "Health Connect settings nu sunt disponibile - folosim date simulate");
                isConnected = true;
                if (listener != null) {
                    listener.onConnected();
                }
                startDataCollection();
            }
        } catch (Exception e) {
            Log.e(TAG, "Eroare la solicitarea permisiunilor", e);
            // În caz de eroare, continuăm cu date simulate
            isConnected = true;
            if (listener != null) {
                listener.onConnected();
            }
            startDataCollection();
        }
    }

    private void startDataCollection() {
        Log.d(TAG, "Începem colectarea datelor de sănătate");

        // Oprim orice colectare anterioară
        stopDataCollection();

        // Începem colectarea periodică de date
        dataUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                readHealthData();
                // Repetăm la fiecare 10 secunde
                dataUpdateHandler.postDelayed(this, 10000);
            }
        };

        // Prima citire imediată
        dataUpdateHandler.post(dataUpdateRunnable);
    }

    private void stopDataCollection() {
        if (dataUpdateRunnable != null && dataUpdateHandler != null) {
            dataUpdateHandler.removeCallbacks(dataUpdateRunnable);
        }
    }

    public void readHealthData() {
        if (!isConnected) {
            if (listener != null) {
                listener.onConnectionFailed("Health Connect nu este conectat");
            }
            return;
        }

        // Executăm într-un thread separat
        executor.execute(() -> {
            try {
                Map<String, Double> healthData = generateSimulatedData();

                // Returnăm datele pe main thread
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (listener != null) {
                        listener.onDataReceived(healthData);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Eroare la citirea datelor de sănătate", e);
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (listener != null) {
                        listener.onConnectionFailed("Eroare la citirea datelor: " + e.getMessage());
                    }
                });
            }
        });
    }

    private Map<String, Double> generateSimulatedData() {
        Map<String, Double> data = new HashMap<>();

        // Generăm date simulate realiste pentru testare
        data.put("heart_rate", 70 + random.nextDouble() * 30); // 70-100 BPM
        data.put("blood_pressure_systolic", 110 + random.nextDouble() * 30); // 110-140 mmHg
        data.put("blood_pressure_diastolic", 70 + random.nextDouble() * 20); // 70-90 mmHg
        data.put("blood_oxygen", 95 + random.nextDouble() * 5); // 95-100%
        data.put("temperature", 36.0 + random.nextDouble() * 1.5); // 36-37.5°C
        data.put("steps", random.nextDouble() * 1000); // 0-1000 pași (incrementali)
        data.put("stress_level", random.nextDouble() * 100); // 0-100%

        String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        Log.d(TAG, "Date simulate generate la " + timestamp + ": " + data.toString());

        return data;
    }

    // Metodă pentru testarea conectivității
    public void testConnection() {
        Log.d(TAG, "Test conexiune Health Connect");

        Map<String, Double> testData = new HashMap<>();
        testData.put("heart_rate", 75.0);
        testData.put("blood_pressure_systolic", 120.0);
        testData.put("blood_pressure_diastolic", 80.0);
        testData.put("blood_oxygen", 98.0);
        testData.put("temperature", 36.6);
        testData.put("test_mode", 1.0); // Indicator că sunt date de test

        Log.d(TAG, "Trimitem date de test: " + testData.toString());

        if (listener != null) {
            listener.onDataReceived(testData);
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void disconnect() {
        Log.d(TAG, "Deconectare Health Connect");
        stopDataCollection();
        isConnected = false;
    }

    // Cleanup resources
    public void cleanup() {
        stopDataCollection();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}