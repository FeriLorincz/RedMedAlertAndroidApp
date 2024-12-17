package com.feri.redmedalertandroidapp.api;

import android.util.Log;

import com.feri.redmedalertandroidapp.api.model.HealthDataPayload;

import java.util.HashMap;
import java.util.Map;

public class ApiTestManager {

    private static final String TAG = "ApiTestManager";
    private final HealthDataApiManager apiManager;
    private TestCallback testCallback;
    private final String deviceId;
    private final String userId;

    public interface TestCallback {
        void onTestSuccess();
        void onTestFailure(String error);
    }

    public ApiTestManager(String authToken, String deviceId, String userId) {
        this.apiManager = new HealthDataApiManager(authToken, deviceId, userId);
        this.deviceId = deviceId;
        this.userId = userId;
    }

    public void setTestCallback(TestCallback callback) {
        this.testCallback = callback;
    }

    public void testApiConnection() {
        // Creăm date de test similare cu cele reale
        Map<String, Double> testData = new HashMap<>();
        testData.put("heart_rate", 75.0);
        testData.put("blood_oxygen", 98.0);

        HealthDataPayload payload = new HealthDataPayload(deviceId, userId, testData);

        Log.d(TAG, "Sending test data: " + testData);

        // Trimitem datele de test
        apiManager.sendHealthData(testData, new HealthDataApiManager.ApiCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Test API connection successful!");
                if (testCallback != null) {
                    testCallback.onTestSuccess();
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Test API connection failed: " + message);
                if (testCallback != null) {
                    testCallback.onTestFailure(message);
                }
            }
        });
    }
}
