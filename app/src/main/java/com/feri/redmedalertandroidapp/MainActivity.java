package com.feri.redmedalertandroidapp;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Switch;
import android.widget.TextView;
import android.graphics.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;
import retrofit2.Callback;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import java.util.Set;
import android.content.pm.PackageInfo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.feri.redmedalertandroidapp.api.HealthDataApiManager;
import com.feri.redmedalertandroidapp.api.RetrofitClient;
import com.feri.redmedalertandroidapp.api.config.ApiClient;
import com.feri.redmedalertandroidapp.api.service.ApiCallback;
import com.feri.redmedalertandroidapp.api.service.ApiService;
import com.feri.redmedalertandroidapp.auth.service.AuthApiService;
import com.feri.redmedalertandroidapp.auth.service.AuthService;
import com.feri.redmedalertandroidapp.auth.ui.LoginActivity;
import com.feri.redmedalertandroidapp.dashboard.DashboardActivity;
import com.feri.redmedalertandroidapp.health.HealthConnectManager;
import com.feri.redmedalertandroidapp.notification.NotificationSettingsActivity;
import com.feri.redmedalertandroidapp.settings.ServerConfigActivity;
import com.feri.redmedalertandroidapp.util.PermissionHelper;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private AuthService authService;
    private Button btnConnectWatch;
    private View watchStatusIndicator;
    private TextView sensorDataText;
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    private Runnable dataUpdateRunnable;
    private boolean isMonitoring = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private HealthConnectManager healthConnectManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "Aplicație pornită - onCreate()");

        initializeServices();
        initializeViews();
        checkPermissions();

        // Mai întâi testăm conexiunea la server
        testServerConnection();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Verifică dacă Health Connect este disponibil
        checkHealthConnectAvailability();

        // Inițializează Health Connect Manager
        Log.d(TAG, "Inițializare Health Connect Manager");
        healthConnectManager = new HealthConnectManager(this);
        healthConnectManager.registerForActivityResult(this);
        healthConnectManager.setListener(new HealthConnectManager.HealthConnectListener() {
            @Override
            public void onConnected() {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this,
                            "Conectat la Health Connect",
                            Toast.LENGTH_SHORT).show();
                    updateSmartWatchUI(true);
                    Log.d(TAG, "Health Connect conectat cu succes");
                });
            }

            @Override
            public void onConnectionFailed(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this,
                            "Eroare Health Connect: " + error,
                            Toast.LENGTH_LONG).show();
                    updateSmartWatchUI(false);
                    Log.e(TAG, "Health Connect connection failed: " + error);
                });
            }

            @Override
            public void onDataReceived(Map<String, Double> data) {
                runOnUiThread(() -> {
                    if (data.isEmpty()) {
                        Toast.makeText(MainActivity.this,
                                "Nu s-au primit date de la Health Connect",
                                Toast.LENGTH_SHORT).show();
                        if (sensorDataText != null) {
                            sensorDataText.setText("Nu există date disponibile");
                        }
                        return;
                    }

                    // Afișează datele în UI
                    StringBuilder sb = new StringBuilder();
                    for (Map.Entry<String, Double> entry : data.entrySet()) {
                        sb.append(formatSensorName(entry.getKey())).append(": ")
                                .append(formatSensorValue(entry.getKey(), entry.getValue())).append("\n");
                    }
                    sb.append("\nUltima actualizare: ")
                            .append(new SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                    .format(new Date()));

                    if (sensorDataText != null) {
                        sensorDataText.setText(sb.toString());
                    }

                    // Trimitere date către server
                    Log.d(TAG, "Trimitem datele către server: " + data.toString());
                    ApiClient.getInstance(MainActivity.this).uploadHealthData(data, new ApiCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {
                                Log.d(TAG, "Date trimise cu succes către server");
                                Toast.makeText(MainActivity.this,
                                        "✓ Date trimise cu succes către server",
                                        Toast.LENGTH_SHORT).show();
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                Log.e(TAG, "Eroare la trimiterea datelor către server: " + error);
                                Toast.makeText(MainActivity.this,
                                        "✗ Eroare trimitere date: " + error,
                                        Toast.LENGTH_LONG).show();
                            });
                        }
                    });
                });
            }
        });

        setupListeners();
        setupSensorDataMonitoring();

        // Verifică și afișează dispozitivele Bluetooth conectate
        logConnectedBluetoothDevices();

        Log.d(TAG, "MainActivity onCreate() completat cu succes");
    }

    private void checkHealthConnectAvailability() {
        try {
            // Verifică dacă Health Connect este instalat
            getPackageManager().getPackageInfo("com.google.android.apps.healthdata", 0);
            Log.d(TAG, "Health Connect este instalat");
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Health Connect nu este instalat");
            Toast.makeText(this,
                    "Health Connect nu este instalat. Aplicația va folosi date simulate pentru testare.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void initializeViews() {
        btnConnectWatch = findViewById(R.id.btnConnectWatch);
        watchStatusIndicator = findViewById(R.id.watchStatusIndicator);
        Button btnDashboard = findViewById(R.id.btnDashboard);
        Button btnNotificationSettings = findViewById(R.id.btnNotificationSettings);
        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnServerConfig = findViewById(R.id.btnServerConfig);
        Button btnSamsungHealthSettings = findViewById(R.id.btnSamsungHealthSettings);

        // Configurare listener pentru butonul Health Connect
        if (btnSamsungHealthSettings != null) {
            btnSamsungHealthSettings.setText("Test Health Connect");
            btnSamsungHealthSettings.setOnClickListener(v -> {
                testHealthConnectConnection();
            });
        }

        // Configurare listener pentru butonul de configurare server
        if (btnServerConfig != null) {
            btnServerConfig.setOnClickListener(v -> {
                startActivity(new Intent(this, ServerConfigActivity.class));
            });
        }
    }

    private void checkPermissions() {
        // Lista de permisiuni necesare
        String[] permissions = new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        // Pentru Android 12 (API 31) și mai nou, adăugăm permisiunile Bluetooth
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            permissions = new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            };
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            permissions = new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN
            };
        }

        // Verifică și solicită permisiunile
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
                break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean allPermissionsGranted = true;
        if (grantResults.length > 0) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
        } else {
            allPermissionsGranted = false;
        }

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (allPermissionsGranted) {
                Log.d(TAG, "Toate permisiunile au fost acordate");
                logConnectedBluetoothDevices();
            } else {
                Log.e(TAG, "Unele permisiuni au fost refuzate");
                Toast.makeText(this, "Aplicația necesită aceste permisiuni pentru funcționare corectă",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void logConnectedBluetoothDevices() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                Log.e(TAG, "Dispozitivul nu suportă Bluetooth");
                return;
            }

            if (!bluetoothAdapter.isEnabled()) {
                Log.e(TAG, "Bluetooth nu este activat");
                return;
            }

            // Verificăm permisiunile explicite pentru Bluetooth
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "Lipsă permisiuni BLUETOOTH_CONNECT pentru Android 12+");
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                            102);
                    return;
                }
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "Lipsă permisiuni BLUETOOTH pentru Android <12");
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.BLUETOOTH},
                            101);
                    return;
                }
            }

            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                Log.d(TAG, "Dispozitive Bluetooth asociate:");
                for (BluetoothDevice device : pairedDevices) {
                    Log.d(TAG, "  - Nume: " + device.getName() + ", Adresă: " + device.getAddress());
                }
            } else {
                Log.d(TAG, "Nu există dispozitive Bluetooth asociate");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException: Permisiuni Bluetooth insuficiente", e);
        } catch (Exception e) {
            Log.e(TAG, "Eroare la verificarea dispozitivelor Bluetooth", e);
        }
    }

    private void initializeServices() {
        // Inițializare AuthService
        Retrofit retrofit = com.feri.redmedalertandroidapp.api.RetrofitClient.getClient();
        AuthApiService authApiService = retrofit.create(AuthApiService.class);
        authService = new AuthService(this, authApiService);

        // Verifică dacă utilizatorul este autentificat
        if (!authService.isLoggedIn()) {
            startLoginActivity();
            return;
        }

        // Verifică și cere permisiunile necesare
        PermissionHelper.requestNotificationPermission(this);
    }

    private void setupListeners() {
        btnConnectWatch.setOnClickListener(v -> {
            try {
                if (!authService.isLoggedIn()) {
                    Toast.makeText(this,
                            "Sesiune expirată. Vă rugăm să vă autentificați din nou.",
                            Toast.LENGTH_LONG).show();
                    logout();
                    return;
                }

                if (healthConnectManager != null) {
                    if (healthConnectManager.isConnected()) {
                        healthConnectManager.disconnect();
                        updateSmartWatchUI(false);
                    } else {
                        healthConnectManager.requestPermissions();
                    }
                } else {
                    Toast.makeText(this,
                            "Health Connect Manager nu este inițializat",
                            Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error with Health Connect connection: " + e.getMessage());
                Toast.makeText(this,
                        "Eroare la conectarea Health Connect: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });

        findViewById(R.id.btnDashboard).setOnClickListener(v -> {
            if (!authService.isLoggedIn()) {
                Toast.makeText(this,
                        "Sesiune expirată. Vă rugăm să vă autentificați din nou.",
                        Toast.LENGTH_LONG).show();
                logout();
                return;
            }
            startActivity(new Intent(this, DashboardActivity.class));
        });

        findViewById(R.id.btnNotificationSettings).setOnClickListener(v -> {
            if (!authService.isLoggedIn()) {
                Toast.makeText(this,
                        "Sesiune expirată. Vă rugăm să vă autentificați din nou.",
                        Toast.LENGTH_LONG).show();
                logout();
                return;
            }
            startActivity(new Intent(this, NotificationSettingsActivity.class));
        });

        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());
    }

    private void setupSensorDataMonitoring() {
        sensorDataText = findViewById(R.id.sensorDataText);
        if (sensorDataText == null) {
            Log.e(TAG, "sensorDataText not found in layout");
            return;
        }

        uiHandler = new Handler(Looper.getMainLooper());
    }

    private void startSensorDataUI() {
        if (isMonitoring) return;
        isMonitoring = true;

        dataUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (healthConnectManager != null && healthConnectManager.isConnected()) {
                    healthConnectManager.readHealthData();
                }
                uiHandler.postDelayed(this, 10000); // Actualizare la fiecare 10 secunde
            }
        };
        uiHandler.post(dataUpdateRunnable);
        Log.d(TAG, "Started sensor data UI updates");
    }

    private void stopSensorDataUI() {
        isMonitoring = false;
        if (dataUpdateRunnable != null) {
            uiHandler.removeCallbacks(dataUpdateRunnable);
        }
        Log.d(TAG, "Stopped sensor data UI updates");
    }

    private String formatSensorName(String key) {
        switch (key) {
            case "heart_rate": return "Puls";
            case "blood_oxygen": return "Saturație oxigen";
            case "temperature": return "Temperatură";
            case "blood_pressure_systolic": return "Tensiune sistolică";
            case "blood_pressure_diastolic": return "Tensiune diastolică";
            case "stress_level": return "Nivel stres";
            case "steps": return "Pași";
            default: return key;
        }
    }

    private String formatSensorValue(String key, Double value) {
        if (value == null) return "N/A";

        switch (key) {
            case "heart_rate": return String.format("%.0f BPM", value);
            case "blood_oxygen": return String.format("%.1f%%", value);
            case "temperature": return String.format("%.1f°C", value);
            case "blood_pressure_systolic":
            case "blood_pressure_diastolic": return String.format("%.0f mmHg", value);
            case "stress_level": return String.format("%.0f/100", value);
            case "steps": return String.format("%.0f pași", value);
            default: return String.format("%.2f", value);
        }
    }

    private void updateSmartWatchUI(boolean isConnected) {
        runOnUiThread(() -> {
            btnConnectWatch.setText(isConnected ? "Deconectare Health Connect" : "Conectare Health Connect");
            watchStatusIndicator.setBackgroundResource(
                    isConnected ? R.drawable.status_connected : R.drawable.status_disconnected
            );

            if (isConnected) {
                startSensorDataUI();
                Toast.makeText(this, "Health Connect conectat cu succes", Toast.LENGTH_SHORT).show();
            } else {
                stopSensorDataUI();
                if (sensorDataText != null) {
                    sensorDataText.setText("Health Connect deconectat");
                }
            }
        });
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void logout() {
        if (healthConnectManager != null) {
            healthConnectManager.disconnect();
        }
        authService.logout();
        startLoginActivity();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!authService.isLoggedIn()) {
            startLoginActivity();
            return;
        }

        // Verifică dacă Health Connect este conectat
        if (healthConnectManager != null && !healthConnectManager.isConnected()) {
            Log.d(TAG, "Health Connect nu este conectat în onResume");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (healthConnectManager != null) {
            healthConnectManager.cleanup();
        }
        stopSensorDataUI();
    }

    private void testServerConnection() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.testConnection().enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Conexiune reușită la server");
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Server OK", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Log.e(TAG, "Eroare la conexiunea cu serverul: " + response.code());
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Eroare server: " + response.code(), Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "Eroare la conexiunea cu serverul: " + t.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Server offline", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void testHealthConnectConnection() {
        Log.d(TAG, "Testăm conexiunea Health Connect");
        if (healthConnectManager != null) {
            if (healthConnectManager.isConnected()) {
                Toast.makeText(this, "Health Connect este conectat și funcțional", Toast.LENGTH_SHORT).show();
                healthConnectManager.readHealthData();
            } else {
                Toast.makeText(this, "Health Connect nu este conectat", Toast.LENGTH_SHORT).show();
                healthConnectManager.requestPermissions();
            }
        } else {
            Toast.makeText(this, "Health Connect nu este inițializat", Toast.LENGTH_SHORT).show();
        }
    }
}