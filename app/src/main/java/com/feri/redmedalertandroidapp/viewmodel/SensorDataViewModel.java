package com.feri.redmedalertandroidapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.Map;

public class SensorDataViewModel extends AndroidViewModel{

    private final MutableLiveData<Map<String, Double>> sensorData = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public SensorDataViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Map<String, Double>> getSensorData() {
        return sensorData;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void updateSensorData(Map<String, Double> newData) {
        sensorData.setValue(newData);
    }

}
