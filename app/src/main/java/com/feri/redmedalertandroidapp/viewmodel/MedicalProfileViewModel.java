package com.feri.redmedalertandroidapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.feri.redmedalertandroidapp.api.model.MedicalProfile;
import com.feri.redmedalertandroidapp.api.model.Medication;
import com.feri.redmedalertandroidapp.repository.MedicalProfileRepository;
import com.feri.redmedalertandroidapp.repository.RepositoryCallback;

import java.util.List;
import java.util.Set;

public class MedicalProfileViewModel extends AndroidViewModel {

    private final MedicalProfileRepository repository;
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<MedicalProfile> currentProfile = new MutableLiveData<>();

    public interface UpdateCallback {
        void onSuccess();
        void onError(String message);
    }

    public MedicalProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new MedicalProfileRepository(application);
    }

    public LiveData<MedicalProfile> getCurrentProfile() {
        return currentProfile;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void loadProfileData(String userId) {
        isLoading.setValue(true);
        repository.fetchProfileByUserId(userId, new RepositoryCallback<MedicalProfile>() {
            @Override
            public void onSuccess(MedicalProfile result) {
                isLoading.setValue(false);
                currentProfile.setValue(result);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                error.setValue(message);
            }
        });
    }

    public void updateProfile(MedicalProfile profile, UpdateCallback callback) {
        isLoading.setValue(true);
        repository.updateMedicalProfile(profile.getIdMedicalProfile(), profile, new RepositoryCallback<MedicalProfile>() {
            @Override
            public void onSuccess(MedicalProfile result) {
                isLoading.setValue(false);
                currentProfile.setValue(result);
                callback.onSuccess();
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                error.setValue(message);
                callback.onError(message);
            }
        });
    }

    public void addDisease(String disease) {
        MedicalProfile currentProfileValue = currentProfile.getValue();
        if (currentProfileValue != null) {
            Set<String> diseases = currentProfileValue.getCurrentDiseases();
            diseases.add(disease);
            updateProfile(currentProfileValue, new UpdateCallback() {
                @Override
                public void onSuccess() {
                    // Disease added successfully
                }

                @Override
                public void onError(String message) {
                    error.setValue("Eroare la adăugarea bolii: " + message);
                }
            });
        }
    }

    public void removeDisease(String disease) {
        MedicalProfile currentProfileValue = currentProfile.getValue();
        if (currentProfileValue != null) {
            Set<String> diseases = currentProfileValue.getCurrentDiseases();
            diseases.remove(disease);
            updateProfile(currentProfileValue, new UpdateCallback() {
                @Override
                public void onSuccess() {
                    // Disease removed successfully
                }

                @Override
                public void onError(String message) {
                    error.setValue("Eroare la ștergerea bolii: " + message);
                }
            });
        }
    }

    public void updateAthleticHistory(boolean hasHistory, String details) {
        MedicalProfile currentProfileValue = currentProfile.getValue();
        if (currentProfileValue != null) {
            currentProfileValue.setHasAthleticHistory(hasHistory);
            currentProfileValue.setAthleticHistoryDetails(details);
            updateProfile(currentProfileValue, new UpdateCallback() {
                @Override
                public void onSuccess() {
                    // Athletic history updated successfully
                }

                @Override
                public void onError(String message) {
                    error.setValue("Eroare la actualizarea istoricului sportiv: " + message);
                }
            });
        }
    }

    public void updateConsents(boolean gdpr, boolean disclaimer, boolean emergencyEntry) {
        MedicalProfile currentProfileValue = currentProfile.getValue();
        if (currentProfileValue != null) {
            currentProfileValue.setGdprConsent(gdpr);
            currentProfileValue.setDisclaimerAccepted(disclaimer);
            currentProfileValue.setEmergencyEntryPermission(emergencyEntry);
            updateProfile(currentProfileValue, new UpdateCallback() {
                @Override
                public void onSuccess() {
                    // Consents updated successfully
                }

                @Override
                public void onError(String message) {
                    error.setValue("Eroare la actualizarea acordurilor: " + message);
                }
            });
        }
    }
}