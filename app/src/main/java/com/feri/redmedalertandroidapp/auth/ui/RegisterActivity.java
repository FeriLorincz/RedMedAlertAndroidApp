package com.feri.redmedalertandroidapp.auth.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.feri.redmedalertandroidapp.R;
import com.feri.redmedalertandroidapp.api.dto.UserDTO;
import com.feri.redmedalertandroidapp.api.model.UserType;
import com.feri.redmedalertandroidapp.auth.service.AuthApiService;
import com.feri.redmedalertandroidapp.auth.service.AuthService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;

import retrofit2.Retrofit;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout firstNameLayout;
    private TextInputLayout lastNameLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout phoneLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout confirmPasswordLayout;

    private TextInputEditText firstNameInput;
    private TextInputEditText lastNameInput;
    private TextInputEditText emailInput;
    private TextInputEditText phoneInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;

    private MaterialButton registerButton;
    private View progressBar;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Retrofit retrofit = com.feri.redmedalertandroidapp.api.RetrofitClient.getClient();
        AuthApiService authApiService = retrofit.create(AuthApiService.class);
        authService = new AuthService(this, authApiService);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        firstNameLayout = findViewById(R.id.firstNameLayout);
        lastNameLayout = findViewById(R.id.lastNameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        phoneLayout = findViewById(R.id.phoneLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);

        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);

        registerButton = findViewById(R.id.registerButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        registerButton.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();

        Log.d("RegisterActivity", "Attempting registration with email: " + email);

        if (!validateInput(firstName, lastName, email, phone, password, confirmPassword)) {
            return;
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setFirstNameUser(firstName);
        userDTO.setLastNameUser(lastName);
        userDTO.setEmailUser(email);
        userDTO.setPhoneNumberUser(phone);
        userDTO.setPassword(password);
        userDTO.setUserType(UserType.ELDER); // Implicit setăm tipul la ELDER
        // Setăm o dată fixă pentru test
        userDTO.setDateOfBirthUser(LocalDate.of(1990, 1, 1)); // Acest camp va fi completat ulterior

        Log.d("RegisterActivity", "Created UserDTO: " + userDTO.getEmailUser());

        showLoading(true);
        authService.register(userDTO, new AuthService.AuthCallback() {
            @Override
            public void onSuccess(UserDTO user) {
                Log.d("RegisterActivity", "Registration successful");
                showLoading(false);
                Toast.makeText(RegisterActivity.this,
                        "Înregistrare reușită!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message) {
                Log.e("RegisterActivity", "Registration error: " + message);
                showLoading(false);
                Log.e("RegisterActivity", "Error: " + message);
                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateInput(String firstName, String lastName, String email,
                                  String phone, String password, String confirmPassword) {
        boolean isValid = true;

        if (firstName.isEmpty()) {
            firstNameLayout.setError("Prenumele este obligatoriu");
            isValid = false;
        } else {
            firstNameLayout.setError(null);
        }

        if (lastName.isEmpty()) {
            lastNameLayout.setError("Numele este obligatoriu");
            isValid = false;
        } else {
            lastNameLayout.setError(null);
        }

        if (email.isEmpty()) {
            emailLayout.setError("Email-ul este obligatoriu");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Email invalid");
            isValid = false;
        } else {
            emailLayout.setError(null);
        }

        if (phone.isEmpty()) {
            phoneLayout.setError("Numărul de telefon este obligatoriu");
            isValid = false;
        } else {
            phoneLayout.setError(null);
        }

        if (password.isEmpty()) {
            passwordLayout.setError("Parola este obligatorie");
            isValid = false;
        } else if (password.length() < 8) {
            passwordLayout.setError("Parola trebuie să aibă minim 8 caractere");
            isValid = false;
        } else {
            passwordLayout.setError(null);
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordLayout.setError("Parolele nu coincid");
            isValid = false;
        } else {
            confirmPasswordLayout.setError(null);
        }

        return isValid;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        registerButton.setEnabled(!show);
    }
}
