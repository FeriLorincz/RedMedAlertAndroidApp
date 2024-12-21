package com.feri.redmedalertandroidapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.feri.redmedalertandroidapp.api.model.MedicalProfile;
import com.feri.redmedalertandroidapp.repository.MedicalProfileRepository;
import com.feri.redmedalertandroidapp.repository.RepositoryCallback;
import java.util.List;
import java.util.Set;

public class MedicalProfileViewModel extends AndroidViewModel {

    private final MedicalProfileRepository repository;
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public interface UpdateCallback {
        void onSuccess();
        void onError(String message);
    }

    public MedicalProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new MedicalProfileRepository(application);
    }

    public LiveData<MedicalProfile> getCurrentProfile() {
        return repository.getCurrentProfile();
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void loadProfileData(String userId) {
        isLoading.setValue(true);
        repository.fetchProfileByUserId(userId);
        isLoading.setValue(false);
    }

    public void updateProfile(MedicalProfile profile, UpdateCallback callback) {
        isLoading.setValue(true);
        repository.updateProfile(profile.getIdMedicalProfile(), profile, new RepositoryCallback<MedicalProfile>() {
            @Override
            public void onSuccess(MedicalProfile result) {
                isLoading.setValue(false);
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
        MedicalProfile currentProfile = getCurrentProfile().getValue();
        if (currentProfile != null) {
            Set<String> diseases = currentProfile.getCurrentDiseases();
            diseases.add(disease);
            updateProfile(currentProfile, new UpdateCallback() {
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
        MedicalProfile currentProfile = getCurrentProfile().getValue();
        if (currentProfile != null) {
            Set<String> diseases = currentProfile.getCurrentDiseases();
            diseases.remove(disease);
            updateProfile(currentProfile, new UpdateCallback() {
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
        MedicalProfile currentProfile = getCurrentProfile().getValue();
        if (currentProfile != null) {
            currentProfile.setHasAthleticHistory(hasHistory);
            currentProfile.setAthleticHistoryDetails(details);
            updateProfile(currentProfile, new UpdateCallback() {
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
        MedicalProfile currentProfile = getCurrentProfile().getValue();
        if (currentProfile != null) {
            currentProfile.setGdprConsent(gdpr);
            currentProfile.setDisclaimerAccepted(disclaimer);
            currentProfile.setEmergencyEntryPermission(emergencyEntry);
            updateProfile(currentProfile, new UpdateCallback() {
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
