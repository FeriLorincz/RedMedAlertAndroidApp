package com.feri.redmedalertandroidapp.health;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import com.samsung.android.sdk.healthdata.HealthPermissionManager;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SamsungHealthManager {

    private static final String TAG = "SamsungHealthManager";

    private final Context context;
    private HealthDataStore mStore;
    private HealthPermissionManager permissionManager;
    private Set<HealthPermissionManager.PermissionKey> permissionKeys;
    private HealthConnectionCallback connectionCallback;
    private HealthMonitoringService monitoringService;

    public SamsungHealthManager(Context context) {
        this.context = context;
        this.permissionKeys = new HashSet<>();
        initializeHealthKit();
    }

    private void initializeHealthKit() {
        try {
            // Initialize connection to Samsung Health
            mStore = new HealthDataStore(context, connectionListener);

            // Initialize permission manager
            permissionManager = new HealthPermissionManager(mStore);

            // Setup required permissions
            setupPermissionKeys();

        } catch (Exception e) {
            Log.e(TAG, "Error initializing Samsung Health SDK: " + e.getMessage());
            throw new RuntimeException("Failed to initialize Samsung Health SDK", e);
        }
    }

    private void setupPermissionKeys() {
        // Heart Rate
        permissionKeys.add(new HealthPermissionManager.PermissionKey(
                "com.samsung.health.heart_rate",
                HealthPermissionManager.PermissionType.READ
        ));

        // Blood Pressure
        permissionKeys.add(new HealthPermissionManager.PermissionKey(
                "com.samsung.health.blood_pressure",
                HealthPermissionManager.PermissionType.READ
        ));

        // Blood Oxygen
        permissionKeys.add(new HealthPermissionManager.PermissionKey(
                "com.samsung.health.oxygen_saturation",
                HealthPermissionManager.PermissionType.READ
        ));
    }

    public void requestPermissions(Activity activity) {
        try {
            permissionManager.requestPermissions(permissionKeys, activity)
                    .setResultListener(result -> {
                        Map<HealthPermissionManager.PermissionKey, Boolean> resultMap =
                                result.getResultMap();

                        if (resultMap.containsValue(Boolean.FALSE)) {
                            Log.e(TAG, "All permissions not granted");
                            if (connectionCallback != null) {
                                connectionCallback.onPermissionsDenied();
                            }
                        } else {
                            Log.d(TAG, "All permissions granted");
                            if (connectionCallback != null) {
                                connectionCallback.onPermissionsGranted();
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error requesting permissions: " + e.getMessage());
            if (connectionCallback != null) {
                connectionCallback.onPermissionsDenied();
            }
        }
    }

    public void connect() {
        if (mStore != null) {
            mStore.connectService();
        }
    }

    public void disconnect() {
        if (mStore != null) {
            mStore.disconnectService();
        }
    }

    private final HealthDataStore.ConnectionListener connectionListener =
            new HealthDataStore.ConnectionListener() {
                @Override
                public void onConnected() {
                    Log.d(TAG, "Connected to Samsung Health");
                    if (connectionCallback != null) {
                        connectionCallback.onConnected();
                    }
                }

                @Override
                public void onConnectionFailed(HealthConnectionErrorResult error) { // Modificat tipul parametrului
                    Log.e(TAG, "Connection to Samsung Health failed: " + error.toString());
                    if (connectionCallback != null) {
                        connectionCallback.onConnectionFailed(error); // Actualizăm și interfața
                    }
                }

                @Override
                public void onDisconnected() {
                    Log.d(TAG, "Disconnected from Samsung Health");
                    if (connectionCallback != null) {
                        connectionCallback.onDisconnected();
                    }
                }
            };

    public void setConnectionCallback(HealthConnectionCallback callback) {
        this.connectionCallback = callback;
    }

    public void startMonitoring(Context context) {
        if (mStore != null) {  // Verificăm doar dacă mStore există
            Intent serviceIntent = new Intent(context, HealthMonitoringService.class);
            context.startService(serviceIntent);

            monitoringService = new HealthMonitoringService();
            monitoringService.startMonitoring(mStore);
        }
    }

    public void stopMonitoring(Context context) {
        if (monitoringService != null) {
            monitoringService.stopMonitoring();
            Intent serviceIntent = new Intent(context, HealthMonitoringService.class);
            context.stopService(serviceIntent);
        }
    }
}
