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

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputLayout emailLayout;
    private TextInputEditText emailInput;
    private MaterialButton resetButton;
    private View progressBar;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Inițializare AuthService
        Retrofit retrofit = com.feri.redmedalertandroidapp.api.RetrofitClient.getClient();
        AuthApiService authApiService = retrofit.create(AuthApiService.class);
        authService = new AuthService(this, authApiService);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        emailLayout = findViewById(R.id.emailLayout);
        emailInput = findViewById(R.id.emailInput);
        resetButton = findViewById(R.id.resetButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        resetButton.setOnClickListener(v -> attemptPasswordReset());
    }

    private void attemptPasswordReset() {
        String email = emailInput.getText().toString().trim();

        if (!validateEmail(email)) {
            return;
        }

        showLoading(true);
        authService.requestPasswordReset(email, new AuthService.ResetCallback() {
            @Override
            public void onSuccess() {
                showLoading(false);
                Toast.makeText(ForgotPasswordActivity.this,
                        "Email de resetare trimis cu succes. Verificați căsuța de email.",
                        Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onError(String message) {
                showLoading(false);
                Toast.makeText(ForgotPasswordActivity.this,
                        message,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            emailLayout.setError("Email-ul este obligatoriu");
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Email invalid");
            return false;
        }
        emailLayout.setError(null);
        return true;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        resetButton.setEnabled(!show);
    }
}
