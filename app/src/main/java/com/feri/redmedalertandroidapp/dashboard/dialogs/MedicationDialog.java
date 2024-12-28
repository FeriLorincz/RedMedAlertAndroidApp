package com.feri.redmedalertandroidapp.dashboard.dialogs;

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import androidx.appcompat.app.AlertDialog;
import com.feri.redmedalertandroidapp.R;
import com.feri.redmedalertandroidapp.api.model.MedicalProfile;
import com.feri.redmedalertandroidapp.api.model.Medication;
import com.google.android.material.textfield.TextInputEditText;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class MedicationDialog {

    private final Context context;
    private final OnMedicationSaveListener listener;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private Medication existingMedication;
    private MedicalProfile medicalProfile;
    private TextInputEditText nameInput;
    private TextInputEditText dosageInput;
    private TextInputEditText startDateInput;
    private LocalDate selectedDate;

    public interface OnMedicationSaveListener {
        void onMedicationSaved(Medication medication);
    }

    public MedicationDialog(Context context, OnMedicationSaveListener listener, MedicalProfile medicalProfile) {
        this.context = context;
        this.listener = listener;
        this.medicalProfile = medicalProfile;
    }

    public void show(Medication medication) {
        this.existingMedication = medication;

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_medication, null);
        initializeViews(dialogView);
        setupDatePicker();

        if (existingMedication != null) {
            populateFields();
        } else {
            selectedDate = LocalDate.now();
            startDateInput.setText(selectedDate.format(dateFormatter));
        }

        new AlertDialog.Builder(context)
                .setTitle(existingMedication != null ? "Editare medicament" : "Adăugare medicament")
                .setView(dialogView)
                .setPositiveButton("Salvează", (dialog, which) -> saveMedication())
                .setNegativeButton("Anulează", null)
                .show();
    }

    private void initializeViews(View view) {
        nameInput = view.findViewById(R.id.medicationNameInput);
        dosageInput = view.findViewById(R.id.medicationDosageInput);
        startDateInput = view.findViewById(R.id.startDateInput);
    }

    private void setupDatePicker() {
        startDateInput.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            if (selectedDate != null) {
                calendar.set(selectedDate.getYear(), selectedDate.getMonthValue() - 1, selectedDate.getDayOfMonth());
            }

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    context,
                    (view, year, month, dayOfMonth) -> {
                        selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                        startDateInput.setText(selectedDate.format(dateFormatter));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
    }

    private void populateFields() {
        nameInput.setText(existingMedication.getMedicationName());
        dosageInput.setText(existingMedication.getMedicationDosage());
        selectedDate = existingMedication.getMedicationStartDate();
        startDateInput.setText(selectedDate.format(dateFormatter));
    }

    private void saveMedication() {
        String name = nameInput.getText().toString().trim();
        String dosage = dosageInput.getText().toString().trim();

        if (name.isEmpty() || dosage.isEmpty() || selectedDate == null) {
            return;
        }

        Medication medication = new Medication();
        if (existingMedication != null) {
            medication.setIdMedication(existingMedication.getIdMedication());
        }
        medication.setMedicationName(name);
        medication.setMedicationDosage(dosage);
        medication.setMedicationStartDate(selectedDate);
        medication.setMedicalProfile(medicalProfile);

        listener.onMedicationSaved(medication);
    }
}
