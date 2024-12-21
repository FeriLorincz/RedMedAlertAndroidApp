package com.feri.redmedalertandroidapp.api.service;

import com.feri.redmedalertandroidapp.api.model.Medication;

import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;

public interface MedicationApiService {

    @GET("api/medications/{id}")
    Call<Medication> getMedicationById(@Path("id") String id);

    @GET("api/medications/profile/{profileId}")
    Call<List<Medication>> getMedicationsByProfileId(@Path("profileId") String profileId);

    @POST("api/medications/profile/{profileId}")
    Call<Medication> addMedication(@Path("profileId") String profileId, @Body Medication medication);

    @PUT("api/medications/{id}")
    Call<Medication> updateMedication(@Path("id") String id, @Body Medication medication);

    @DELETE("api/medications/{id}")
    Call<Void> deleteMedication(@Path("id") String id);
}
