package com.feri.redmedalertandroidapp.dashboard.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.feri.redmedalertandroidapp.R;
import com.feri.redmedalertandroidapp.api.model.MedicalProfile;
import com.feri.redmedalertandroidapp.api.model.Medication;
import com.feri.redmedalertandroidapp.dashboard.adapters.MedicationsAdapter;
import com.feri.redmedalertandroidapp.dashboard.dialogs.MedicationDialog;
import com.feri.redmedalertandroidapp.viewmodel.MedicalProfileViewModel;
import com.feri.redmedalertandroidapp.viewmodel.MedicationViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;

public class MedicalProfileFragment extends Fragment implements MedicationsAdapter.OnMedicationClickListener {

    private MedicalProfileViewModel viewModel;
    private MedicationViewModel medicationViewModel;
    private ChipGroup currentDiseasesChipGroup;
    private ChipGroup sensorConditionsChipGroup;
    private CheckBox athleticHistoryCheck;
    private TextInputLayout athleticHistoryLayout;
    private TextInputEditText athleticHistoryInput;
    private RecyclerView medicationsRecyclerView;
    private MedicationsAdapter medicationsAdapter;
    private Button addMedicationButton;
    private Button addDiseaseButton;
    private CheckBox gdprConsentCheck;
    private CheckBox disclaimerCheck;
    private CheckBox emergencyEntryCheck;
    private Button saveProfileButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MedicalProfileViewModel.class);
        medicationViewModel = new ViewModelProvider(requireActivity()).get(MedicationViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_medical_profile, container, false);
        initializeViews(view);
        setupObservers();
        setupListeners();
        return view;
    }

    private void initializeViews(View view) {
        currentDiseasesChipGroup = view.findViewById(R.id.currentDiseasesChipGroup);
        sensorConditionsChipGroup = view.findViewById(R.id.sensorConditionsChipGroup);
        athleticHistoryCheck = view.findViewById(R.id.athleticHistoryCheck);
        athleticHistoryLayout = view.findViewById(R.id.athleticHistoryLayout);
        athleticHistoryInput = view.findViewById(R.id.athleticHistoryInput);
        medicationsRecyclerView = view.findViewById(R.id.medicationsRecyclerView);
        addMedicationButton = view.findViewById(R.id.addMedicationButton);
        addDiseaseButton = view.findViewById(R.id.addDiseaseButton);
        gdprConsentCheck = view.findViewById(R.id.gdprConsentCheck);
        disclaimerCheck = view.findViewById(R.id.disclaimerCheck);
        emergencyEntryCheck = view.findViewById(R.id.emergencyEntryCheck);
        saveProfileButton = view.findViewById(R.id.saveProfileButton);

        // Configurare RecyclerView pentru medicamente
        medicationsAdapter = new MedicationsAdapter(this);
        medicationsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        medicationsRecyclerView.setAdapter(medicationsAdapter);
    }

    private void setupObservers() {
        viewModel.getCurrentProfile().observe(getViewLifecycleOwner(), this::populateFields);

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        medicationViewModel.getMedications().observe(getViewLifecycleOwner(), medications -> {
            medicationsAdapter.setMedications(medications);
        });

        medicationViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupListeners() {
        athleticHistoryCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            athleticHistoryLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        addDiseaseButton.setOnClickListener(v -> showAddDiseaseDialog());
        addMedicationButton.setOnClickListener(v -> showAddMedicationDialog());
        saveProfileButton.setOnClickListener(v -> saveProfile());
    }

    private void populateFields(MedicalProfile profile) {
        if (profile == null) return;

        // Populate diseases
        currentDiseasesChipGroup.removeAllViews();
        for (String disease : profile.getCurrentDiseases()) {
            addDiseaseChip(disease);
        }

        // Populate sensor conditions
        sensorConditionsChipGroup.removeAllViews();
        for (String condition : profile.getSensorModifyingConditions()) {
            addSensorConditionChip(condition);
        }

        // Athletic history
        athleticHistoryCheck.setChecked(profile.isHasAthleticHistory());
        athleticHistoryInput.setText(profile.getAthleticHistoryDetails());
        athleticHistoryLayout.setVisibility(profile.isHasAthleticHistory() ? View.VISIBLE : View.GONE);

        // Consents
        gdprConsentCheck.setChecked(profile.isGdprConsent());
        disclaimerCheck.setChecked(profile.isDisclaimerAccepted());
        emergencyEntryCheck.setChecked(profile.isEmergencyEntryPermission());

        // Încărcăm medicamentele pentru profil
        if (profile.getIdMedicalProfile() != null) {
            medicationViewModel.loadMedicationsForProfile(profile.getIdMedicalProfile());
        }
    }

    private void addDiseaseChip(String disease) {
        Chip chip = new Chip(requireContext());
        chip.setText(disease);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            viewModel.removeDisease(disease);
            currentDiseasesChipGroup.removeView(chip);
        });
        currentDiseasesChipGroup.addView(chip);
    }

    private void addSensorConditionChip(String condition) {
        Chip chip = new Chip(requireContext());
        chip.setText(condition);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            sensorConditionsChipGroup.removeView(chip);
        });
        sensorConditionsChipGroup.addView(chip);
    }

    private void showAddDiseaseDialog() {
        TextInputEditText input = new TextInputEditText(requireContext());
        input.setHint("Introduceți numele bolii");

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Adăugare boală")
                .setView(input)
                .setPositiveButton("Adaugă", (dialog, which) -> {
                    String disease = input.getText().toString().trim();
                    if (!disease.isEmpty()) {
                        viewModel.addDisease(disease);
                        addDiseaseChip(disease);
                    }
                })
                .setNegativeButton("Anulează", null)
                .show();
    }

    private void showAddMedicationDialog() {
        MedicalProfile currentProfile = viewModel.getCurrentProfile().getValue();
        if (currentProfile == null) {
            Toast.makeText(requireContext(), "Eroare: Profilul medical nu este disponibil",
                    Toast.LENGTH_LONG).show();
            return;
        }

        MedicationDialog dialog = new MedicationDialog(
                requireContext(),
                medication -> {
                    medicationViewModel.addMedication(
                            currentProfile.getIdMedicalProfile(),
                            medication,
                            new MedicationViewModel.UpdateCallback() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(requireContext(),
                                            "Medicament adăugat cu succes",
                                            Toast.LENGTH_SHORT).show();
                                    medicationViewModel.loadMedicationsForProfile(
                                            currentProfile.getIdMedicalProfile());
                                }

                                @Override
                                public void onError(String message) {
                                    Toast.makeText(requireContext(),
                                            "Eroare la adăugarea medicamentului: " + message,
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                    );
                },
                currentProfile
        );
        dialog.show(null);
    }

    private void saveProfile() {
        MedicalProfile currentProfile = viewModel.getCurrentProfile().getValue();
        if (currentProfile == null) return;

        currentProfile.setHasAthleticHistory(athleticHistoryCheck.isChecked());
        currentProfile.setAthleticHistoryDetails(athleticHistoryInput.getText().toString());
        currentProfile.setGdprConsent(gdprConsentCheck.isChecked());
        currentProfile.setDisclaimerAccepted(disclaimerCheck.isChecked());
        currentProfile.setEmergencyEntryPermission(emergencyEntryCheck.isChecked());

        viewModel.updateProfile(currentProfile, new MedicalProfileViewModel.UpdateCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(),
                        "Profilul medical a fost actualizat",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(),
                        "Eroare: " + message,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onMedicationEdit(Medication medication) {
        MedicalProfile currentProfile = viewModel.getCurrentProfile().getValue();
        if (currentProfile == null) return;

        MedicationDialog dialog = new MedicationDialog(
                requireContext(),
                updatedMedication -> {
                    medicationViewModel.updateMedication(
                            medication.getIdMedication(),
                            updatedMedication,
                            new MedicationViewModel.UpdateCallback() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(requireContext(),
                                            "Medicament actualizat cu succes",
                                            Toast.LENGTH_SHORT).show();
                                    medicationViewModel.loadMedicationsForProfile(
                                            currentProfile.getIdMedicalProfile());
                                }

                                @Override
                                public void onError(String message) {
                                    Toast.makeText(requireContext(),
                                            "Eroare la actualizarea medicamentului: " + message,
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                    );
                },
                currentProfile
        );
        dialog.show(medication);
    }

    @Override
    public void onMedicationDelete(Medication medication) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Ștergere medicament")
                .setMessage("Sigur doriți să ștergeți acest medicament?")
                .setPositiveButton("Da", (dialog, which) -> {
                    medicationViewModel.deleteMedication(
                            medication.getIdMedication(),
                            new MedicationViewModel.UpdateCallback() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(requireContext(),
                                            "Medicament șters cu succes",
                                            Toast.LENGTH_SHORT).show();
                                    MedicalProfile currentProfile = viewModel.getCurrentProfile().getValue();
                                    if (currentProfile != null) {
                                        medicationViewModel.loadMedicationsForProfile(
                                                currentProfile.getIdMedicalProfile());
                                    }
                                }

                                @Override
                                public void onError(String message) {
                                    Toast.makeText(requireContext(),
                                            "Eroare la ștergerea medicamentului: " + message,
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                    );
                })
                .setNegativeButton("Nu", null)
                .show();
    }
}