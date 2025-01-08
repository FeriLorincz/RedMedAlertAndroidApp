package com.feri.redmedalertandroidapp.health.sensor;

import android.content.Context;
import android.util.Log;
import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import com.samsung.android.sdk.healthdata.HealthPermissionManager;
import com.feri.redmedalertandroidapp.health.HealthConnectionCallback;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class SensorConnectionManager {

    private static final String TAG = "SensorConnectionManager";
    private final Context context;
    private final HealthDataStore healthDataStore;
    private final HealthPermissionManager permissionManager;
    private final Set<HealthPermissionManager.PermissionKey> permissionKeys;
    private final AtomicBoolean isConnected;
    private ConnectionCallback connectionCallback;

    public interface ConnectionCallback {
        void onConnected();
        void onDisconnected();
        void onConnectionFailed(String reason);
        void onPermissionsGranted();
        void onPermissionsDenied();
    }

    private android.app.Activity activity;

    public SensorConnectionManager(Context context) {
        this.context = context;
        this.permissionKeys = new HashSet<>();
        this.isConnected = new AtomicBoolean(false);
        this.healthDataStore = new HealthDataStore(context, connectionListener);
        this.permissionManager = new HealthPermissionManager(healthDataStore);
        setupPermissions();
    }

    public void setActivity(android.app.Activity activity) {
        this.activity = activity;
    }

    private void setupPermissions() {
        // Permisiuni pentru senzori:
        // Senzori vitali de bază
        permissionKeys.add(new HealthPermissionManager.PermissionKey(
                "com.samsung.health.heart_rate",
                HealthPermissionManager.PermissionType.READ));
        permissionKeys.add(new HealthPermissionManager.PermissionKey(
                "com.samsung.health.blood_pressure",
                HealthPermissionManager.PermissionType.READ));
        permissionKeys.add(new HealthPermissionManager.PermissionKey(
                "com.samsung.health.oxygen_saturation",
                HealthPermissionManager.PermissionType.READ));
        permissionKeys.add(new HealthPermissionManager.PermissionKey(
                "com.samsung.health.body_temperature",
                HealthPermissionManager.PermissionType.READ));

        // Senzori de mișcare și orientare
        permissionKeys.add(new HealthPermissionManager.PermissionKey(
                "com.samsung.health.acceleration",
                HealthPermissionManager.PermissionType.READ));
        permissionKeys.add(new HealthPermissionManager.PermissionKey(
                "com.samsung.health.gyroscope",
                HealthPermissionManager.PermissionType.READ));
        permissionKeys.add(new HealthPermissionManager.PermissionKey(
                "com.samsung.health.barometer",
                HealthPermissionManager.PermissionType.READ));

        // Senzori de biometrie avansată
        permissionKeys.add(new HealthPermissionManager.PermissionKey(
                "com.samsung.health.bioelectrical_impedance",
                HealthPermissionManager.PermissionType.READ));
        permissionKeys.add(new HealthPermissionManager.PermissionKey(
                "com.samsung.health.bioactive_data",
                HealthPermissionManager.PermissionType.READ));

        // Senzori de activitate și somn
        permissionKeys.add(new HealthPermissionManager.PermissionKey(
                "com.samsung.health.sleep",
                HealthPermissionManager.PermissionType.READ));
        permissionKeys.add(new HealthPermissionManager.PermissionKey(
                "com.samsung.health.stress",
                HealthPermissionManager.PermissionType.READ));
        permissionKeys.add(new HealthPermissionManager.PermissionKey(
                "com.samsung.health.step_count",
                HealthPermissionManager.PermissionType.READ));
        permissionKeys.add(new HealthPermissionManager.PermissionKey(
                "com.samsung.health.body_posture",
                HealthPermissionManager.PermissionType.READ));

        // Fall detection și senzori de urgență
        permissionKeys.add(new HealthPermissionManager.PermissionKey(
                "com.samsung.health.fall_detection",
                HealthPermissionManager.PermissionType.READ));
        permissionKeys.add(new HealthPermissionManager.PermissionKey(
                "com.samsung.health.emergency_status",
                HealthPermissionManager.PermissionType.READ));

        // Exercise și activitate fizică
        permissionKeys.add(new HealthPermissionManager.PermissionKey(
                "com.samsung.health.exercise",
                HealthPermissionManager.PermissionType.READ));
        permissionKeys.add(new HealthPermissionManager.PermissionKey(
                "com.samsung.health.physical_activity",
                HealthPermissionManager.PermissionType.READ));
    }

    private final HealthDataStore.ConnectionListener connectionListener =
            new HealthDataStore.ConnectionListener() {
                @Override
                public void onConnected() {
                    isConnected.set(true);
                    if (connectionCallback != null) {
                        connectionCallback.onConnected();
                    }
                    requestPermissions();
                }

                @Override
                public void onConnectionFailed(HealthConnectionErrorResult error) {
                    isConnected.set(false);
                    if (connectionCallback != null) {
                        connectionCallback.onConnectionFailed(error.toString());
                    }
                }

                @Override
                public void onDisconnected() {
                    isConnected.set(false);
                    if (connectionCallback != null) {
                        connectionCallback.onDisconnected();
                    }
                }
            };

    public void connect() {
        try {
            healthDataStore.connectService();
        } catch (Exception e) {
            Log.e(TAG, "Error connecting to Samsung Health: " + e.getMessage());
            if (connectionCallback != null) {
                connectionCallback.onConnectionFailed(e.getMessage());
            }
        }
    }

    public void disconnect() {
        try {
            healthDataStore.disconnectService();
            isConnected.set(false);
        } catch (Exception e) {
            Log.e(TAG, "Error disconnecting from Samsung Health: " + e.getMessage());
        }
    }

    private void requestPermissions() {
        try {
            if (activity == null) {
                Log.e(TAG, "Activity not set. Cannot request permissions.");
                if (connectionCallback != null) {
                    connectionCallback.onPermissionsDenied();
                }
                return;
            }

            permissionManager.requestPermissions(permissionKeys, activity)
                    .setResultListener(result -> {
                        if (result.getResultMap().containsValue(Boolean.FALSE)) {
                            if (connectionCallback != null) {
                                connectionCallback.onPermissionsDenied();
                            }
                        } else {
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

    public boolean isConnected() {
        return isConnected.get();
    }

    public void setConnectionCallback(ConnectionCallback callback) {
        this.connectionCallback = callback;
    }

    public HealthDataStore getHealthDataStore() {
        return healthDataStore;
    }
}