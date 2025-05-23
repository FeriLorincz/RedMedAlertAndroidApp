package com.feri.redmedalertandroidapp.api.config;




import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;


import com.feri.redmedalertandroidapp.api.RetrofitClient;
import com.feri.redmedalertandroidapp.api.dto.SensorDataDTO;
import com.feri.redmedalertandroidapp.api.model.HealthDataPayload;
import com.feri.redmedalertandroidapp.api.service.ApiCallback;
import com.feri.redmedalertandroidapp.api.service.ApiService;
import com.feri.redmedalertandroidapp.api.service.HealthDataApiService;
import com.feri.redmedalertandroidapp.api.validator.HealthDataValidator;
import com.feri.redmedalertandroidapp.network.SensorDataApi;
import com.samsung.android.sdk.healthdata.BuildConfig;
import com.feri.redmedalertandroidapp.notification.NotificationService;
import android.annotation.SuppressLint;
import android.os.Build;
import android.provider.Settings;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ApiClient {


    private static final String TAG = "ApiClient";
    private static final String BASE_URL = "http://192.168.0.91:8080/"; // Înlocuiește cu URL-ul real
    private static Retrofit retrofit = null;
    private static ApiClient instance = null;
    private final HealthDataApiService apiService;
    private final Context context;
    private final NotificationService notificationService;


    // Constructor privat pentru Singleton
    private ApiClient(Context context) {
        this.context = context.getApplicationContext();
        retrofit = getClient();
        apiService = retrofit.create(HealthDataApiService.class);
        notificationService = new NotificationService(context);
    }


    public static String getBaseUrl() {
        return BASE_URL;
    }
    // Metodă pentru obținerea instanței singleton
    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context);
        }
        return instance;
    }


    // Configurare client Retrofit
    public static Retrofit getClient() {
        if (retrofit == null) {
            // Configurăm logging pentru debugging
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message ->
                    Log.d(TAG, "okHttp: " + message));
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);


            // Configurăm OkHttpClient cu timeout și logging
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(chain -> {
                        Request request = chain.request();
                        Log.d(TAG, "Sending request to: " + request.url());
                        Response response = chain.proceed(request);
                        Log.d(TAG, "Received response code: " + response.code());
                        return response;
                    })
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build();


            // Construim Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }


    // Metodă simplificată pentru cazurile fără callback
    public void uploadHealthData(Map<String, Double> data) {
        uploadHealthData(data, null);
    }


    // Metodă completă pentru upload date cu callback
    public void uploadHealthData(Map<String, Double> data, ApiCallback callback) {
        String deviceId = getDeviceInfo().getDeviceId();
        String userId = getUserInfo().getUserId();

        List<SensorDataDTO> sensorDataList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            SensorDataDTO sensorData = new SensorDataDTO(
                    deviceId,
                    userId,
                    entry.getKey(),
                    entry.getValue(),
                    now
            );
            sensorDataList.add(sensorData);
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.uploadBatchSensorData(sensorDataList).enqueue(new Callback<List<SensorDataDTO>>() {
            @Override
            public void onResponse(Call<List<SensorDataDTO>> call, retrofit2.Response<List<SensorDataDTO>> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Data uploaded successfully");
                    notificationService.showDataUploadNotification(true);
                    if (callback != null) callback.onSuccess();
                } else {
                    String error = "Upload failed: " + response.code();
                    Log.e(TAG, error);
                    notificationService.showDataUploadNotification(false);
                    if (callback != null) callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<List<SensorDataDTO>> call, Throwable t) {
                String error = "Upload failed: " + t.getMessage();
                Log.e(TAG, error, t);
                notificationService.showDataUploadNotification(false);
                if (callback != null) callback.onError(error);
            }
        });
    }


    // Metode helper pentru obținerea informațiilor reale
    private DeviceInfo getDeviceInfo() {
        String deviceId;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Pentru Android 10 și mai nou
            deviceId = Settings.Secure.getString(
                    context.getContentResolver(),
                    Settings.Secure.ANDROID_ID
            );
        } else {
            // Pentru versiuni mai vechi
            @SuppressLint("HardwareIds")
            String id = Settings.Secure.getString(
                    context.getContentResolver(),
                    Settings.Secure.ANDROID_ID
            );
            deviceId = id;
        }
        return new DeviceInfo(deviceId);
    }


    private UserInfo getUserInfo() {
        // Implementare pentru obținerea informațiilor despre utilizator
        // Aici ar trebui să vină din SharedPreferences sau alt storage
        SharedPreferences prefs = context.getSharedPreferences("RedMedAlert", Context.MODE_PRIVATE);
        return new UserInfo(prefs.getString("user_id", "unknown"));
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


    // Metodă pentru resetarea instanței (utilă pentru teste)
    public static void resetInstance() {
        instance = null;
        retrofit = null;
    }


    public static synchronized SensorDataApi createSensorDataApi() {
        return getClient().create(SensorDataApi.class);
    }


    private final OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(chain -> {
                Request original = chain.request();
                Request request = original.newBuilder()
                        .header("Authorization", getBasicAuthHeader())
                        .build();
                Log.d(TAG, "Sending request to: " + request.url());
                Response response = chain.proceed(request);
                Log.d(TAG, "Received response code: " + response.code());
                return response;
            })
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();


    private String getBasicAuthHeader() {
        String credentials = "test-user:test-password";
        return "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
    }
}