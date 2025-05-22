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
import com.feri.redmedalertandroidapp.health.HealthConnectionCallback;
import com.feri.redmedalertandroidapp.health.HealthDataReader;
import com.feri.redmedalertandroidapp.health.SamsungHealthConnector;
import com.feri.redmedalertandroidapp.health.SamsungHealthManager;
import com.feri.redmedalertandroidapp.health.SensorDataSimulator;
import com.feri.redmedalertandroidapp.notification.NotificationSettingsActivity;
import com.feri.redmedalertandroidapp.settings.ServerConfigActivity;
import com.feri.redmedalertandroidapp.util.PermissionHelper;
import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult;
import com.feri.redmedalertandroidapp.settings.ServerConfigActivity;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private AuthService authService;
    private SamsungHealthManager healthManager;
    private Button btnConnectWatch;
    private View watchStatusIndicator;
    private TextView sensorDataText;
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    private Runnable dataUpdateRunnable;
    private boolean isMonitoring = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private SamsungHealthConnector healthConnector;
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

        // Verifică dacă Samsung Health este instalat
        if (!isSamsungHealthInstalled()) {
            Toast.makeText(this, "Samsung Health nu este instalat. Vă rugăm să-l instalați din Galaxy Store",
                    Toast.LENGTH_LONG).show();
            Log.e(TAG, "Samsung Health nu este instalat pe dispozitiv");

            // Încearcă să deschidă Galaxy Store pentru instalare
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("samsungapps://ProductDetail/com.sec.android.app.shealth")));
            } catch (Exception e) {
                Log.e(TAG, "Nu se poate deschide Galaxy Store", e);
                Toast.makeText(this, "Nu se poate deschide Galaxy Store", Toast.LENGTH_SHORT).show();
            }

            // NU oprește inițializarea - continuă cu Health Connect
            Log.d(TAG, "Continuăm cu Health Connect în ciuda lipsei Samsung Health");
        } else {
            Log.d(TAG, "Samsung Health este instalat");

            // Verifică versiunea Samsung Health - cu protecție împotriva erorilor
            try {
                String[] possiblePackages = {
                        "com.sec.android.app.shealth",
                        "com.sec.android.app.shealth:remote",
                        "com.samsung.android.app.health",
                        "com.samsung.android.health"
                };
                boolean foundVersion = false;

                for (String packageName : possiblePackages) {
                    try {
                        PackageInfo packageInfo = getPackageManager().getPackageInfo(packageName, 0);
                        Log.d(TAG, "Versiune Samsung Health (" + packageName + "): " + packageInfo.versionName);
                        foundVersion = true;
                        break;
                    } catch (Exception e) {
                        Log.d(TAG, "Nu s-a putut obține versiunea pentru pachetul: " + packageName);
                    }
                }

                if (!foundVersion) {
                    Log.w(TAG, "Nu s-a putut obține versiunea Samsung Health pentru niciun pachet cunoscut");
                }
            } catch (Exception e) {
                Log.e(TAG, "Eroare generală la obținerea versiunii Samsung Health", e);
            }
        }

        // =================================================================
        // PRIORITATE: Inițializează Health Connect Manager (metoda principală)
        // =================================================================
        Log.d(TAG, "Inițializare Health Connect Manager");

        healthConnectManager = new HealthConnectManager(this);
        healthConnectManager.registerForActivityResult(this);
        healthConnectManager.setListener(new HealthConnectManager.HealthConnectListener() {
            @Override
            public void onConnected() {
                Toast.makeText(MainActivity.this,
                        "Conectat la Health Connect",
                        Toast.LENGTH_SHORT).show();
                updateSmartWatchUI(true);
                Log.d(TAG, "Health Connect conectat cu succes");
            }

            @Override
            public void onConnectionFailed(String error) {
                Toast.makeText(MainActivity.this,
                        "Eroare Health Connect: " + error,
                        Toast.LENGTH_LONG).show();
                updateSmartWatchUI(false);
                Log.e(TAG, "Health Connect connection failed: " + error);
            }

            @Override
            public void onDataReceived(Map<String, Double> data) {
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

                // =================================================================
                // TRIMITERE DATE CĂTRE SERVER
                // =================================================================
                Log.d(TAG, "Trimitem datele către server: " + data.toString());

                // Folosim ApiClient pentru a trimite datele
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
            }
        });

        // Configurează butonul principal de conectare
        Button btnConnectWatch = findViewById(R.id.btnConnectWatch);
        if (btnConnectWatch != null) {
            btnConnectWatch.setOnClickListener(v -> {
                Log.d(TAG, "Buton conectare apăsat - Health Connect");
                try {
                    if (healthConnectManager != null) {
                        healthConnectManager.requestPermissions();
                    } else {
                        Toast.makeText(MainActivity.this,
                                "Health Connect Manager nu este inițializat",
                                Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Eroare la apăsarea butonului de conectare", e);
                    Toast.makeText(MainActivity.this,
                            "Eroare: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }


//        // Modifică butonul de conectare pentru a folosi Health Connect
//        Button btnConnectWatch = findViewById(R.id.btnConnectWatch);
//        btnConnectWatch.setOnClickListener(v -> {
//            Log.d(TAG, "Buton conectare apăsat - folosim Health Connect");
//            healthConnectManager.requestPermissions();
//        });

        // =================================================================
        // OPȚIONAL: Păstrăm Samsung Health SDK ca backup (comentat pentru a evita conflictele)
        // =================================================================
    /*
    // Inițializăm noul SamsungHealthConnector doar ca backup
    healthConnector = new SamsungHealthConnector(this);
    healthConnector.setConnectionListener(new SamsungHealthConnector.ConnectionListener() {
        @Override
        public void onConnected(boolean permissionsGranted) {
            if (permissionsGranted) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this,
                            "Backup: Conectat la Samsung Health cu permisiuni",
                            Toast.LENGTH_SHORT).show();
                });
                startHealthMonitoring();
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this,
                            "Backup: Conectat la Samsung Health, dar unele permisiuni lipsesc",
                            Toast.LENGTH_LONG).show();
                });
            }
        }

        @Override
        public void onConnectionFailed(String error) {
            runOnUiThread(() -> {
                Log.w(TAG, "Samsung Health backup connection failed: " + error);
            });
        }
    });
    */

        // Configurează conexiunea la smartwatch după verificarea Samsung Health
        // setupSmartWatchConnection(); // Comentat pentru a evita conflictele
        setupListeners();
        setupSensorDataMonitoring();

        // Verifică și afișează dispozitivele Bluetooth conectate
        logConnectedBluetoothDevices();

        // Adăugăm butonul pentru testarea Health Connect
        Button btnTestSamsungHealth = findViewById(R.id.btnSamsungHealthSettings);
        if (btnTestSamsungHealth != null) {
            btnTestSamsungHealth.setText("Test Health Connect");
            btnTestSamsungHealth.setOnClickListener(v -> {
                testHealthConnectConnection();
            });
        }

        Log.d(TAG, "MainActivity onCreate() completat cu succes");

    }


    private void startHealthMonitoring() {
        if (healthConnector == null || healthConnector.getHealthDataStore() == null) {
            Toast.makeText(this, "Samsung Health nu este conectat", Toast.LENGTH_SHORT).show();
            return;
        }

        HealthDataReader reader = new HealthDataReader(healthConnector.getHealthDataStore());
        reader.readLatestData(new HealthDataReader.HealthDataListener() {
            @Override
            public void onDataReceived(Map<String, Double> data) {
                if (data.isEmpty()) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this,
                                "Nu s-au primit date de la Samsung Health",
                                Toast.LENGTH_SHORT).show();
                        if (sensorDataText != null) {
                            sensorDataText.setText("Nu există date disponibile");
                        }
                    });
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (Map.Entry<String, Double> entry : data.entrySet()) {
                        sb.append(formatSensorName(entry.getKey())).append(": ")
                                .append(formatSensorValue(entry.getKey(), entry.getValue())).append("\n");
                    }
                    sb.append("\nUltima actualizare: ")
                            .append(new SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                    .format(new Date()));

                    // Update UI with data
                    runOnUiThread(() -> {
                        if (sensorDataText != null) {
                            sensorDataText.setText(sb.toString());
                        }
                    });
                }
            }

            @Override
            public void onDataReadError(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this,
                            "Eroare la citirea datelor: " + message,
                            Toast.LENGTH_LONG).show();
                    if (sensorDataText != null) {
                        sensorDataText.setText("Eroare la citirea datelor: " + message);
                    }
                });
            }
        });
    }


    private void initializeViews() {
        btnConnectWatch = findViewById(R.id.btnConnectWatch);
        watchStatusIndicator = findViewById(R.id.watchStatusIndicator);
        Button btnDashboard = findViewById(R.id.btnDashboard);
        Button btnNotificationSettings = findViewById(R.id.btnNotificationSettings);
        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnServerConfig = findViewById(R.id.btnServerConfig);
        Button btnSamsungHealthSettings = findViewById(R.id.btnSamsungHealthSettings);

        // Configurare listener pentru butonul de setări Samsung Health
        btnSamsungHealthSettings.setOnClickListener(v -> {
            showSamsungHealthSettings();
        });

        // Configurare listener pentru butonul de configurare server
        if (btnServerConfig != null) {
            btnServerConfig.setOnClickListener(v -> {
                startActivity(new Intent(this, ServerConfigActivity.class));
            });
        }
    }

    private void showSamsungHealthSettings() {
        // Deschide dialogul pentru instrucțiuni despre activarea modului developer
        new android.app.AlertDialog.Builder(this)
                .setTitle("Activare mod developer Samsung Health")
                .setMessage("Pentru a permite accesul, trebuie să activați modul developer în Samsung Health:\n\n" +
                        "1. Deschideți Samsung Health\n" +
                        "2. Apăsați pe ⋮ (meniu) > Settings > About Samsung Health\n" +
                        "3. Apăsați de 10 ori rapid pe versiune\n" +
                        "4. Activați Developer Mode\n" +
                        "5. Activați Developer Mode for Data Read\n\n" +
                        "După ce ați terminat, reveniți la această aplicație.")
                .setPositiveButton("Deschide Samsung Health", (dialog, id) -> {
                    try {
                        Intent intent = getPackageManager().getLaunchIntentForPackage("com.sec.android.app.shealth");
                        if (intent != null) {
                            startActivity(intent);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Nu s-a putut deschide Samsung Health", e);
                    }
                })
                .setNegativeButton("Anulează", (dialog, id) -> {
                    dialog.dismiss();
                })
                .show();
    }


    // Metodă pentru a verifica și loga dispozitivele Bluetooth conectate
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
                // Pentru Android 12 (API 31) și mai nou, avem nevoie de BLUETOOTH_CONNECT
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "Lipsă permisiuni BLUETOOTH_CONNECT pentru Android 12+");
                    // Solicităm permisiunea, dar nu blocăm execuția
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                            102);
                    return;
                }
            } else {
                // Pentru versiuni mai vechi de Android, verificăm permisiunea BLUETOOTH
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "Lipsă permisiuni BLUETOOTH pentru Android <12");
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.BLUETOOTH},
                            101);
                    return;
                }
            }

            // După ce am verificat permisiunile, putem apela în siguranță getBondedDevices()
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
            // Pentru Android 6.0+ dar mai vechi de Android 12
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
                break;  // Ieșim din buclă după ce am solicitat toate permisiunile
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
                // Reîncercăm operațiunile care depind de aceste permisiuni
                if (healthManager != null) {
                    setupSmartWatchConnection();
                }
                // Reîncercăm să listăm dispozitivele Bluetooth
                logConnectedBluetoothDevices();
            } else {
                Log.e(TAG, "Unele permisiuni au fost refuzate");
                Toast.makeText(this, "Aplicația necesită aceste permisiuni pentru funcționare corectă",
                        Toast.LENGTH_LONG).show();
            }
        }
    }


    private boolean isSamsungHealthInstalled() {
        try {
            // Verifică pachetul corect pentru Samsung Health pe telefonul tău
            String[] healthPackages = {
                    "com.sec.android.app.shealth",
                    "com.sec.android.app.shealth:remote",
                    "com.samsung.android.app.health",
                    "com.samsung.android.health"
            };

            for (String packageName : healthPackages) {
                try {
                    getPackageManager().getPackageInfo(packageName, 0);
                    Log.d(TAG, "Samsung Health găsit cu numele de pachet: " + packageName);
                    return true;
                } catch (PackageManager.NameNotFoundException ex) {
                    Log.d(TAG, "Nu s-a găsit pachetul: " + packageName);
                }
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Eroare la verificarea Samsung Health: " + e.getMessage());
            return false;
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


    private void setupSmartWatchConnection() {
        Log.d(TAG, "Inițializare conexiune smartwatch");

        // Verifică dacă aplicația are permisiunile necesare
        checkAndRequestSamsungHealthPermissions();

        healthManager = new SamsungHealthManager(this);

        // Log starea inițială
        Log.d(TAG, "Stare inițială: HealthDataStore inițializat=" + (healthManager.getHealthDataStore() != null));

        healthManager.setConnectionCallback(new HealthConnectionCallback() {
            @Override
            public void onConnected() {
                Log.d(TAG, "Callback: Conectat la Samsung Health");
                updateSmartWatchUI(true);
                healthManager.requestPermissions(MainActivity.this);
            }

            @Override
            public void onConnectionFailed(HealthConnectionErrorResult error) {
                Log.e(TAG, "Callback: Eroare conectare: " + error.toString());
                updateSmartWatchUI(false);
                Toast.makeText(MainActivity.this,
                        "Eroare conectare: " + error.toString(),
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Connection failed: " + error.toString());
            }

            @Override
            public void onDisconnected() {
                Log.d(TAG, "Callback: Smartwatch deconectat");
                updateSmartWatchUI(false);
                Toast.makeText(MainActivity.this,
                        "Smartwatch deconectat",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionsGranted() {
                Log.d(TAG, "Callback: Permisiuni acordate pentru Samsung Health");
                healthManager.startMonitoring(MainActivity.this);
                Toast.makeText(MainActivity.this,
                        "Permisiuni acordate pentru Samsung Health",
                        Toast.LENGTH_SHORT).show();
                startSensorDataUI();
            }

            @Override
            public void onPermissionsDenied() {
                Log.e(TAG, "Callback: Permisiuni refuzate pentru Samsung Health");
                Toast.makeText(MainActivity.this,
                        "Permisiunile sunt necesare pentru monitorizare",
                        Toast.LENGTH_LONG).show();
            }
        });

        // Dialog cu instrucțiuni pentru permisiunile Samsung Health
        new android.app.AlertDialog.Builder(this)
                .setTitle("Permisiuni Samsung Health")
                .setMessage("Pentru ca aplicația să funcționeze corect, trebuie să activați TOATE permisiunile din Samsung Health. Când apare dialogul Samsung Health, activați Developer Mode și acordați toate permisiunile pentru aplicație.")
                .setPositiveButton("OK", (dialog, which) -> {
                    if (healthManager != null) {
                        healthManager.connect();
                        healthManager.forceRequestPermissions(this);
                    }
                })
                .setCancelable(false)
                .show();

        // Adăugăm conectare explicită după configurarea callback-ului
        Log.d(TAG, "Încercare explicită de conectare la Samsung Health");
        healthManager.connect();

        // Verificăm starea conexiunii după încercarea de conectare
        boolean isConnectedAfterAttempt = healthManager.isConnected();
        Log.d(TAG, "Stare conexiune după încercare: " + (isConnectedAfterAttempt ? "conectat" : "neconectat"));

        // Dacă încă nu suntem conectați, încercăm din nou după o mică întârziere
        if (!isConnectedAfterAttempt) {
            new Handler().postDelayed(() -> {
                Log.d(TAG, "Reîncercare conectare după întârziere");
                healthManager.connect();
                Log.d(TAG, "Stare conexiune după reîncercare: " + (healthManager.isConnected() ? "conectat" : "neconectat"));
            }, 2000); // Reîncercăm după 2 secunde
        }

        // Adaugă timeout pentru conexiune
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!healthManager.isConnected()) {
                Log.e(TAG, "Timeout: Samsung Health nu s-a conectat în 10 secunde");
                Toast.makeText(MainActivity.this,
                        "Conectarea la Samsung Health a întârziat. Încearcă din nou.",
                        Toast.LENGTH_LONG).show();

                // Oferă opțiunea de reîncercare manuală
                Button btnReconnect = new Button(this);
                btnReconnect.setText("Reconectează la Samsung Health");
                btnReconnect.setOnClickListener(v -> {
                    Log.d(TAG, "Reîncercare manuală de conectare");
                    healthManager.disconnect();
                    healthManager.connect();
                });

                // Adaugă butonul la layout dacă este necesar
                // sau afișează un dialog
            }
        }, 10000); // 10 secunde timeout
    }


    private void checkAndRequestSamsungHealthPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] requiredPermissions = {
                    android.Manifest.permission.BODY_SENSORS,
                    android.Manifest.permission.BODY_SENSORS_BACKGROUND,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.BLUETOOTH_CONNECT,
                    android.Manifest.permission.BLUETOOTH_SCAN
            };

            for (String permission : requiredPermissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, requiredPermissions, 100);
                    break;
                }
            }
        }
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

                if (healthManager.isConnected()) {
                    healthManager.disconnect();
                } else {
                    healthManager.connect();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error with smartwatch connection: " + e.getMessage());
                Toast.makeText(this,
                        "Eroare la conectarea smartwatch-ului: " + e.getMessage(),
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
        // Verificăm dacă TextView-ul pentru datele senzorilor există
        sensorDataText = findViewById(R.id.sensorDataText);
        if (sensorDataText == null) {
            Log.e(TAG, "sensorDataText not found in layout");
            return;
        }

        // Inițializăm handler-ul pentru actualizarea UI
        uiHandler = new Handler(Looper.getMainLooper());

        // Verificăm dacă ceasul este conectat
        if (healthManager != null && healthManager.isConnected()) {
            startSensorDataUI();
        }
    }

    // Adăugați aceste metode pentru actualizarea UI cu datele senzorilor
    private void startSensorDataUI() {
        if (isMonitoring) return;
        isMonitoring = true;

        dataUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                updateSensorDataUI();
                uiHandler.postDelayed(this, 3000); // Actualizare la fiecare 3 secunde
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

    private void updateSensorDataUI() {
        if (sensorDataText == null) {
            Log.e(TAG, "sensorDataText este null!");
            return;
        }

        if (healthManager == null || !healthManager.isConnected()) {
            String statusMessage = "Stare: HealthManager=" + (healthManager != null) +
                    ", Connected=" + (healthManager != null && healthManager.isConnected());
            sensorDataText.setText(statusMessage);
            Log.e(TAG, statusMessage);
            return;
        }

        // Obținem datele de la HealthDataReader
        HealthDataReader dataReader = new HealthDataReader(healthManager.getHealthDataStore());
        dataReader.readLatestData(new HealthDataReader.HealthDataListener() {
            @Override
            public void onDataReceived(Map<String, Double> data) {
                StringBuilder dataText = new StringBuilder();
                if (data.isEmpty()) {
                    dataText.append("Nu există date disponibile în acest moment");
                } else {
                    for (Map.Entry<String, Double> entry : data.entrySet()) {
                        dataText.append(formatSensorName(entry.getKey())).append(": ")
                                .append(formatSensorValue(entry.getKey(), entry.getValue())).append("\n");
                    }
                    dataText.append("\nUltima actualizare: ")
                            .append(new SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                    .format(new Date()));
                }

                runOnUiThread(() -> {
                    if (sensorDataText != null) {
                        sensorDataText.setText(dataText.toString());
                    }
                });
            }

            @Override
            public void onDataReadError(String message) {
                runOnUiThread(() -> {
                    if (sensorDataText != null) {
                        sensorDataText.setText("Eroare la citirea datelor: " + message);
                    }
                });
            }
        });
    }

    private String formatSensorName(String key) {
        switch (key) {
            case "heart_rate": return "Puls";
            case "blood_oxygen": return "Saturație oxigen";
            case "temperature": return "Temperatură";
            case "blood_pressure_systolic": return "Tensiune sistolică";
            case "blood_pressure_diastolic": return "Tensiune diastolică";
            case "stress_level": return "Nivel stres";
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
            default: return String.format("%.2f", value);
        }
    }

    // Modificați metoda updateSmartWatchUI() pentru a include și textView-ul pentru datele senzorilor
    private void updateSmartWatchUI(boolean isConnected) {
        runOnUiThread(() -> {
            btnConnectWatch.setText(isConnected ? "Deconectare Smartwatch" : "Conectare Smartwatch");
            watchStatusIndicator.setBackgroundResource(
                    isConnected ? R.drawable.status_connected : R.drawable.status_disconnected
            );

            if (isConnected) {
                healthManager.startMonitoring(this);
                startSensorDataUI(); // Adăugat aici
                Toast.makeText(this, "Smartwatch conectat cu succes", Toast.LENGTH_SHORT).show();
            } else {
                healthManager.stopMonitoring(this);
                stopSensorDataUI(); // Adăugat aici
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
        if (healthManager != null) {
            healthManager.disconnect();
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

        // Verifică dacă Samsung Health este conectat și cere permisiuni dacă e necesar
        if (healthManager != null) {
            if (!healthManager.isConnected()) {
                Log.d(TAG, "Încercare de conectare la Samsung Health");
                healthManager.connect();
            } else {
                Log.d(TAG, "Deja conectat la Samsung Health");
                healthManager.requestPermissions(this);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (healthManager != null) {
            healthManager.disconnect();
        }
    }

    private void testServerConnection() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.testConnection().enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Conexiune reușită la server");
                    runOnUiThread(() -> {
                        // Toast-ul să fie optional sau micuț
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


    // Metodă nouă pentru testarea Health Connect
    private void testHealthConnectConnection() {
        Log.d(TAG, "Testăm conexiunea Health Connect");
        if (healthConnectManager != null) {
            healthConnectManager.readHealthData();
        } else {
            Toast.makeText(this, "Health Connect nu este inițializat", Toast.LENGTH_SHORT).show();
        }
    }

    private void testSamsungHealthConnection() {
        if (healthConnector != null && healthConnector.getHealthDataStore() != null) {
            Log.d(TAG, "Testăm conexiunea Samsung Health...");

            HealthDataReader reader = new HealthDataReader(healthConnector.getHealthDataStore());
            reader.readLatestData(new HealthDataReader.HealthDataListener() {
                @Override
                public void onDataReceived(Map<String, Double> data) {
                    StringBuilder sb = new StringBuilder("Date primite de la Samsung Health:\n");
                    if (data.isEmpty()) {
                        sb.append("Nu s-au primit date");
                    } else {
                        for (Map.Entry<String, Double> entry : data.entrySet()) {
                            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                        }
                    }
                    Log.d(TAG, sb.toString());
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this,
                                "Test Samsung Health: " + (data.isEmpty() ? "Fără date" : data.size() + " măsurători primite"),
                                Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onDataReadError(String message) {
                    Log.e(TAG, "Eroare la citirea datelor: " + message);
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this,
                                "Eroare la citirea datelor: " + message,
                                Toast.LENGTH_LONG).show();
                    });
                }
            });
        } else {
            Log.e(TAG, "Nu se poate testa - Samsung Health nu este conectat");
            Toast.makeText(this, "Samsung Health nu este conectat pentru test", Toast.LENGTH_SHORT).show();
        }
    }

}