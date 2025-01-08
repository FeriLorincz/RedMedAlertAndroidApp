package com.feri.redmedalertandroidapp.health.monitor;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

public class BatteryMonitor {

    private static final String TAG = "BatteryMonitor";
    private static final int CRITICAL_BATTERY_LEVEL = 15;
    private static final int LOW_BATTERY_LEVEL = 30;
    private static final double POWER_SAVE_MULTIPLIER = 1.5;
    private final Context context;
    private BatteryCallback callback;
    private final BatteryManager batteryManager;

    public interface BatteryCallback {
        void onBatteryCritical();
        void onBatteryLow();
        void onBatteryNormal();
    }

    public BatteryMonitor(Context context) {
        this.context = context;
        this.batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
    }

    public void setBatteryCallback(BatteryCallback callback) {
        this.callback = callback;
    }

    public int getBatteryLevel() {
        BatteryManager batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    }

    public boolean isCharging() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, filter);

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
    }

    public void checkBatteryStatus() {
        int level = getBatteryLevel();
        Log.d(TAG, "Battery level: " + level + "%");

        if (level <= CRITICAL_BATTERY_LEVEL && !isCharging()) {
            if (callback != null) callback.onBatteryCritical();
        } else if (level <= LOW_BATTERY_LEVEL && !isCharging()) {
            if (callback != null) callback.onBatteryLow();
        } else {
            if (callback != null) callback.onBatteryNormal();
        }
    }

    public int calculateAdjustedInterval(int baseInterval, boolean isHighPriority) {
        int batteryLevel = getBatteryLevel();
        boolean isCharging = isCharging();

        if (isHighPriority) return baseInterval;
        if (isCharging) return baseInterval;

        if (batteryLevel <= CRITICAL_BATTERY_LEVEL) {
            return (int)(baseInterval * POWER_SAVE_MULTIPLIER * 2);
        } else if (batteryLevel <= LOW_BATTERY_LEVEL) {
            return (int)(baseInterval * POWER_SAVE_MULTIPLIER);
        }
        return baseInterval;
    }

    public float getUploadSizeLimit() {
        int batteryLevel = getBatteryLevel();
        if (batteryLevel <= CRITICAL_BATTERY_LEVEL) return 50 * 1024; // 50KB
        if (batteryLevel <= LOW_BATTERY_LEVEL) return 200 * 1024; // 200KB
        return 1024 * 1024; // 1MB
    }

    public boolean shouldDisableLTE() {
        int batteryLevel = getBatteryLevel();
        return batteryLevel <= LOW_BATTERY_LEVEL && !isCharging();
    }
}
