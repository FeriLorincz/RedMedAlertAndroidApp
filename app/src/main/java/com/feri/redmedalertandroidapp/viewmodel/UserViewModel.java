package com.feri.redmedalertandroidapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.feri.redmedalertandroidapp.api.model.User;
import com.feri.redmedalertandroidapp.repository.UserRepository;
import com.feri.redmedalertandroidapp.repository.RepositoryCallback;

public class UserViewModel extends AndroidViewModel{

    private final UserRepository userRepository;
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public interface UpdateCallback {
        void onSuccess();
        void onError(String message);
    }

    public UserViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    public LiveData<User> getCurrentUser() {
        return userRepository.getCurrentUser();
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadUserData(String userId) {
        userRepository.fetchUserById(userId);
    }

    public void updateUser(User user, UpdateCallback callback) {
        userRepository.updateUser(user.getIdUser(), user, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User result) {
                callback.onSuccess();
            }

            @Override
            public void onError(String message) {
                error.setValue(message);
                callback.onError(message);
            }
        });
    }

    public void createUser(User user, UpdateCallback callback) {
        userRepository.createUser(user, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User result) {
                callback.onSuccess();
            }

            @Override
            public void onError(String message) {
                error.setValue(message);
                callback.onError(message);
            }
        });
    }

    public void deleteUser(String userId, UpdateCallback callback) {
        userRepository.deleteUser(userId, new RepositoryCallback<Void>() {
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
