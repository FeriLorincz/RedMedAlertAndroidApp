package com.feri.redmedalertandroidapp.api.config;

import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class ApiClient {

    private static final String TAG = "ApiClient";
    private static final String BASE_URL = "http://192.168.0.91:8080/"; // Înlocuiește cu URL-ul real
    private static Retrofit retrofit = null;

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
}
