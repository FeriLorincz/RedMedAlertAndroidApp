package com.feri.redmedalertandroidapp.health;

import android.content.Context;
import androidx.annotation.NonNull;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.feri.redmedalertandroidapp.api.config.ApiClient;
import com.feri.redmedalertandroidapp.api.config.ApiClient.ApiCallback;
import com.feri.redmedalertandroidapp.api.service.DatabaseHelper;
import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult;
import com.samsung.android.sdk.healthdata.HealthDataStore;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SensorMonitoringWorker extends Worker {

    private static final String TAG = "SensorMonitoringWorker";
    private static final long NORMAL_INTERVAL = 5 * 60 * 1000; // 5 minute
    private static final long ALERT_INTERVAL = 30 * 1000;      // 30 secunde
    private static final int LOW_BATTERY_THRESHOLD = 15;       // 15%

    private final Context context;
    private final HealthDataStore healthDataStore;
    private final HealthDataReader healthDataReader;
    private final BatteryManager batteryManager;

    public SensorMonitoringWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;

        // Implementăm un listener obligatoriu pentru HealthDataStore
        HealthDataStore.ConnectionListener connectionListener = new HealthDataStore.ConnectionListener() {
            @Override
            public void onConnected() {
                Log.d(TAG, "Connected to Samsung Health from worker");
            }

            @Override
            public void onConnectionFailed(HealthConnectionErrorResult error) {
                Log.e(TAG, "Connection to Samsung Health failed: " + error.toString());
            }

            @Override
            public void onDisconnected() {
                Log.d(TAG, "Disconnected from Samsung Health");
            }
        };

        // Inițializăm HealthDataStore cu listener-ul
        this.healthDataStore = new HealthDataStore(context, connectionListener);
        this.healthDataReader = new HealthDataReader(healthDataStore);
        this.batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // Conectăm serviciul înainte de a-l folosi
            healthDataStore.connectService();

            // Verificăm nivelul bateriei
            int batteryLevel = getBatteryLevel();
            long interval = determineInterval(batteryLevel);

            // Colectăm datele
            collectAndProcessData(interval);

            // Programăm următoarea rulare
            scheduleNextRun(interval);

            // Deconectăm serviciul când am terminat
            healthDataStore.disconnectService();

            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Error in worker", e);
            return Result.retry();
        }
    }

    private int getBatteryLevel() {
        if (batteryManager != null) {
            return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        }
        return 100; // Presupunem baterie plină dacă nu putem citi
    }

    private long determineInterval(int batteryLevel) {
        if (batteryLevel <= LOW_BATTERY_THRESHOLD) {
            return NORMAL_INTERVAL * 2; // Dublăm intervalul la baterie scăzută
        }

        // Verificăm dacă suntem în stare de alertă
        if (isInAlertState()) {
            return ALERT_INTERVAL;
        }

        return NORMAL_INTERVAL;
    }

    private boolean isInAlertState() {
        // Verificăm dacă există alerte active
        SharedPreferences prefs = context.getSharedPreferences("RedMedAlert", Context.MODE_PRIVATE);
        return prefs.getBoolean("alert_state", false);
    }

    private void collectAndProcessData(long interval) {
        healthDataReader.readLatestData(new HealthDataReader.HealthDataListener() {
            @Override
            public void onDataReceived(Map<String, Double> data) {
                processAndUploadData(data, interval);
            }

            @Override
            public void onDataReadError(String message) {
                Log.e(TAG, "Error reading health data: " + message);
            }
        });
    }

    private void processAndUploadData(Map<String, Double> data, long interval) {
        // Implementăm logica de caching și upload
        if (shouldUploadData(interval)) {
            uploadData(data);
        } else {
            cacheData(data);
        }
    }

    private boolean shouldUploadData(long interval) {
        // Decidem dacă încărcăm datele acum sau le cacheuim
        int batteryLevel = getBatteryLevel();
        boolean isCharging = isCharging();

        if (isCharging) return true;
        if (batteryLevel > LOW_BATTERY_THRESHOLD) return true;
        if (isInAlertState()) return true;

        return false;
    }

    private boolean isCharging() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
    }

    private void scheduleNextRun(long interval) {
        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                        "SensorMonitoring",
                        ExistingPeriodicWorkPolicy.UPDATE,
                        new PeriodicWorkRequest.Builder(
                                SensorMonitoringWorker.class,
                                interval,
                                TimeUnit.MILLISECONDS)
                                .setConstraints(getWorkConstraints())
                                .build()
                );
    }


    private Constraints getWorkConstraints() {
        return new Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiresDeviceIdle(false)
                .setRequiresStorageNotLow(true)
                .build();
    }

    private void cacheData(Map<String, Double> data) {
        // Implementăm logica de caching
        DatabaseHelper.getInstance(context).cacheHealthData(data);
    }

    private void uploadData(Map<String, Double> data) {
        ApiClient.getInstance(context).uploadHealthData(data, new ApiCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Data uploaded successfully from worker");
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error uploading data from worker: " + error);
                // În caz de eroare, salvăm în cache
                cacheData(data);
            }
        });
    }
}