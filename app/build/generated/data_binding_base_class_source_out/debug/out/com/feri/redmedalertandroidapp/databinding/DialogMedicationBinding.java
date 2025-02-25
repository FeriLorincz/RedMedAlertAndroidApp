// Generated by view binder compiler. Do not edit!
package com.feri.redmedalertandroidapp.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.feri.redmedalertandroidapp.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class DialogMedicationBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final TextInputEditText medicationDosageInput;

  @NonNull
  public final TextInputLayout medicationDosageLayout;

  @NonNull
  public final TextInputEditText medicationNameInput;

  @NonNull
  public final TextInputLayout medicationNameLayout;

  @NonNull
  public final TextInputEditText startDateInput;

  @NonNull
  public final TextInputLayout startDateLayout;

  private DialogMedicationBinding(@NonNull LinearLayout rootView,
      @NonNull TextInputEditText medicationDosageInput,
      @NonNull TextInputLayout medicationDosageLayout,
      @NonNull TextInputEditText medicationNameInput, @NonNull TextInputLayout medicationNameLayout,
      @NonNull TextInputEditText startDateInput, @NonNull TextInputLayout startDateLayout) {
    this.rootView = rootView;
    this.medicationDosageInput = medicationDosageInput;
    this.medicationDosageLayout = medicationDosageLayout;
    this.medicationNameInput = medicationNameInput;
    this.medicationNameLayout = medicationNameLayout;
    this.startDateInput = startDateInput;
    this.startDateLayout = startDateLayout;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static DialogMedicationBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static DialogMedicationBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.dialog_medication, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static DialogMedicationBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.medicationDosageInput;
      TextInputEditText medicationDosageInput = ViewBindings.findChildViewById(rootView, id);
      if (medicationDosageInput == null) {
        break missingId;
      }

      id = R.id.medicationDosageLayout;
      TextInputLayout medicationDosageLayout = ViewBindings.findChildViewById(rootView, id);
      if (medicationDosageLayout == null) {
        break missingId;
      }

      id = R.id.medicationNameInput;
      TextInputEditText medicationNameInput = ViewBindings.findChildViewById(rootView, id);
      if (medicationNameInput == null) {
        break missingId;
      }

      id = R.id.medicationNameLayout;
      TextInputLayout medicationNameLayout = ViewBindings.findChildViewById(rootView, id);
      if (medicationNameLayout == null) {
        break missingId;
      }

      id = R.id.startDateInput;
      TextInputEditText startDateInput = ViewBindings.findChildViewById(rootView, id);
      if (startDateInput == null) {
        break missingId;
      }

      id = R.id.startDateLayout;
      TextInputLayout startDateLayout = ViewBindings.findChildViewById(rootView, id);
      if (startDateLayout == null) {
        break missingId;
      }

      return new DialogMedicationBinding((LinearLayout) rootView, medicationDosageInput,
          medicationDosageLayout, medicationNameInput, medicationNameLayout, startDateInput,
          startDateLayout);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
