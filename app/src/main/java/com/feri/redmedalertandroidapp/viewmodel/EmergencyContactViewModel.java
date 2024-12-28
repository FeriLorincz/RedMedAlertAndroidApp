package com.feri.redmedalertandroidapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.feri.redmedalertandroidapp.api.model.EmergencyContact;
import com.feri.redmedalertandroidapp.repository.EmergencyContactRepository;
import com.feri.redmedalertandroidapp.repository.RepositoryCallback;
import java.util.List;

public class EmergencyContactViewModel extends AndroidViewModel {

    private final EmergencyContactRepository repository;
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public interface UpdateCallback {
        void onSuccess();
        void onError(String message);
    }

    public EmergencyContactViewModel(@NonNull Application application) {
        super(application);
        repository = new EmergencyContactRepository(application);
    }

    public LiveData<List<EmergencyContact>> getContacts() {
        return repository.getContacts();
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadContactsForUser(String userId) {
        repository.fetchContactsByUserId(userId);
    }

    public void createContact(EmergencyContact contact, UpdateCallback callback) {
        repository.createContact(contact, new RepositoryCallback<EmergencyContact>() {
            @Override
            public void onSuccess(EmergencyContact result) {
                callback.onSuccess();
            }

            @Override
            public void onError(String message) {
                error.setValue(message);
                callback.onError(message);
            }
        });
    }

    public void updateContact(String contactId, EmergencyContact contact, UpdateCallback callback) {
        repository.updateContact(contactId, contact, new RepositoryCallback<EmergencyContact>() {
            @Override
            public void onSuccess(EmergencyContact result) {
                callback.onSuccess();
            }

            @Override
            public void onError(String message) {
                error.setValue(message);
                callback.onError(message);
            }
        });
    }

    public void deleteContact(String contactId, UpdateCallback callback) {
        repository.deleteContact(contactId, new RepositoryCallback<Void>() {
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
