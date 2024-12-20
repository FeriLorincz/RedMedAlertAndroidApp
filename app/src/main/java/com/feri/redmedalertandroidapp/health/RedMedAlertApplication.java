package com.feri.redmedalertandroidapp.health;

import android.app.Application;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class RedMedAlertApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initializeWorkManager();
    }

    private void initializeWorkManager() {
        // Configurăm work manager pentru monitorizare continuă
        PeriodicWorkRequest monitoringWork =
                new PeriodicWorkRequest.Builder(
                        SensorMonitoringWorker.class,
                        15, TimeUnit.MINUTES) // Interval minim recomandat
                        .setConstraints(getWorkConstraints())
                        .build();

        WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork(
                        "SensorMonitoring",
                        ExistingPeriodicWorkPolicy.KEEP,
                        monitoringWork);
    }

    private Constraints getWorkConstraints() {
        return new Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiresDeviceIdle(false)
                .setRequiresStorageNotLow(true)
                .build();
    }
}
