package com.feri.redmedalertandroidapp.repository;
import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.feri.redmedalertandroidapp.api.RetrofitClient;
import com.feri.redmedalertandroidapp.api.model.MedicalProfile;
import com.feri.redmedalertandroidapp.api.service.MedicalProfileApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MedicalProfileRepository {

    private static final String TAG = "MedicalProfileRepo";
    private final MedicalProfileApiService medicalProfileApiService;

    public MedicalProfileRepository(Context context) {
        medicalProfileApiService = RetrofitClient.getMedicalProfileService();
    }

    public void fetchProfileByUserId(String userId, RepositoryCallback<MedicalProfile> callback) {
        medicalProfileApiService.getMedicalProfileByUserId(userId)
                .enqueue(new Callback<MedicalProfile>() {
                    @Override
                    public void onResponse(Call<MedicalProfile> call, Response<MedicalProfile> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError("Error fetching medical profile: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<MedicalProfile> call, Throwable t) {
                        Log.e(TAG, "Failed to fetch medical profile", t);
                        callback.onError("Failed to fetch medical profile: " + t.getMessage());
                    }
                });
    }

    public void createMedicalProfile(MedicalProfile profile, RepositoryCallback<MedicalProfile> callback) {
        medicalProfileApiService.createMedicalProfile(profile)
                .enqueue(new Callback<MedicalProfile>() {
                    @Override
                    public void onResponse(Call<MedicalProfile> call, Response<MedicalProfile> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError("Error creating medical profile: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<MedicalProfile> call, Throwable t) {
                        callback.onError("Failed to create medical profile: " + t.getMessage());
                    }
                });
    }

    public void updateMedicalProfile(String profileId, MedicalProfile profile,
                                     RepositoryCallback<MedicalProfile> callback) {
        medicalProfileApiService.updateMedicalProfile(profileId, profile)
                .enqueue(new Callback<MedicalProfile>() {
                    @Override
                    public void onResponse(Call<MedicalProfile> call, Response<MedicalProfile> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError("Error updating medical profile: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<MedicalProfile> call, Throwable t) {
                        callback.onError("Failed to update medical profile: " + t.getMessage());
                    }
                });
    }

    public void deleteMedicalProfile(String profileId, RepositoryCallback<Void> callback) {
        medicalProfileApiService.deleteMedicalProfile(profileId)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            callback.onSuccess(null);
                        } else {
                            callback.onError("Error deleting medical profile: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        callback.onError("Failed to delete medical profile: " + t.getMessage());
                    }
                });
    }
}