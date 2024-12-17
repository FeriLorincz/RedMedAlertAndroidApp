package com.feri.redmedalertandroidapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.feri.redmedalertandroidapp.api.ApiTestManager;

public class TestActivity extends AppCompatActivity {

    private static final String TAG = "TestActivity";
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        statusText = findViewById(R.id.statusText);
        Button testButton = findViewById(R.id.testButton);

        testButton.setOnClickListener(v -> {
            Log.d(TAG, "Starting API test...");
            statusText.setText("Testing connection...");

            // Folosim valori de test - în producție acestea vor veni din autentificare
            String testToken = "test-token";
            String testDeviceId = "test-device";
            String testUserId = "test-user";

            ApiTestManager testManager = new ApiTestManager(testToken, testDeviceId, testUserId);
            testManager.testApiConnection();


            // Adăugăm un handler pentru a actualiza UI-ul cu rezultatul
            testManager.setTestCallback(new ApiTestManager.TestCallback() {
                @Override
                public void onTestSuccess() {
                    runOnUiThread(() -> {
                        statusText.setText("Connection successful!");
                        Log.d(TAG, "API test successful");
                    });
                }

                @Override
                public void onTestFailure(String error) {
                    runOnUiThread(() -> {
                        statusText.setText("Connection failed: " + error);
                        Log.e(TAG, "API test failed: " + error);
                    });
                }
            });
        });
    }
}