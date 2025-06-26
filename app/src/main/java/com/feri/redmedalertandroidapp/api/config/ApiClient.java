package com.feri.redmedalertandroidapp.api.config;

import android.content.Context;
import android.util.Log;
import com.feri.redmedalertandroidapp.api.model.HealthDataPayload;
import com.feri.redmedalertandroidapp.api.service.HealthDataApiService;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static final String TAG = "ApiClient";
    private static final String BASE_URL =  "http://192.168.0.91:8080/"; // "http://192.168.0.91:8080/"   "http://192.168.1.147:3000/"

    private static ApiClient instance;
    private final Context context;
    private final Retrofit retrofit;
    private final HealthDataApiService apiService;
    private final DeviceInfo deviceInfo;
    private final UserInfo userInfo;

    // Interfața callback pentru API
    public interface ApiCallback {
        void onSuccess();
        void onError(String error);
    }

    // Clase helper interne
    private static class DeviceInfo {
        private final String deviceId;
        DeviceInfo(String deviceId) { this.deviceId = deviceId; }
        String getDeviceId() { return deviceId; }
    }

    private static class UserInfo {
        private final String userId;
        UserInfo(String userId) { this.userId = userId; }
        String getUserId() { return userId; }
    }

    private ApiClient(Context context) {
        this.context = context;

        // Configurează logging pentru debug
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(HealthDataApiService.class);

        // Inițializează device și user info
        String androidId = android.provider.Settings.Secure.getString(
                context.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID
        );

        deviceInfo = new DeviceInfo(androidId);
        userInfo = new UserInfo("user-" + androidId); // User ID bazat pe device ID

        Log.d(TAG, "ApiClient inițializat cu URL: " + BASE_URL);
        Log.d(TAG, "Device ID: " + deviceInfo.getDeviceId());
        Log.d(TAG, "User ID: " + userInfo.getUserId());
    }

    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context.getApplicationContext());
        }
        return instance;
    }

    public void uploadHealthData(Map<String, Double> healthData, ApiCallback callback) {
        // Extrage timestamp-ul din datele primite (dacă există)
        long currentTimestamp = System.currentTimeMillis();
        if (healthData.containsKey("timestamp")) {
            currentTimestamp = healthData.get("timestamp").longValue();
            Log.d(TAG, "Folosim timestamp din date: " + currentTimestamp);
        } else {
            Log.d(TAG, "Folosim timestamp curent: " + currentTimestamp);
        }

        // Creează Map-ul pentru senzori (fără metadata)
        Map<String, Double> sensorData = new HashMap<>();
        for (Map.Entry<String, Double> entry : healthData.entrySet()) {
            // Includem doar datele de senzori, nu metadata tehnică
            if (!entry.getKey().equals("data_source") && !entry.getKey().equals("timestamp")) {
                sensorData.put(entry.getKey(), entry.getValue());
            }
        }

        Log.d(TAG, "Pregătim să trimitem datele către server:");
        Log.d(TAG, "Device ID: " + deviceInfo.getDeviceId());
        Log.d(TAG, "User ID: " + userInfo.getUserId());
        Log.d(TAG, "Timestamp: " + convertToISO8601(currentTimestamp));
        Log.d(TAG, "Numărul de senzori: " + sensorData.size());

        for (Map.Entry<String, Double> entry : sensorData.entrySet()) {
            Log.d(TAG, "  " + entry.getKey() + " = " + entry.getValue());
        }

        // FOLOSESC NOUL ENDPOINT - FĂRĂ AUTORIZARE!
        Call<String> call = apiService.sendHealthData(deviceInfo.getDeviceId(), sensorData);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Date trimise cu succes către server (HTTP " + response.code() + ")");
                    Log.d(TAG, "✅ Răspuns server: " + response.body());
                    Log.d(TAG, "✅ Datele aparțin userului: " + userInfo.getUserId());
                    callback.onSuccess();
                } else {
                    String error = "HTTP " + response.code() + ": " + response.message();
                    Log.e(TAG, "❌ Eroare server: " + error);
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Detalii eroare: " + errorBody);
                            error += " - " + errorBody;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Nu s-a putut citi error body", e);
                    }
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                String error = "Conexiune eșuată: " + t.getMessage();
                Log.e(TAG, "❌ Eroare conexiune: " + error, t);
                callback.onError(error);
            }
        });
    }

    private String convertToISO8601(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date(timestamp));
    }
}