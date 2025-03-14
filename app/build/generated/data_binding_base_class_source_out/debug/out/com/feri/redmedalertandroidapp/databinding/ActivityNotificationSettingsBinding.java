// Generated by view binder compiler. Do not edit!
package com.feri.redmedalertandroidapp.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.feri.redmedalertandroidapp.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityNotificationSettingsBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final Button buttonReset;

  @NonNull
  public final Switch switchDataCollection;

  @NonNull
  public final Switch switchDataUpload;

  @NonNull
  public final Switch switchErrorNotifications;

  @NonNull
  public final Switch switchSound;

  @NonNull
  public final Switch switchVibrate;

  private ActivityNotificationSettingsBinding(@NonNull LinearLayout rootView,
      @NonNull Button buttonReset, @NonNull Switch switchDataCollection,
      @NonNull Switch switchDataUpload, @NonNull Switch switchErrorNotifications,
      @NonNull Switch switchSound, @NonNull Switch switchVibrate) {
    this.rootView = rootView;
    this.buttonReset = buttonReset;
    this.switchDataCollection = switchDataCollection;
    this.switchDataUpload = switchDataUpload;
    this.switchErrorNotifications = switchErrorNotifications;
    this.switchSound = switchSound;
    this.switchVibrate = switchVibrate;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityNotificationSettingsBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityNotificationSettingsBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_notification_settings, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityNotificationSettingsBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.button_reset;
      Button buttonReset = ViewBindings.findChildViewById(rootView, id);
      if (buttonReset == null) {
        break missingId;
      }

      id = R.id.switch_data_collection;
      Switch switchDataCollection = ViewBindings.findChildViewById(rootView, id);
      if (switchDataCollection == null) {
        break missingId;
      }

      id = R.id.switch_data_upload;
      Switch switchDataUpload = ViewBindings.findChildViewById(rootView, id);
      if (switchDataUpload == null) {
        break missingId;
      }

      id = R.id.switch_error_notifications;
      Switch switchErrorNotifications = ViewBindings.findChildViewById(rootView, id);
      if (switchErrorNotifications == null) {
        break missingId;
      }

      id = R.id.switch_sound;
      Switch switchSound = ViewBindings.findChildViewById(rootView, id);
      if (switchSound == null) {
        break missingId;
      }

      id = R.id.switch_vibrate;
      Switch switchVibrate = ViewBindings.findChildViewById(rootView, id);
      if (switchVibrate == null) {
        break missingId;
      }

      return new ActivityNotificationSettingsBinding((LinearLayout) rootView, buttonReset,
          switchDataCollection, switchDataUpload, switchErrorNotifications, switchSound,
          switchVibrate);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
