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
    private final MutableLiveData<MedicalProfile> currentProfile = new MutableLiveData<>();

    public MedicalProfileRepository(Context context) {
        medicalProfileApiService = RetrofitClient.getMedicalProfileService();
    }

    public LiveData<MedicalProfile> getCurrentProfile() {
        return currentProfile;
    }

    public void fetchProfileByUserId(String userId) {
        medicalProfileApiService.getMedicalProfileByUserId(userId).enqueue(new Callback<MedicalProfile>() {
            @Override
            public void onResponse(Call<MedicalProfile> call, Response<MedicalProfile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentProfile.postValue(response.body());
                } else {
                    Log.e(TAG, "Error fetching medical profile: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MedicalProfile> call, Throwable t) {
                Log.e(TAG, "Failed to fetch medical profile", t);
            }
        });
    }

    public void createProfile(MedicalProfile profile, RepositoryCallback<MedicalProfile> callback) {
        medicalProfileApiService.createMedicalProfile(profile).enqueue(new Callback<MedicalProfile>() {
            @Override
            public void onResponse(Call<MedicalProfile> call, Response<MedicalProfile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentProfile.postValue(response.body());
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

    public void updateProfile(String profileId, MedicalProfile profile, RepositoryCallback<MedicalProfile> callback) {
        medicalProfileApiService.updateMedicalProfile(profileId, profile).enqueue(new Callback<MedicalProfile>() {
            @Override
            public void onResponse(Call<MedicalProfile> call, Response<MedicalProfile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentProfile.postValue(response.body());
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

    public void deleteProfile(String profileId, RepositoryCallback<Void> callback) {
        medicalProfileApiService.deleteMedicalProfile(profileId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    currentProfile.postValue(null);
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
