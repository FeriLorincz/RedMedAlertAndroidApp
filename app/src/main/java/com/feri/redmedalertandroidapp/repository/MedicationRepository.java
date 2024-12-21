package com.feri.redmedalertandroidapp.repository;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.feri.redmedalertandroidapp.api.RetrofitClient;
import com.feri.redmedalertandroidapp.api.model.Medication;
import com.feri.redmedalertandroidapp.api.service.MedicationApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class MedicationRepository {

    private static final String TAG = "MedicationRepository";
    private final MedicationApiService medicationApiService;
    private final MutableLiveData<List<Medication>> medications = new MutableLiveData<>();

    public MedicationRepository(Context context) {
        medicationApiService = RetrofitClient.getMedicationService();
    }

    public LiveData<List<Medication>> getMedications() {
        return medications;
    }

    public void fetchMedicationsByProfileId(String profileId) {
        medicationApiService.getMedicationsByProfileId(profileId).enqueue(new Callback<List<Medication>>() {
            @Override
            public void onResponse(Call<List<Medication>> call, Response<List<Medication>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    medications.postValue(response.body());
                } else {
                    Log.e(TAG, "Error fetching medications: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Medication>> call, Throwable t) {
                Log.e(TAG, "Failed to fetch medications", t);
            }
        });
    }

    public void addMedication(String profileId, Medication medication, RepositoryCallback<Medication> callback) {
        medicationApiService.addMedication(profileId, medication).enqueue(new Callback<Medication>() {
            @Override
            public void onResponse(Call<Medication> call, Response<Medication> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Medication> currentMeds = medications.getValue();
                    if (currentMeds != null) {
                        currentMeds.add(response.body());
                        medications.postValue(currentMeds);
                    }
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error adding medication: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Medication> call, Throwable t) {
                callback.onError("Failed to add medication: " + t.getMessage());
            }
        });
    }

    public void updateMedication(String medicationId, Medication medication, RepositoryCallback<Medication> callback) {
        medicationApiService.updateMedication(medicationId, medication).enqueue(new Callback<Medication>() {
            @Override
            public void onResponse(Call<Medication> call, Response<Medication> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Medication> currentMeds = medications.getValue();
                    if (currentMeds != null) {
                        int index = -1;
                        for (int i = 0; i < currentMeds.size(); i++) {
                            if (currentMeds.get(i).getIdMedication().equals(medicationId)) {
                                index = i;
                                break;
                            }
                        }
                        if (index != -1) {
                            currentMeds.set(index, response.body());
                            medications.postValue(currentMeds);
                        }
                    }
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error updating medication: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Medication> call, Throwable t) {
                callback.onError("Failed to update medication: " + t.getMessage());
            }
        });
    }

    public void deleteMedication(String medicationId, RepositoryCallback<Void> callback) {
        medicationApiService.deleteMedication(medicationId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    List<Medication> currentMeds = medications.getValue();
                    if (currentMeds != null) {
                        currentMeds.removeIf(med ->
                                med.getIdMedication().equals(medicationId));
                        medications.postValue(currentMeds);
                    }
                    callback.onSuccess(null);
                } else {
                    callback.onError("Error deleting medication: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Failed to delete medication: " + t.getMessage());
            }
        });
    }
}
