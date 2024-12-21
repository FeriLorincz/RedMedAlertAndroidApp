package com.feri.redmedalertandroidapp.api.service;

import com.feri.redmedalertandroidapp.api.model.MedicalProfile;
import com.feri.redmedalertandroidapp.api.model.Medication;
import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;

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

    // Endpoints noi pentru gestionarea bolilor și condițiilor
    @POST("api/medical-profiles/{id}/diseases")
    Call<MedicalProfile> addDisease(@Path("id") String id, @Body String disease);

    @DELETE("api/medical-profiles/{id}/diseases/{disease}")
    Call<MedicalProfile> removeDisease(@Path("id") String id, @Path("disease") String disease);

    @POST("api/medical-profiles/{id}/conditions")
    Call<MedicalProfile> addCondition(@Path("id") String id, @Body String condition);

    @DELETE("api/medical-profiles/{id}/conditions/{condition}")
    Call<MedicalProfile> removeCondition(@Path("id") String id, @Path("condition") String condition);
}