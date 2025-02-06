package com.feri.redmedalertandroidapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.feri.redmedalertandroidapp.auth.service.AuthApiService;
import com.feri.redmedalertandroidapp.auth.service.AuthService;
import com.feri.redmedalertandroidapp.auth.ui.LoginActivity;
import com.feri.redmedalertandroidapp.dashboard.DashboardActivity;
import com.feri.redmedalertandroidapp.health.HealthConnectionCallback;
import com.feri.redmedalertandroidapp.health.SamsungHealthManager;
import com.feri.redmedalertandroidapp.notification.NotificationSettingsActivity;
import com.feri.redmedalertandroidapp.util.PermissionHelper;
import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private AuthService authService;
    private SamsungHealthManager healthManager;
    private Button btnConnectWatch;
    private View watchStatusIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        initializeServices();
        initializeViews();
        setupSmartWatchConnection();
        setupListeners();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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

    private void initializeViews() {
        btnConnectWatch = findViewById(R.id.btnConnectWatch);
        watchStatusIndicator = findViewById(R.id.watchStatusIndicator);
        Button btnDashboard = findViewById(R.id.btnDashboard);
        Button btnNotificationSettings = findViewById(R.id.btnNotificationSettings);
        Button btnLogout = findViewById(R.id.btnLogout);
    }

    private void updateSmartWatchUI(boolean isConnected) {
        runOnUiThread(() -> {
            btnConnectWatch.setText(isConnected ? "Deconectare Smartwatch" : "Conectare Smartwatch");
            watchStatusIndicator.setBackgroundResource(
                    isConnected ? R.drawable.status_connected : R.drawable.status_disconnected
            );

            if (isConnected) {
                healthManager.startMonitoring(this);
                Toast.makeText(this, "Smartwatch conectat cu succes", Toast.LENGTH_SHORT).show();
            } else {
                healthManager.stopMonitoring(this);
            }
        });
    }

    private void setupSmartWatchConnection() {
        healthManager = new SamsungHealthManager(this);
        healthManager.setConnectionCallback(new HealthConnectionCallback() {
            @Override
            public void onConnected() {
                updateSmartWatchUI(true);
                healthManager.requestPermissions(MainActivity.this);
            }

            @Override
            public void onConnectionFailed(HealthConnectionErrorResult error) {
                updateSmartWatchUI(false);
                Toast.makeText(MainActivity.this,
                        "Eroare conectare: " + error.toString(),
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Connection failed: " + error.toString());
            }

            @Override
            public void onDisconnected() {
                updateSmartWatchUI(false);
                Toast.makeText(MainActivity.this,
                        "Smartwatch deconectat",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionsGranted() {
                healthManager.startMonitoring(MainActivity.this);
                Toast.makeText(MainActivity.this,
                        "Permisiuni acordate pentru Samsung Health",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionsDenied() {
                Toast.makeText(MainActivity.this,
                        "Permisiunile sunt necesare pentru monitorizare",
                        Toast.LENGTH_LONG).show();
            }
        });
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
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (healthManager != null) {
            healthManager.disconnect();
        }
    }
}