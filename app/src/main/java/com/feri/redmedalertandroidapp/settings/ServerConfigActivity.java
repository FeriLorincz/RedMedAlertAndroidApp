package com.feri.redmedalertandroidapp.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.feri.redmedalertandroidapp.R;
import com.feri.redmedalertandroidapp.api.RetrofitClient;
import com.feri.redmedalertandroidapp.api.service.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServerConfigActivity extends AppCompatActivity {

    private static final String TAG = "ServerConfigActivity";
    private EditText etServerUrl;
    private Button btnTestConnection;
    private Button btnSaveConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_config);

        etServerUrl = findViewById(R.id.etServerUrl);
        btnTestConnection = findViewById(R.id.btnTestConnection);
        btnSaveConfig = findViewById(R.id.btnSaveConfig);

        // Pune URL-ul curent
        SharedPreferences prefs = getSharedPreferences("NetworkPrefs", MODE_PRIVATE);
        String currentUrl = prefs.getString("working_server_url", "http://192.168.0.91:8080/");
        etServerUrl.setText(currentUrl);

        btnTestConnection.setOnClickListener(v -> testConnection());
        btnSaveConfig.setOnClickListener(v -> saveConfig());
    }

    private void testConnection() {
        final String serverUrl = etServerUrl.getText().toString().trim();
        if (serverUrl.isEmpty()) {
            Toast.makeText(this, "Introduceți adresa serverului", Toast.LENGTH_SHORT).show();
            return;
        }

        // Adaugă "/" la sfârșit dacă nu există
        final String finalServerUrl = serverUrl.endsWith("/") ? serverUrl : serverUrl + "/";

        // Setează temporar URL-ul pentru test
        RetrofitClient.setServerUrl(finalServerUrl);
        RetrofitClient.resetInstance();
        RetrofitClient.getInstance(this);

        // Testează conexiunea
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.testConnection().enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ServerConfigActivity.this,
                            "Conexiune reușită la " + finalServerUrl, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Server test reușit pentru " + finalServerUrl);
                } else {
                    Toast.makeText(ServerConfigActivity.this,
                            "Eroare de conexiune: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Server test eșuat: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(ServerConfigActivity.this,
                        "Eroare de conexiune: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Server test eșuat: " + t.getMessage());
            }
        });
    }

    private void saveConfig() {
        String serverUrl = etServerUrl.getText().toString().trim();
        if (serverUrl.isEmpty()) {
            Toast.makeText(this, "Introduceți adresa serverului", Toast.LENGTH_SHORT).show();
            return;
        }

        // Adaugă "/" la sfârșit dacă nu există
        if (!serverUrl.endsWith("/")) {
            serverUrl += "/";
        }

        // Salvează configurația
        RetrofitClient.setServerUrl(serverUrl);

        // Resetează instanța pentru a folosi noua configurație
        RetrofitClient.resetInstance();
        RetrofitClient.getInstance(this);

        Toast.makeText(this, "Configurație salvată: " + serverUrl, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Server URL salvat: " + serverUrl);

        finish();
    }
}