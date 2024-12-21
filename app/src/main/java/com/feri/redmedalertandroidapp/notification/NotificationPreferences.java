package com.feri.redmedalertandroidapp.notification;

import android.content.Context;
import android.content.SharedPreferences;

public class NotificationPreferences {

    private static final String PREF_NAME = "notification_preferences";
    private static final String KEY_SHOW_DATA_COLLECTION = "show_data_collection";
    private static final String KEY_SHOW_DATA_UPLOAD = "show_data_upload";
    private static final String KEY_SHOW_ERROR_NOTIFICATIONS = "show_error_notifications";
    private static final String KEY_VIBRATE = "vibrate_enabled";
    private static final String KEY_SOUND = "sound_enabled";

    private final SharedPreferences preferences;

    public NotificationPreferences(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setShowDataCollection(boolean enabled) {
        preferences.edit().putBoolean(KEY_SHOW_DATA_COLLECTION, enabled).apply();
    }

    public boolean showDataCollection() {
        return preferences.getBoolean(KEY_SHOW_DATA_COLLECTION, true);
    }

    public void setShowDataUpload(boolean enabled) {
        preferences.edit().putBoolean(KEY_SHOW_DATA_UPLOAD, enabled).apply();
    }

    public boolean showDataUpload() {
        return preferences.getBoolean(KEY_SHOW_DATA_UPLOAD, true);
    }

    public void setShowErrorNotifications(boolean enabled) {
        preferences.edit().putBoolean(KEY_SHOW_ERROR_NOTIFICATIONS, enabled).apply();
    }

    public boolean showErrorNotifications() {
        return preferences.getBoolean(KEY_SHOW_ERROR_NOTIFICATIONS, true);
    }

    public void setVibrateEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_VIBRATE, enabled).apply();
    }

    public boolean isVibrateEnabled() {
        return preferences.getBoolean(KEY_VIBRATE, true);
    }

    public void setSoundEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_SOUND, enabled).apply();
    }

    public boolean isSoundEnabled() {
        return preferences.getBoolean(KEY_SOUND, true);
    }

    public void resetToDefaults() {
        preferences.edit()
                .putBoolean(KEY_SHOW_DATA_COLLECTION, true)
                .putBoolean(KEY_SHOW_DATA_UPLOAD, true)
                .putBoolean(KEY_SHOW_ERROR_NOTIFICATIONS, true)
                .putBoolean(KEY_VIBRATE, true)
                .putBoolean(KEY_SOUND, true)
                .apply();
    }
}
