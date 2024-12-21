package com.feri.redmedalertandroidapp.api.service;

import com.feri.redmedalertandroidapp.api.model.User;
import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;

public interface UserApiService {

    @GET("api/users/{id}")
    Call<User> getUserById(@Path("id") String id);

    @GET("api/users")
    Call<List<User>> getAllUsers();

    @POST("api/users")
    Call<User> createUser(@Body User user);

    @PUT("api/users/{id}")
    Call<User> updateUser(@Path("id") String id, @Body User user);

    @DELETE("api/users/{id}")
    Call<Void> deleteUser(@Path("id") String id);

    @GET("api/users/type/{userType}")
    Call<List<User>> getUsersByType(@Path("userType") String userType);

    @GET("api/users/phone/{phoneNumber}")
    Call<User> getUserByPhone(@Path("phoneNumber") String phoneNumber);
}
