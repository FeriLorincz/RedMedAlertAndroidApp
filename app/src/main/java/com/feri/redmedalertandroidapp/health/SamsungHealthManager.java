package com.feri.redmedalertandroidapp.health;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import com.samsung.android.sdk.healthdata.HealthPermissionManager;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SamsungHealthManager {

    private static final String TAG = "SamsungHealthManager";

    private final Context context;
    private HealthDataStore mStore;
    private HealthPermissionManager permissionManager;
    private Set<HealthPermissionManager.PermissionKey> permissionKeys;
    private HealthConnectionCallback connectionCallback;
    private HealthMonitoringService monitoringService;
    private boolean isConnected = false;

    public SamsungHealthManager(Context context) {
        this.context = context;
        this.permissionKeys = new HashSet<>();
        initializeHealthKit();
    }

    public HealthDataStore getHealthDataStore() {
        Log.d("SamsungHealthSDK", "Metoda getHealthDataStore() a fost apelată");
        return mStore;
    }

    private void initializeHealthKit() {
        try {
            Log.d("SamsungHealthSDK", "Metoda initializeHealthKit() a fost apelată");
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

        try {
            Log.d(TAG, "Setare chei permisiuni");

            // Use a simpler approach for permission keys
            permissionKeys.add(new HealthPermissionManager.PermissionKey(
                    com.samsung.android.sdk.healthdata.HealthConstants.StepCount.HEALTH_DATA_TYPE,
                    HealthPermissionManager.PermissionType.READ));

            permissionKeys.add(new HealthPermissionManager.PermissionKey(
                    com.samsung.android.sdk.healthdata.HealthConstants.HeartRate.HEALTH_DATA_TYPE,
                    HealthPermissionManager.PermissionType.READ));

            permissionKeys.add(new HealthPermissionManager.PermissionKey(
                    com.samsung.android.sdk.healthdata.HealthConstants.BloodPressure.HEALTH_DATA_TYPE,
                    HealthPermissionManager.PermissionType.READ));

            permissionKeys.add(new HealthPermissionManager.PermissionKey(
                    "com.samsung.health.oxygen_saturation",
                    HealthPermissionManager.PermissionType.READ));

            permissionKeys.add(new HealthPermissionManager.PermissionKey(
                    "com.samsung.health.sleep",
                    HealthPermissionManager.PermissionType.READ));

            Log.d(TAG, "Au fost configurate " + permissionKeys.size() + " chei de permisiuni");
        } catch (Exception e) {
            Log.e(TAG, "Eroare la configurarea permisiunilor", e);
        }
    }




//        Log.d("SamsungHealthSDK", "Setare chei permisiuni");
//
//        try {
//            // Lista completă de permisiuni Samsung Health
//            String[] healthDataTypes = {
//                    "com.samsung.health.heart_rate",
//                    "com.samsung.health.blood_pressure",
//                    "com.samsung.health.oxygen_saturation",
//                    "com.samsung.health.step_count",
//                    "com.samsung.health.sleep",
//                    "com.samsung.health.stress",
//                    "com.samsung.health.exercise",
//                    "com.samsung.health.body_temperature",
//                    "com.samsung.health.body_composition",
//                    "com.samsung.health.activity",
//
//                    // Adaugă și aceste permisiuni specifice pentru pachetul tău
//                    "com.sec.android.app.shealth.step_count",
//                    "com.sec.android.app.shealth.heart_rate",
//                    "com.sec.android.app.shealth.blood_pressure",
//                    "com.sec.android.app.shealth.sleep",
//                    "com.sec.android.app.shealth.blood_glucose"
//            };
//
//            // Adaugă permisiuni pentru fiecare tip de date
//            for (String dataType : healthDataTypes) {
//                try {
//                    permissionKeys.add(new HealthPermissionManager.PermissionKey(
//                            dataType,
//                            HealthPermissionManager.PermissionType.READ
//                    ));
//                    Log.d("SamsungHealthSDK", "Permisiune adăugată pentru: " + dataType);
//                } catch (Exception e) {
//                    Log.e("SamsungHealthSDK", "Eroare la adăugarea permisiunii pentru: " + dataType, e);
//                }
//            }
//        } catch (Exception e) {
//            Log.e("SamsungHealthSDK", "Eroare la configurarea permisiunilor", e);
//        }
//    }



//    private void setupPermissionKeys() {
//        Log.d("SamsungHealthSDK", "Metoda setupPermissionKeys() a fost apelată");
//        // Heart Rate
//        permissionKeys.add(new HealthPermissionManager.PermissionKey(
//                "com.samsung.health.heart_rate",
//                HealthPermissionManager.PermissionType.READ
//        ));
//
//        // Blood Pressure
//        permissionKeys.add(new HealthPermissionManager.PermissionKey(
//                "com.samsung.health.blood_pressure",
//                HealthPermissionManager.PermissionType.READ
//        ));
//
//        // Blood Oxygen
//        permissionKeys.add(new HealthPermissionManager.PermissionKey(
//                "com.samsung.health.oxygen_saturation",
//                HealthPermissionManager.PermissionType.READ
//        ));
//
//        // Adăugăm permisiuni pentru alți senzori
//        permissionKeys.add(new HealthPermissionManager.PermissionKey(
//                "com.samsung.health.step_count",
//                HealthPermissionManager.PermissionType.READ
//        ));
//
//        permissionKeys.add(new HealthPermissionManager.PermissionKey(
//                "com.samsung.health.sleep",
//                HealthPermissionManager.PermissionType.READ
//        ));
//
//        permissionKeys.add(new HealthPermissionManager.PermissionKey(
//                "com.samsung.health.stress",
//                HealthPermissionManager.PermissionType.READ
//        ));
//
//        // Exercise
//        permissionKeys.add(new HealthPermissionManager.PermissionKey(
//                "com.samsung.health.exercise",
//                HealthPermissionManager.PermissionType.READ
//        ));
//
//        // Body Temperature (dacă Watch 7 suportă)
//        permissionKeys.add(new HealthPermissionManager.PermissionKey(
//                "com.samsung.health.body_temperature",
//                HealthPermissionManager.PermissionType.READ
//        ));
//    }

    public void forceRequestPermissions(Activity activity) {
        if (!isConnected()) {
            Log.e(TAG, "Nu se pot cere permisiuni: Samsung Health nu este conectat");
            connect(); // Încearcă să conecteze înainte

            // Așteaptă puțin pentru conectare
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        try {
            Log.d(TAG, "Forțarea cererii de permisiuni pentru Samsung Health");
            if (permissionManager == null) {
                Log.e(TAG, "PermissionManager este null!");
                return;
            }

            permissionManager.requestPermissions(permissionKeys, activity)
                    .setResultListener(result -> {
                        Map<HealthPermissionManager.PermissionKey, Boolean> resultMap = result.getResultMap();

                        // Logăm rezultatele detaliat
                        StringBuilder sb = new StringBuilder();
                        for (Map.Entry<HealthPermissionManager.PermissionKey, Boolean> entry : resultMap.entrySet()) {
                            sb.append(entry.getKey().getDataType())
                                    .append(": ")
                                    .append(entry.getValue() ? "GRANTED" : "DENIED")
                                    .append("\n");
                        }
                        Log.d(TAG, "Rezultate permisiuni:\n" + sb.toString());

                        // Reîncercăm după afișarea mesajului de dialog
                        if (resultMap.containsValue(Boolean.FALSE)) {
                            Log.d(TAG, "Unele permisiuni au fost refuzate, reîncercăm");
                            // Reîncercăm după o scurtă pauză
                            new Handler().postDelayed(() -> {
                                try {
                                    permissionManager.requestPermissions(permissionKeys, activity);
                                } catch (Exception e) {
                                    Log.e(TAG, "Eroare la reîncercarea permisiunilor", e);
                                }
                            }, 2000);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Eroare la cererea permisiunilor", e);
        }
    }

    public void requestPermissions(Activity activity) {
        Log.d("SamsungHealthSDK", "Cerere permisiuni Samsung Health");

        if (!isConnected()) {
            Log.e(TAG, "Nu se pot cere permisiuni: Samsung Health nu este conectat");
            if (connectionCallback != null) {
                connectionCallback.onPermissionsDenied();
            }
            return;
        }

        try {
            // Log toate permisiunile cerute
            for (HealthPermissionManager.PermissionKey key : permissionKeys) {
                Log.d(TAG, "Cerem permisiune pentru: " + key.getDataType());
            }

            permissionManager.requestPermissions(permissionKeys, activity)
                    .setResultListener(result -> {
                        Map<HealthPermissionManager.PermissionKey, Boolean> resultMap = result.getResultMap();

                        // Logăm rezultatele detaliat
                        StringBuilder sb = new StringBuilder();
                        for (Map.Entry<HealthPermissionManager.PermissionKey, Boolean> entry : resultMap.entrySet()) {
                            sb.append(entry.getKey().getDataType())
                                    .append(": ")
                                    .append(entry.getValue() ? "GRANTED" : "DENIED")
                                    .append("\n");
                        }
                        Log.d(TAG, "Rezultate permisiuni:\n" + sb.toString());

                        // Verificăm câte permisiuni au fost refuzate
                        long deniedCount = resultMap.entrySet().stream()
                                .filter(entry -> !entry.getValue())
                                .count();

                        if (deniedCount > 0) {
                            Log.e(TAG, deniedCount + " permisiuni au fost refuzate");
                            if (connectionCallback != null) {
                                connectionCallback.onPermissionsDenied();
                            }
                        } else {
                            Log.d(TAG, "Toate permisiunile au fost acordate");
                            if (connectionCallback != null) {
                                connectionCallback.onPermissionsGranted();
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Eroare la cererea permisiunilor", e);
            if (connectionCallback != null) {
                connectionCallback.onPermissionsDenied();
            }
        }
    }


    private boolean requestPermissionDirectly() {
        try {
            if (permissionManager == null || !isConnected()) {
                Log.e(TAG, "Nu se pot cere permisiuni: PermissionManager este null sau Samsung Health nu este conectat");
                return false;
            }

            // Obține permisiunile curente
            Map<HealthPermissionManager.PermissionKey, Boolean> currentPermissions =
                    permissionManager.isPermissionAcquired(permissionKeys);

            // Verifică dacă toate permisiunile sunt deja acordate
            boolean allGranted = true;
            for (Map.Entry<HealthPermissionManager.PermissionKey, Boolean> entry : currentPermissions.entrySet()) {
                if (!entry.getValue()) {
                    allGranted = false;
                    Log.e(TAG, "Permisiunea lipsă: " + entry.getKey().getDataType());
                }
            }

            if (allGranted) {
                Log.d(TAG, "Toate permisiunile sunt deja acordate");
                return true;
            } else {
                Log.d(TAG, "Unele permisiuni lipsesc, se solicită direct");
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Eroare la verificarea permisiunilor: " + e.getMessage(), e);
            return false;
        }
    }



    public void connect() {
        Log.d("SamsungHealthSDK", "Încercare de conectare la Samsung Health cu metoda connect()");
        if (mStore != null && !isConnected) {
            try {
                // Încearcă să conectezi serviciul
                Log.d("SamsungHealthSDK", "Apel mStore.connectService()");
                mStore.connectService();

                // Așteaptă un răspuns de la listener înainte de a considera conectarea finalizată
                // Statusul conectării va fi actualizat prin connectionListener
            } catch (Exception e) {
                Log.e("SamsungHealthSDK", "Eroare la conectare: " + e.getMessage(), e);
            }
        } else {
            Log.d("SamsungHealthSDK", "Nu s-a putut conecta: mStore=" + (mStore != null) + ", isConnected=" + isConnected);
        }
    }

    public void disconnect() {
        Log.d("SamsungHealthSDK", "Metoda disconnect() a fost apelată");
        if (mStore != null) {
            stopMonitoring(context);
            mStore.disconnectService();
            isConnected = false;
        }
    }

    public boolean isConnected() {
        Log.d("SamsungHealthSDK", "Metoda isConnected() a fost apelată");
        return isConnected;
    }

    private final HealthDataStore.ConnectionListener connectionListener =
            new HealthDataStore.ConnectionListener() {
                @Override
                public void onConnected() {
                    Log.d(TAG, "Connected to Samsung Health");
                    isConnected = true;
                    if (connectionCallback != null) {
                        connectionCallback.onConnected();
                    }
                }

                @Override
                public void onConnectionFailed(HealthConnectionErrorResult error) {
                    Log.e(TAG, "Connection to Samsung Health failed: " + error.toString());
                    isConnected = false;
                    if (connectionCallback != null) {
                        connectionCallback.onConnectionFailed(error);
                    }
                }

                @Override
                public void onDisconnected() {
                    Log.d(TAG, "Disconnected from Samsung Health");
                    isConnected = false;
                    if (connectionCallback != null) {
                        connectionCallback.onDisconnected();
                    }
                }
            };

    public void setConnectionCallback(HealthConnectionCallback callback) {
        Log.d("SamsungHealthSDK", "Metoda setConnectionCallback() a fost apelată");
        this.connectionCallback = callback;
    }

    public boolean isWatchConnected() {
        Log.d("SamsungHealthSDK", "Metoda isWatchConnected() a fost apelată");
        if (!isConnected) {
            return false;
        }

        // Verificăm dacă putem accesa datele de la ceas
        try {
            // O metodă simplă pentru a verifica dacă putem citi date
            return testWatchConnection();
        } catch (Exception e) {
            Log.e(TAG, "Error checking watch connection: " + e.getMessage());
            return false;
        }
    }

    private boolean testWatchConnection() {
        Log.d("SamsungHealthSDK", "Metoda testWatchConnection() a fost apelată");
        // Încercăm să citim un set minim de date pentru a verifica conexiunea
        final boolean[] result = {false};
        final CountDownLatch latch = new CountDownLatch(1);

        if (mStore == null) {
            return false;
        }

        HealthDataReader testReader = new HealthDataReader(mStore);
        testReader.readLatestData(new HealthDataReader.HealthDataListener() {
            @Override
            public void onDataReceived(Map<String, Double> data) {
                // Verificăm dacă datele conțin cel puțin o măsurătoare
                result[0] = !data.isEmpty();
                latch.countDown();
            }

            @Override
            public void onDataReadError(String message) {
                Log.e(TAG, "Watch connection test failed: " + message);
                result[0] = false;
                latch.countDown();
            }
        });

        try {
            // Așteptăm maxim 5 secunde pentru rezultat
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "Watch connection test interrupted");
            return false;
        }

        return result[0];
    }

    public String getWatchStatus() {
        Log.d("SamsungHealthSDK", "Metoda getWatchStatus() a fost apelată");
        if (!isConnected) {
            return "Samsung Health SDK: Neconectat";
        }

        boolean watchConnected = isWatchConnected();
        if (watchConnected) {
            return "Samsung Galaxy Watch 7: Conectat";
        } else {
            return "Samsung Health SDK: Conectat, dar ceasul nu este detectat";
        }
    }

    public void startMonitoring(Context context) {
        Log.d("SamsungHealthSDK", "Metoda startMonitoring() a fost apelată");
        if (mStore != null && isConnected) {
            Intent serviceIntent = new Intent(context, HealthMonitoringService.class);
            context.startService(serviceIntent);

            monitoringService = new HealthMonitoringService();
            monitoringService.startMonitoring(mStore);
        } else {
            Log.e(TAG, "Cannot start monitoring: Health store is null or not connected");
        }
    }

    public void stopMonitoring(Context context) {
        Log.d("SamsungHealthSDK", "Metoda stopMonitoring() a fost apelată");
        if (monitoringService != null) {
            monitoringService.stopMonitoring();
            Intent serviceIntent = new Intent(context, HealthMonitoringService.class);
            context.stopService(serviceIntent);
        }
    }
}