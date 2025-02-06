package com.feri.redmedalertandroidapp.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.feri.redmedalertandroidapp.MainActivity;
import com.feri.redmedalertandroidapp.R;
import com.feri.redmedalertandroidapp.api.model.EmergencyContact;
import com.feri.redmedalertandroidapp.api.model.MedicalProfile;
import com.feri.redmedalertandroidapp.api.model.User;
import com.feri.redmedalertandroidapp.auth.service.AuthApiService;
import com.feri.redmedalertandroidapp.auth.service.AuthService;
import com.feri.redmedalertandroidapp.auth.ui.LoginActivity;
import com.feri.redmedalertandroidapp.viewmodel.EmergencyContactViewModel;
import com.feri.redmedalertandroidapp.viewmodel.MedicalProfileViewModel;
import com.feri.redmedalertandroidapp.viewmodel.UserViewModel;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import retrofit2.Retrofit;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DURATION = 2000;
    private AuthService authService;
    private Button btnContinue;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initializeServices();
        setupAnimations();
        setupButton();
        startSplashTimer();
    }

    private void initializeServices() {
        try {
            Retrofit retrofit = com.feri.redmedalertandroidapp.api.RetrofitClient.getClient();
            AuthApiService authApiService = retrofit.create(AuthApiService.class);
            authService = new AuthService(this, authApiService);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing services: " + e.getMessage());
            Toast.makeText(this, "Eroare la inițializarea serviciilor", Toast.LENGTH_LONG).show();
        }
    }

    private void setupAnimations() {
        ImageView logoImage = findViewById(R.id.splashLogo);
        TextView appNameText = findViewById(R.id.appNameText);
        TextView versionText = findViewById(R.id.versionText);

        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        logoImage.startAnimation(fadeIn);
        appNameText.startAnimation(fadeIn);
        versionText.startAnimation(fadeIn);
    }

    private void setupButton() {
        btnContinue = findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(v -> {
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
            }
            navigateNext();
        });
    }

    private void startSplashTimer() {
        handler = new Handler();
        handler.postDelayed(this::navigateNext, SPLASH_DURATION);
    }

    private void navigateNext() {
        try {
            // Verificăm dacă serviciile sunt inițializate corect
            if (authService == null) {
                Toast.makeText(this, "Eroare la inițializarea serviciilor", Toast.LENGTH_LONG).show();
                return;
            }

            // Verificăm dacă utilizatorul este autentificat
            if (!authService.isLoggedIn()) {
                Log.d(TAG, "User is not logged in, navigating to LoginActivity");
                startActivity(new Intent(this, LoginActivity.class));
            } else {
                Log.d(TAG, "User is logged in, checking if data is complete");
                String userId = authService.getUserId();
                // Resetăm autentificarea pentru a forța utilizatorul să se autentifice din nou
                authService.logout();
                startActivity(new Intent(this, LoginActivity.class));
            }

            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error during navigation: " + e.getMessage());
            Toast.makeText(this, "Eroare la navigare: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}