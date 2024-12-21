package com.feri.redmedalertandroidapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;
import com.feri.redmedalertandroidapp.api.model.EmergencyContact;
import com.feri.redmedalertandroidapp.api.model.MedicalProfile;
import com.feri.redmedalertandroidapp.api.model.User;
import com.feri.redmedalertandroidapp.repository.UserRepository;
import com.feri.redmedalertandroidapp.repository.RepositoryCallback;

public class UserViewModel extends AndroidViewModel{

    private final UserRepository userRepository;
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public interface UpdateCallback {
        void onSuccess();
        void onError(String message);
    }

    public UserViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        isLoading.setValue(false);
    }

    public LiveData<User> getCurrentUser() {
        return userRepository.getCurrentUser();
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void loadUserData(String userId) {
        isLoading.setValue(true);
        userRepository.fetchUserById(userId);
        isLoading.setValue(false);
    }

    public void updateUser(User user, UpdateCallback callback) {
        isLoading.setValue(true);
        userRepository.updateUser(user.getIdUser(), user, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User result) {
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

    public void createUser(User user, UpdateCallback callback) {
        isLoading.setValue(true);
        userRepository.createUser(user, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User result) {
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

    public void deleteUser(String userId, UpdateCallback callback) {
        isLoading.setValue(true);
        userRepository.deleteUser(userId, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
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

    public void loadUserEmergencyContacts(String userId) {
        isLoading.setValue(true);
        userRepository.fetchUserEmergencyContacts(userId, new RepositoryCallback<List<EmergencyContact>>() {
            @Override
            public void onSuccess(List<EmergencyContact> result) {
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                error.setValue(message);
            }
        });
    }

    public void loadUserMedicalProfile(String userId) {
        isLoading.setValue(true);
        userRepository.fetchUserMedicalProfile(userId, new RepositoryCallback<MedicalProfile>() {
            @Override
            public void onSuccess(MedicalProfile result) {
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                error.setValue(message);
            }
        });
    }
}
