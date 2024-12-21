package com.feri.redmedalertandroidapp.repository;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.feri.redmedalertandroidapp.api.RetrofitClient;
import com.feri.redmedalertandroidapp.api.model.EmergencyContact;
import com.feri.redmedalertandroidapp.api.service.EmergencyContactApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class EmergencyContactRepository {

    private static final String TAG = "EmergencyContactRepo";
    private final EmergencyContactApiService contactApiService;
    private final MutableLiveData<List<EmergencyContact>> contacts = new MutableLiveData<>();

    public EmergencyContactRepository(Context context) {
        contactApiService = RetrofitClient.getEmergencyContactService();
    }

    public LiveData<List<EmergencyContact>> getContacts() {
        return contacts;
    }

    public void fetchContactsByUserId(String userId) {
        contactApiService.getContactsByUserId(userId).enqueue(new Callback<List<EmergencyContact>>() {
            @Override
            public void onResponse(Call<List<EmergencyContact>> call, Response<List<EmergencyContact>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    contacts.postValue(response.body());
                } else {
                    Log.e(TAG, "Error fetching contacts: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<EmergencyContact>> call, Throwable t) {
                Log.e(TAG, "Failed to fetch contacts", t);
            }
        });
    }

    public void createContact(EmergencyContact contact, RepositoryCallback<EmergencyContact> callback) {
        contactApiService.createContact(contact).enqueue(new Callback<EmergencyContact>() {
            @Override
            public void onResponse(Call<EmergencyContact> call, Response<EmergencyContact> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Actualizăm lista de contacte
                    List<EmergencyContact> currentContacts = contacts.getValue();
                    if (currentContacts != null) {
                        currentContacts.add(response.body());
                        contacts.postValue(currentContacts);
                    }
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error creating contact: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<EmergencyContact> call, Throwable t) {
                callback.onError("Failed to create contact: " + t.getMessage());
            }
        });
    }

    public void updateContact(String contactId, EmergencyContact contact, RepositoryCallback<EmergencyContact> callback) {
        contactApiService.updateContact(contactId, contact).enqueue(new Callback<EmergencyContact>() {
            @Override
            public void onResponse(Call<EmergencyContact> call, Response<EmergencyContact> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Actualizăm lista de contacte
                    List<EmergencyContact> currentContacts = contacts.getValue();
                    if (currentContacts != null) {
                        int index = -1;
                        for (int i = 0; i < currentContacts.size(); i++) {
                            if (currentContacts.get(i).getIdContact().equals(contactId)) {
                                index = i;
                                break;
                            }
                        }
                        if (index != -1) {
                            currentContacts.set(index, response.body());
                            contacts.postValue(currentContacts);
                        }
                    }
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error updating contact: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<EmergencyContact> call, Throwable t) {
                callback.onError("Failed to update contact: " + t.getMessage());
            }
        });
    }

    public void deleteContact(String contactId, RepositoryCallback<Void> callback) {
        contactApiService.deleteContact(contactId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Ștergem contactul din lista locală
                    List<EmergencyContact> currentContacts = contacts.getValue();
                    if (currentContacts != null) {
                        currentContacts.removeIf(contact ->
                                contact.getIdContact().equals(contactId));
                        contacts.postValue(currentContacts);
                    }
                    callback.onSuccess(null);
                } else {
                    callback.onError("Error deleting contact: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Failed to delete contact: " + t.getMessage());
            }
        });
    }
}