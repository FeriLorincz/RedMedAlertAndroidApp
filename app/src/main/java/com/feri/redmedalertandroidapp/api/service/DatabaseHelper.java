package com.feri.redmedalertandroidapp.api.service;

import android.content.Context;
import android.util.Log;
import androidx.room.Room;

import com.feri.redmedalertandroidapp.api.database.HealthDatabase;
import com.feri.redmedalertandroidapp.api.model.HealthDataEntity;
import com.feri.redmedalertandroidapp.api.config.ApiClient;
import com.feri.redmedalertandroidapp.api.config.ApiClient.ApiCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper {

    private static final String TAG = "DatabaseHelper";
    private static DatabaseHelper instance;
    private final HealthDatabase database;
    private final Context context;

    private DatabaseHelper(Context context) {
        this.context = context.getApplicationContext();
        database = Room.databaseBuilder(context,
                        HealthDatabase.class, "redmedalert-db")
                .fallbackToDestructiveMigration()
                .build();
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public void cacheHealthData(Map<String, Double> data) {
        long timestamp = System.currentTimeMillis();

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            HealthDataEntity entity = new HealthDataEntity();
            entity.timestamp = timestamp;
            entity.dataType = entry.getKey();
            entity.value = entry.getValue();
            entity.uploaded = false;

            database.healthDataDao().insert(entity);
        }
    }

    // Adăugăm metoda pentru a obține datele neîncărcate
    public List<HealthDataEntity> getUnuploadedData() {
        return database.healthDataDao().getUnuploadedData();
    }

    public void uploadCachedData() {
        List<HealthDataEntity> unuploadedData = getUnuploadedData();
        if (!unuploadedData.isEmpty()) {
            // Convertim datele pentru upload
            Map<String, Double> dataToUpload = new HashMap<>();
            List<Long> uploadedIds = new ArrayList<>();

            for (HealthDataEntity entity : unuploadedData) {
                dataToUpload.put(entity.dataType, entity.value);
                uploadedIds.add(entity.id);
            }

            // Încercăm să încărcăm datele
            ApiClient.getInstance(context).uploadHealthData(dataToUpload, new ApiCallback() {
                @Override
                public void onSuccess() {
                    database.healthDataDao().markAsUploaded(uploadedIds);
                }

                @Override
                public void onError(String error) {
                    Log.e("DatabaseHelper", "Failed to upload cached data: " + error);
                }
            });
        }
    }
}
