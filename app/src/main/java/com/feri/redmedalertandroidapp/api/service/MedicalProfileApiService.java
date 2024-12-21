package com.feri.redmedalertandroidapp.api.service;

import com.feri.redmedalertandroidapp.api.model.MedicalProfile;
import retrofit2.Call;
import retrofit2.http.*;

public interface MedicalProfileApiService {

    @GET("api/medical-profiles/{id}")
    Call<MedicalProfile> getMedicalProfileById(@Path("id") String id);

    @GET("api/medical-profiles/user/{userId}")
    Call<MedicalProfile> getMedicalProfileByUserId(@Path("userId") String userId);

    @POST("api/medical-profiles")
    Call<MedicalProfile> createMedicalProfile(@Body MedicalProfile profile);

    @PUT("api/medical-profiles/{id}")
    Call<MedicalProfile> updateMedicalProfile(@Path("id") String id, @Body MedicalProfile profile);

    @DELETE("api/medical-profiles/{id}")
    Call<Void> deleteMedicalProfile(@Path("id") String id);

}
