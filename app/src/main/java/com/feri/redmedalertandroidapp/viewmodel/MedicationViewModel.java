package com.feri.redmedalertandroidapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.feri.redmedalertandroidapp.api.model.Medication;
import com.feri.redmedalertandroidapp.repository.MedicationRepository;
import com.feri.redmedalertandroidapp.repository.RepositoryCallback;

import java.util.List;

public class MedicationViewModel extends AndroidViewModel {

    private final MedicationRepository repository;
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public interface UpdateCallback {
        void onSuccess();
        void onError(String message);
    }

    public MedicationViewModel(@NonNull Application application) {
        super(application);
        repository = new MedicationRepository(application);
    }

    public LiveData<List<Medication>> getMedications() {
        return repository.getMedications();
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadMedicationsForProfile(String profileId) {
        repository.fetchMedicationsByProfileId(profileId);
    }

    public void addMedication(String profileId, Medication medication, UpdateCallback callback) {
        repository.addMedication(profileId, medication, new RepositoryCallback<Medication>() {
            @Override
            public void onSuccess(Medication result) {
                callback.onSuccess();
            }

            @Override
            public void onError(String message) {
                error.setValue(message);
                callback.onError(message);
            }
        });
    }

    public void updateMedication(String medicationId, Medication medication, UpdateCallback callback) {
        repository.updateMedication(medicationId, medication, new RepositoryCallback<Medication>() {
            @Override
            public void onSuccess(Medication result) {
                callback.onSuccess();
            }

            @Override
            public void onError(String message) {
                error.setValue(message);
                callback.onError(message);
            }
        });
    }

    public void deleteMedication(String medicationId, UpdateCallback callback) {
        repository.deleteMedication(medicationId, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess();
            }

            @Override
            public void onError(String message) {
                error.setValue(message);
                callback.onError(message);
            }
        });
    }
}
