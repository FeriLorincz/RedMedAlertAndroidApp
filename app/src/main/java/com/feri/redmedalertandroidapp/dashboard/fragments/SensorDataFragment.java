package com.feri.redmedalertandroidapp.dashboard.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.snackbar.Snackbar;
import com.feri.redmedalertandroidapp.R;
import com.feri.redmedalertandroidapp.viewmodel.SensorDataViewModel;

public class SensorDataFragment extends Fragment {

    private SensorDataViewModel viewModel;

    // UI components - Vital Signs
    private TextView heartRateValue;
    private TextView bloodOxygenValue;
    private TextView bloodPressureValue;
    private TextView temperatureValue;

    // UI components - Motion Sensors
    private TextView accelerometerValue;
    private TextView gyroscopeValue;
    private TextView stepCountValue;
    private TextView gravityValue;
    private TextView linearAccelerationValue;
    private TextView rotationValue;
    private TextView orientationValue;
    private TextView magneticFieldValue;

    // UI components - Environment
    private TextView humidityValue;
    private TextView lightValue;
    private TextView proximityValue;

    // UI components - Samsung Health
    private TextView biaValue;
    private TextView stressValue;
    private TextView sleepValue;
    private TextView fallDetectionValue;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(SensorDataViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sensor_data, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);
        setupObservers();
    }

    private void setupViews(View view) {
        // Vital Signs
        heartRateValue = view.findViewById(R.id.heartRateValue);
        bloodOxygenValue = view.findViewById(R.id.bloodOxygenValue);
        bloodPressureValue = view.findViewById(R.id.bloodPressureValue);
        temperatureValue = view.findViewById(R.id.temperatureValue);

        // Motion Sensors
        accelerometerValue = view.findViewById(R.id.accelerometerValue);
        gyroscopeValue = view.findViewById(R.id.gyroscopeValue);
        stepCountValue = view.findViewById(R.id.stepCountValue);
        gravityValue = view.findViewById(R.id.gravityValue);
        linearAccelerationValue = view.findViewById(R.id.linearAccelerationValue);
        rotationValue = view.findViewById(R.id.rotationValue);
        orientationValue = view.findViewById(R.id.orientationValue);
        magneticFieldValue = view.findViewById(R.id.magneticFieldValue);

        // Environment
        humidityValue = view.findViewById(R.id.humidityValue);
        lightValue = view.findViewById(R.id.lightValue);
        proximityValue = view.findViewById(R.id.proximityValue);

        // Samsung Health
        biaValue = view.findViewById(R.id.biaValue);
        stressValue = view.findViewById(R.id.stressValue);
        sleepValue = view.findViewById(R.id.sleepValue);
        fallDetectionValue = view.findViewById(R.id.fallDetectionValue);
    }

    private void setupObservers() {
        // Vital Signs
        viewModel.getHeartRate().observe(getViewLifecycleOwner(),
                value -> heartRateValue.setText(formatValue(value, "BPM", 0)));

        viewModel.getBloodOxygen().observe(getViewLifecycleOwner(),
                value -> bloodOxygenValue.setText(formatValue(value, "%", 1)));

        viewModel.getBloodPressure().observe(getViewLifecycleOwner(),
                value -> bloodPressureValue.setText(formatValue(value, "mmHg", 0)));

        viewModel.getTemperature().observe(getViewLifecycleOwner(),
                value -> temperatureValue.setText(formatValue(value, "°C", 2)));

        // Motion Sensors
        viewModel.getAccelerometer().observe(getViewLifecycleOwner(),
                value -> accelerometerValue.setText(formatValue(value, "m/s²", 2)));

        viewModel.getGyroscope().observe(getViewLifecycleOwner(),
                value -> gyroscopeValue.setText(formatValue(value, "rad/s", 2)));

        viewModel.getStepCount().observe(getViewLifecycleOwner(),
                value -> stepCountValue.setText(formatValue(value, "steps", 0)));

        viewModel.getGravity().observe(getViewLifecycleOwner(),
                value -> gravityValue.setText(formatValue(value, "m/s²", 2)));

        viewModel.getLinearAcceleration().observe(getViewLifecycleOwner(),
                value -> linearAccelerationValue.setText(formatValue(value, "m/s²", 2)));

        viewModel.getRotation().observe(getViewLifecycleOwner(),
                value -> rotationValue.setText(formatValue(value, "°", 1)));

        viewModel.getOrientation().observe(getViewLifecycleOwner(),
                value -> orientationValue.setText(formatValue(value, "°", 1)));

        viewModel.getMagneticField().observe(getViewLifecycleOwner(),
                value -> magneticFieldValue.setText(formatValue(value, "μT", 2)));

        // Environment
        viewModel.getHumidity().observe(getViewLifecycleOwner(),
                value -> humidityValue.setText(formatValue(value, "%", 1)));

        viewModel.getLight().observe(getViewLifecycleOwner(),
                value -> lightValue.setText(formatValue(value, "lx", 0)));

        viewModel.getProximity().observe(getViewLifecycleOwner(),
                value -> proximityValue.setText(formatValue(value, "cm", 1)));

        // Samsung Health
        viewModel.getBia().observe(getViewLifecycleOwner(),
                value -> biaValue.setText(formatValue(value, "%", 1)));

        viewModel.getStress().observe(getViewLifecycleOwner(),
                value -> stressValue.setText(formatValue(value, "%", 0)));

        viewModel.getSleep().observe(getViewLifecycleOwner(),
                value -> sleepValue.setText(getSleepStatusText(value)));

        viewModel.getFallDetection().observe(getViewLifecycleOwner(),
                value -> fallDetectionValue.setText(getFallDetectionText(value)));

        // Error Handling
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(requireView(), error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private String formatValue(Double value, String unit, int decimals) {
        if (value == null) return "N/A";
        return String.format("%." + decimals + "f %s", value, unit);
    }

    private String getSleepStatusText(Double value) {
        if (value == null) return "N/A";
        if (value > 0.7) return "Deep Sleep";
        if (value > 0.3) return "Light Sleep";
        return "Awake";
    }

    private String getFallDetectionText(Double value) {
        if (value == null) return "N/A";
        return value > 0.5 ? "Fall Detected!" : "Normal";
    }
}