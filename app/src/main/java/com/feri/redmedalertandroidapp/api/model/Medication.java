package com.feri.redmedalertandroidapp.api.model;

import java.time.LocalDate;

public class Medication {

    private String idMedication;
    private MedicalProfile medicalProfile;
    private String medicationName;
    private LocalDate medicationStartDate;
    private String medicationDosage;

    // Getters and Setters existente
    public String getIdMedication() { return idMedication; }
    public void setIdMedication(String idMedication) { this.idMedication = idMedication; }

    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }

    public LocalDate getMedicationStartDate() { return medicationStartDate; }
    public void setMedicationStartDate(LocalDate medicationStartDate) {
        this.medicationStartDate = medicationStartDate;
    }

    public String getMedicationDosage() { return medicationDosage; }
    public void setMedicationDosage(String medicationDosage) { this.medicationDosage = medicationDosage; }

    // Noi getters și setters pentru medicalProfile
    public MedicalProfile getMedicalProfile() { return medicalProfile; }
    public void setMedicalProfile(MedicalProfile medicalProfile) { this.medicalProfile = medicalProfile; }
}
