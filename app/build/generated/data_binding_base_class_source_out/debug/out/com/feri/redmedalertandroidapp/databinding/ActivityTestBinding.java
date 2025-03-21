// Generated by view binder compiler. Do not edit!
package com.feri.redmedalertandroidapp.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.feri.redmedalertandroidapp.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityTestBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final Button sensorTestButton;

  @NonNull
  public final TextView statusText;

  @NonNull
  public final Button testButton;

  private ActivityTestBinding(@NonNull LinearLayout rootView, @NonNull Button sensorTestButton,
      @NonNull TextView statusText, @NonNull Button testButton) {
    this.rootView = rootView;
    this.sensorTestButton = sensorTestButton;
    this.statusText = statusText;
    this.testButton = testButton;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityTestBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityTestBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_test, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityTestBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.sensorTestButton;
      Button sensorTestButton = ViewBindings.findChildViewById(rootView, id);
      if (sensorTestButton == null) {
        break missingId;
      }

      id = R.id.statusText;
      TextView statusText = ViewBindings.findChildViewById(rootView, id);
      if (statusText == null) {
        break missingId;
      }

      id = R.id.testButton;
      Button testButton = ViewBindings.findChildViewById(rootView, id);
      if (testButton == null) {
        break missingId;
      }

      return new ActivityTestBinding((LinearLayout) rootView, sensorTestButton, statusText,
          testButton);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
