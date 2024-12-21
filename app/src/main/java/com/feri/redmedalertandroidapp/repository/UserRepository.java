package com.feri.redmedalertandroidapp.repository;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.feri.redmedalertandroidapp.api.RetrofitClient;
import com.feri.redmedalertandroidapp.api.model.User;
import com.feri.redmedalertandroidapp.api.model.EmergencyContact;
import com.feri.redmedalertandroidapp.api.model.MedicalProfile;
import com.feri.redmedalertandroidapp.api.service.UserApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class UserRepository {

    private static final String TAG = "UserRepository";
    private final UserApiService userApiService;
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<List<User>> allUsers = new MutableLiveData<>();

    public UserRepository(Context context) {
        userApiService = RetrofitClient.getUserService();
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<List<User>> getAllUsers() {
        return allUsers;
    }

    public void fetchUserById(String userId) {
        userApiService.getUserById(userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser.postValue(response.body());
                } else {
                    Log.e(TAG, "Error fetching user: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "Failed to fetch user", t);
            }
        });
    }

    public void fetchAllUsers() {
        userApiService.getAllUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allUsers.postValue(response.body());
                } else {
                    Log.e(TAG, "Error fetching users: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Log.e(TAG, "Failed to fetch users", t);
            }
        });
    }

    public void createUser(User user, RepositoryCallback<User> callback) {
        userApiService.createUser(user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error creating user: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                callback.onError("Failed to create user: " + t.getMessage());
            }
        });
    }

    public void updateUser(String userId, User user, RepositoryCallback<User> callback) {
        userApiService.updateUser(userId, user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error updating user: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                callback.onError("Failed to update user: " + t.getMessage());
            }
        });
    }

    public void deleteUser(String userId, RepositoryCallback<Void> callback) {
        userApiService.deleteUser(userId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Error deleting user: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Failed to delete user: " + t.getMessage());
            }
        });
    }

    public void fetchUserEmergencyContacts(String userId, RepositoryCallback<List<EmergencyContact>> callback) {
        userApiService.getUserEmergencyContacts(userId).enqueue(new Callback<List<EmergencyContact>>() {
            @Override
            public void onResponse(Call<List<EmergencyContact>> call, Response<List<EmergencyContact>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error fetching emergency contacts: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<EmergencyContact>> call, Throwable t) {
                callback.onError("Failed to fetch emergency contacts: " + t.getMessage());
            }
        });
    }

    public void fetchUserMedicalProfile(String userId, RepositoryCallback<MedicalProfile> callback) {
        userApiService.getUserMedicalProfile(userId).enqueue(new Callback<MedicalProfile>() {
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
                callback.onError("Failed to fetch medical profile: " + t.getMessage());
            }
        });
    }
}
