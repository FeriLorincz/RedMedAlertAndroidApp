package com.feri.redmedalertandroidapp.notification;

import android.os.Bundle;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;
import com.feri.redmedalertandroidapp.R;

public class NotificationSettingsActivity extends AppCompatActivity {

    private NotificationPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        preferences = new NotificationPreferences(this);

        setupSwitches();
    }

    private void setupSwitches() {
        Switch dataCollectionSwitch = findViewById(R.id.switch_data_collection);
        Switch dataUploadSwitch = findViewById(R.id.switch_data_upload);
        Switch errorNotificationsSwitch = findViewById(R.id.switch_error_notifications);
        Switch vibrateSwitch = findViewById(R.id.switch_vibrate);
        Switch soundSwitch = findViewById(R.id.switch_sound);

        // Setăm starea inițială
        dataCollectionSwitch.setChecked(preferences.showDataCollection());
        dataUploadSwitch.setChecked(preferences.showDataUpload());
        errorNotificationsSwitch.setChecked(preferences.showErrorNotifications());
        vibrateSwitch.setChecked(preferences.isVibrateEnabled());
        soundSwitch.setChecked(preferences.isSoundEnabled());

        // Adăugăm listeners
        dataCollectionSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                preferences.setShowDataCollection(isChecked));

        dataUploadSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                preferences.setShowDataUpload(isChecked));

        errorNotificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                preferences.setShowErrorNotifications(isChecked));

        vibrateSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                preferences.setVibrateEnabled(isChecked));

        soundSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                preferences.setSoundEnabled(isChecked));

        findViewById(R.id.button_reset).setOnClickListener(v -> {
            preferences.resetToDefaults();
            setupSwitches(); // Reîmprospătăm switchurile
        });
    }
}
