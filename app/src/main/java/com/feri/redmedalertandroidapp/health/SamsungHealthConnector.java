package com.feri.redmedalertandroidapp.health;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult;
import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import com.samsung.android.sdk.healthdata.HealthPermissionManager;
import com.samsung.android.sdk.healthdata.HealthResultHolder;

import java.util.HashSet;
import java.util.Set;

public class SamsungHealthConnector {
    private static final String TAG = "SamsungHealthConnector";

    private final Context context;
    private HealthDataStore healthDataStore;
    private Set<HealthPermissionManager.PermissionKey> permissionKeys;
    private ConnectionListener connectionListener;

    public interface ConnectionListener {
        void onConnected(boolean permissionsGranted);
        void onConnectionFailed(String error);
    }

    public SamsungHealthConnector(Context context) {
        this.context = context;
        this.permissionKeys = new HashSet<>();
        initializeSdk();
    }

    private void initializeSdk() {
        healthDataStore = new HealthDataStore(context, connListener);
        setupPermissionKeys();
    }

    private void setupPermissionKeys() {
        try {
            permissionKeys.add(new HealthPermissionManager.PermissionKey(
                    HealthConstants.HeartRate.HEALTH_DATA_TYPE,
                    HealthPermissionManager.PermissionType.READ));

            permissionKeys.add(new HealthPermissionManager.PermissionKey(
                    HealthConstants.BloodPressure.HEALTH_DATA_TYPE,
                    HealthPermissionManager.PermissionType.READ));

            permissionKeys.add(new HealthPermissionManager.PermissionKey(
                    "com.samsung.health.oxygen_saturation",
                    HealthPermissionManager.PermissionType.READ));

            permissionKeys.add(new HealthPermissionManager.PermissionKey(
                    "com.samsung.health.temperature",
                    HealthPermissionManager.PermissionType.READ));

            Log.d(TAG, "Permisiuni configurate: " + permissionKeys.size());
        } catch (Exception e) {
            Log.e(TAG, "Eroare la configurarea permisiunilor: " + e.getMessage(), e);
        }
    }

    public void setConnectionListener(ConnectionListener listener) {
        this.connectionListener = listener;
    }

    public void connect() {
        if (healthDataStore != null) {
            healthDataStore.connectService();
        }
    }

    public void disconnect() {
        if (healthDataStore != null) {
            healthDataStore.disconnectService();
        }
    }

    public HealthDataStore getHealthDataStore() {
        return healthDataStore;
    }

    public void requestPermissions(Activity activity) {
        if (healthDataStore == null) {
            if (connectionListener != null) {
                connectionListener.onConnectionFailed("HealthDataStore nu este inițializat");
            }
            return;
        }

        HealthPermissionManager pmsManager = new HealthPermissionManager(healthDataStore);

        try {
            // Solicită permisiuni direct
            pmsManager.requestPermissions(permissionKeys, activity).setResultListener(result -> {
                boolean allGranted = true;
                StringBuilder logBuilder = new StringBuilder("Rezultate permisiuni:\n");

                for (HealthPermissionManager.PermissionKey key : permissionKeys) {
                    boolean isGranted = result.getResultMap().get(key);
                    String permission = key.getDataType();
                    logBuilder.append(permission).append(": ")
                            .append(isGranted ? "GRANTED" : "DENIED").append("\n");

                    if (!isGranted) {
                        allGranted = false;
                    }
                }

                Log.d(TAG, logBuilder.toString());

                if (connectionListener != null) {
                    connectionListener.onConnected(allGranted);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Eroare la solicitarea permisiunilor: " + e.getMessage(), e);
            if (connectionListener != null) {
                connectionListener.onConnectionFailed("Eroare la solicitarea permisiunilor: " + e.getMessage());
            }
        }
    }

    private final HealthDataStore.ConnectionListener connListener = new HealthDataStore.ConnectionListener() {
        @Override
        public void onConnected() {
            Log.d(TAG, "Conectat cu succes la Samsung Health");
            if (connectionListener != null) {
                // După conectare, vom verifica/solicita permisiunile
                requestPermissions((Activity) context);
            }
        }

        @Override
        public void onConnectionFailed(HealthConnectionErrorResult error) {
            Log.e(TAG, "Conexiunea a eșuat: " + error.toString());
            if (connectionListener != null) {
                connectionListener.onConnectionFailed("Conexiunea a eșuat: " + error.toString());
            }
        }

        @Override
        public void onDisconnected() {
            Log.d(TAG, "Deconectat de la Samsung Health");
            if (connectionListener != null) {
                connectionListener.onConnectionFailed("Deconectat de la Samsung Health");
            }
        }
    };
}