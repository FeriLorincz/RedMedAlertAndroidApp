package com.feri.redmedalertandroidapp.network;

import com.feri.redmedalertandroidapp.api.config.ApiClient;
import retrofit2.Retrofit;

public class NetworkModule {
    private static final NetworkModule instance = new NetworkModule();
    private final SensorDataApi sensorDataApi;

    private NetworkModule() {
        Retrofit retrofit = ApiClient.getClient();
        sensorDataApi = retrofit.create(SensorDataApi.class);
    }

    public static NetworkModule getInstance() {
        return instance;
    }

    public SensorDataApi getSensorDataApi() {
        return sensorDataApi;
    }
}
