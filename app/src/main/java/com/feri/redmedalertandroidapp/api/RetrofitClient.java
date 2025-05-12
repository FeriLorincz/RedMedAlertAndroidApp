package com.feri.redmedalertandroidapp.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.feri.redmedalertandroidapp.api.service.EmergencyContactApiService;
import com.feri.redmedalertandroidapp.api.service.MedicalProfileApiService;
import com.feri.redmedalertandroidapp.api.service.MedicationApiService;
import com.feri.redmedalertandroidapp.api.service.UserApiService;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import com.feri.redmedalertandroidapp.auth.service.TokenManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RetrofitClient {

    private static final String TAG = "RetrofitClient";

    // Definirea diferitelor URL-uri posibile
    private static final String[] SERVER_URLS = {
            "http://192.168.0.91:8080/",    // IP-ul calculatorului tău
            "http://10.0.2.2:8080/",        // Emulator Android
            "http://192.168.1.100:8080/",   // Posibil IP alternativ
            "http://127.0.0.1:8080/"        // localhost
    };

    private static int currentUrlIndex = 0;
    private static String BASE_URL = SERVER_URLS[0];

    private static Retrofit retrofit = null;
    private static RetrofitClient instance = null;
    private static Context context;

    // Pentru controlul reîncercărilor
    private static int retryCount = 0;
    private static final int MAX_RETRIES = 3;
    private static boolean isRetrying = false;

    private RetrofitClient(Context context) {
        if (context != null) {
            RetrofitClient.context = context.getApplicationContext();
            Log.d(TAG, "RetrofitClient initialized with context: " + context.getApplicationContext());

            // Încărcăm URL-ul salvat dacă există
            loadSavedServerUrl();
        } else {
            Log.e(TAG, "Încercare de inițializare RetrofitClient cu context null!");
        }
    }

    // Metoda pentru a încerca următorul URL în listă
    public static String tryNextServerUrl() {
        currentUrlIndex = (currentUrlIndex + 1) % SERVER_URLS.length;
        BASE_URL = SERVER_URLS[currentUrlIndex];
        Log.d(TAG, "Încercare următorul server URL: " + BASE_URL);

        // Salvăm URL-ul curent
        if (context != null) {
            SharedPreferences prefs = context.getSharedPreferences("NetworkPrefs", Context.MODE_PRIVATE);
            prefs.edit().putString("current_server_url", BASE_URL).apply();
        }

        // Resetăm Retrofit pentru a forța recrearea cu noul URL
        retrofit = null;

        return BASE_URL;
    }

    // Încărcăm URL-ul salvat
    private void loadSavedServerUrl() {
        if (context != null) {
            SharedPreferences prefs = context.getSharedPreferences("NetworkPrefs", Context.MODE_PRIVATE);
            String savedUrl = prefs.getString("current_server_url", null);

            if (savedUrl != null && !savedUrl.isEmpty()) {
                BASE_URL = savedUrl;

                // Găsim indexul URL-ului salvat
                for (int i = 0; i < SERVER_URLS.length; i++) {
                    if (SERVER_URLS[i].equals(BASE_URL)) {
                        currentUrlIndex = i;
                        break;
                    }
                }

                Log.d(TAG, "URL salvat încărcat: " + BASE_URL);
            }
        }
    }


    // Metodă pentru a seta manual URL-ul serverului
    public static void setServerUrl(String url) {
        if (url != null && !url.isEmpty()) {
            BASE_URL = url;
            Log.d(TAG, "Server URL schimbat manual la: " + BASE_URL);

            // Resetăm Retrofit pentru a folosi noul URL
            retrofit = null;

            // Salvăm URL-ul pentru utilizare viitoare
            if (context != null) {
                SharedPreferences prefs = context.getSharedPreferences("NetworkPrefs", Context.MODE_PRIVATE);
                prefs.edit().putString("current_server_url", url).apply();
            }

            // Resetăm contorul de reîncercări
            retryCount = 0;
        }
    }

    // Obținem URL-ul curent
    public static String getCurrentUrl() {
        return BASE_URL;
    }

    // Verificăm dacă mai putem face reîncercări
    public static boolean canRetry() {
        return retryCount < MAX_RETRIES;
    }

    // Incrementăm contorul de reîncercări
    public static void incrementRetryCount() {
        retryCount++;
    }

    // Resetăm contorul de reîncercări
    public static void resetRetryCount() {
        retryCount = 0;
    }

    // Setăm starea de reîncercare
    public static void setRetrying(boolean retrying) {
        isRetrying = retrying;
    }

    // Verificăm dacă suntem în proces de reîncercare
    public static boolean isRetrying() {
        return isRetrying;
    }

    public static synchronized RetrofitClient getInstance(Context context) {
        if (instance == null) {
            if (context == null) {
                Log.e(TAG, "getInstance() apelat cu context null!");
                throw new IllegalArgumentException("Context cannot be null in RetrofitClient.getInstance()");
            }
            instance = new RetrofitClient(context);
        } else if (RetrofitClient.context == null && context != null) {
            // Actualizează contextul dacă a fost null înainte
            RetrofitClient.context = context.getApplicationContext();
            Log.d(TAG, "Context actualizat în getInstance()");
        }
        return instance;
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            Log.d(TAG, "Inițializare Retrofit cu URL: " + BASE_URL);

            // Configurare pentru LocalDateTime
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .registerTypeAdapter(LocalDateTime.class,
                            (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                                    new JsonPrimitive(src.toString()))
                    .registerTypeAdapter(LocalDateTime.class,
                            (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) ->
                                    LocalDateTime.parse(json.getAsString()))
                    .registerTypeAdapter(LocalDate.class,
                            (JsonSerializer<LocalDate>) (src, typeOfSrc, context) ->
                                    new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE)))
                    .registerTypeAdapter(LocalDate.class,
                            (JsonDeserializer<LocalDate>) (json, typeOfT, context) ->
                                    LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE))
                    .create();

            // Configurăm logging pentru debugging
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message ->
                    Log.d(TAG, "okHttp: " + message));
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Construim client OkHttp
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS);

            // Adăugăm interceptor pentru token
            if (context != null) {
                clientBuilder.addInterceptor(chain -> {
                    Request original = chain.request();

                    try {
                        TokenManager tokenManager = new TokenManager(context);
                        String token = tokenManager.getToken();

                        Request.Builder requestBuilder = original.newBuilder()
                                .header("Accept", "application/json")
                                .header("Content-Type", "application/json");

                        if (token != null && !token.isEmpty()) {
                            requestBuilder.header("Authorization", "Bearer " + token);
                        }

                        Request request = requestBuilder.build();
                        return chain.proceed(request);
                    } catch (Exception e) {
                        Log.e(TAG, "Eroare în interceptor: " + e.getMessage(), e);
                        return chain.proceed(original);
                    }
                });
            }

            // Construim Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())  // Adaugă acesta
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(clientBuilder.build())
                    .build();

            Log.d(TAG, "Retrofit client creat cu URL: " + BASE_URL);
        }
        return retrofit;
    }

    // Metode helper pentru a obține serviciile
    public static UserApiService getUserService() {
        return getClient().create(UserApiService.class);
    }

    public static MedicalProfileApiService getMedicalProfileService() {
        return getClient().create(MedicalProfileApiService.class);
    }

    public static EmergencyContactApiService getEmergencyContactService() {
        return getClient().create(EmergencyContactApiService.class);
    }

    public static MedicationApiService getMedicationService() {
        return getClient().create(MedicationApiService.class);
    }

    // Metodă pentru resetarea instanței (utilă pentru teste)
    public static void resetInstance() {
        Log.d(TAG, "Resetare RetrofitClient");
        instance = null;
        retrofit = null;
    }
}