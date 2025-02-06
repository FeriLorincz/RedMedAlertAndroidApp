package com.feri.redmedalertandroidapp.auth.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.feri.redmedalertandroidapp.R;
import com.feri.redmedalertandroidapp.auth.service.AuthApiService;
import com.feri.redmedalertandroidapp.auth.service.AuthService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Retrofit;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputLayout newPasswordLayout;
    private TextInputLayout confirmPasswordLayout;
    private TextInputEditText newPasswordInput;
    private TextInputEditText confirmPasswordInput;
    private MaterialButton resetButton;
    private View progressBar;
    private AuthService authService;
    private String resetToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Obținem token-ul din intent
        resetToken = getIntent().getStringExtra("reset_token");
        if (resetToken == null) {
            Toast.makeText(this, "Token de resetare invalid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inițializare AuthService
        Retrofit retrofit = com.feri.redmedalertandroidapp.api.RetrofitClient.getClient();
        AuthApiService authApiService = retrofit.create(AuthApiService.class);
        authService = new AuthService(this, authApiService);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        newPasswordLayout = findViewById(R.id.newPasswordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);
        newPasswordInput = findViewById(R.id.newPasswordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        resetButton = findViewById(R.id.resetButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        resetButton.setOnClickListener(v -> attemptPasswordReset());
    }

    private void attemptPasswordReset() {
        String newPassword = newPasswordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();

        if (!validatePasswords(newPassword, confirmPassword)) {
            return;
        }

        showLoading(true);
        authService.resetPassword(resetToken, newPassword, new AuthService.ResetCallback() {
            @Override
            public void onSuccess() {
                showLoading(false);
                Toast.makeText(ResetPasswordActivity.this,
                        "Parola a fost resetată cu succes",
                        Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message) {
                showLoading(false);
                Toast.makeText(ResetPasswordActivity.this,
                        message,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validatePasswords(String newPassword, String confirmPassword) {
        boolean isValid = true;

        if (newPassword.isEmpty()) {
            newPasswordLayout.setError("Parola este obligatorie");
            isValid = false;
        } else if (newPassword.length() < 8) {
            newPasswordLayout.setError("Parola trebuie să aibă minim 8 caractere");
            isValid = false;
        } else {
            newPasswordLayout.setError(null);
        }

        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordLayout.setError("Parolele nu coincid");
            isValid = false;
        } else {
            confirmPasswordLayout.setError(null);
        }

        return isValid;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        resetButton.setEnabled(!show);
    }
}
