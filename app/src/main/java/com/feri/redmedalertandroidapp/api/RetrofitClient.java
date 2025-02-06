package com.feri.redmedalertandroidapp.api;

import android.content.Context;
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
    //private static final String BASE_URL = "http://10.0.2.2:8080/";  // pentru emulator Android
    private static final String BASE_URL = "http://192.168.0.91:8080/"; // Înlocuiește cu URL-ul real pentru dispozitiv fizic

    private static Retrofit retrofit = null;
    private static RetrofitClient instance = null;
    private static Context context;

    private RetrofitClient(Context context) {
        RetrofitClient.context = context.getApplicationContext();
    }

    public static synchronized RetrofitClient getInstance(Context context) {
        if (instance == null) {
            instance = new RetrofitClient(context);
        }
        return instance;
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Configurare pentru LocalDate
            Gson gson = new GsonBuilder()
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

            // Adăugăm interceptor pentru token
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(chain -> {
                        Request original = chain.request();
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
                    })
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS);

            // Construim Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(clientBuilder.build())
                    .build();
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
        instance = null;
        retrofit = null;
    }
}
