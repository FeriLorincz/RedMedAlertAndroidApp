package com.feri.redmedalertandroidapp.network;


import com.feri.redmedalertandroidapp.api.config.ApiClient;


import java.util.concurrent.TimeUnit;


import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class NetworkModule {
    private static final NetworkModule instance = new NetworkModule();
    private final SensorDataApi sensorDataApi;


    private NetworkModule() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiClient.getBaseUrl())
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        sensorDataApi = retrofit.create(SensorDataApi.class);
    }


    public static NetworkModule getInstance() {
        return instance;
    }


    public SensorDataApi getSensorDataApi() {
        return sensorDataApi;
    }
}