package com.feri.redmedalertandroidapp.api.service;

import com.feri.redmedalertandroidapp.api.model.EmergencyContact;

import java.util.List;
import retrofit2.http.*;
import retrofit2.Call;

public interface EmergencyContactApiService {

    @GET("api/emergency-contacts/{id}")
    Call<EmergencyContact> getContactById(@Path("id") String id);

    @GET("api/emergency-contacts/user/{userId}")
    Call<List<EmergencyContact>> getContactsByUserId(@Path("userId") String userId);

    @GET("api/emergency-contacts/search")
    Call<List<EmergencyContact>> getContactsByLastName(@Query("lastName") String lastName);

    @POST("api/emergency-contacts")
    Call<EmergencyContact> createContact(@Body EmergencyContact contact);

    @PUT("api/emergency-contacts/{id}")
    Call<EmergencyContact> updateContact(@Path("id") String id, @Body EmergencyContact contact);

    @DELETE("api/emergency-contacts/{id}")
    Call<Void> deleteContact(@Path("id") String id);
}
