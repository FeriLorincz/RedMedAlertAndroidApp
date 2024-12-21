package com.feri.redmedalertandroidapp.api;

import com.feri.redmedalertandroidapp.api.service.EmergencyContactApiService;
import com.feri.redmedalertandroidapp.api.service.MedicalProfileApiService;
import com.feri.redmedalertandroidapp.api.service.MedicationApiService;
import com.feri.redmedalertandroidapp.api.service.UserApiService;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {

    private static final String BASE_URL = "http://192.168.0.91:8080/"; // Înlocuiește cu URL-ul real
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Configurăm logging pentru debugging
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Configurăm OkHttpClient cu timeout și logging
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
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
}
