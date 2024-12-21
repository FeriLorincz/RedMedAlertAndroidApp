package com.feri.redmedalertandroidapp.api.model;

import java.util.HashSet;
import java.util.Set;

public class MedicalProfile {

    private String idMedicalProfile;
    private User user;  // Adăugat pentru consistență
    private Set<String> currentDiseases = new HashSet<>();
    private Set<String> sensorModifyingConditions = new HashSet<>();
    private boolean hasAthleticHistory;
    private String athleticHistoryDetails;
    private Set<Medication> medications = new HashSet<>();
    private boolean gdprConsent;
    private boolean disclaimerAccepted;
    private boolean emergencyEntryPermission;

    // Constructor
    public MedicalProfile() {
        this.currentDiseases = new HashSet<>();
        this.sensorModifyingConditions = new HashSet<>();
        this.medications = new HashSet<>();
    }


    // Getters and Setters
    public String getIdMedicalProfile() {
        return idMedicalProfile;
    }

    public void setIdMedicalProfile(String idMedicalProfile) {
        this.idMedicalProfile = idMedicalProfile;
    }

    public Set<String> getCurrentDiseases() {
        return currentDiseases;
    }

    public void setCurrentDiseases(Set<String> currentDiseases) {
        this.currentDiseases = currentDiseases;
    }

    public Set<String> getSensorModifyingConditions() {
        return sensorModifyingConditions;
    }

    public void setSensorModifyingConditions(Set<String> sensorModifyingConditions) {
        this.sensorModifyingConditions = sensorModifyingConditions;
    }

    public boolean isHasAthleticHistory() {
        return hasAthleticHistory;
    }

    public void setHasAthleticHistory(boolean hasAthleticHistory) {
        this.hasAthleticHistory = hasAthleticHistory;
    }

    public String getAthleticHistoryDetails() {
        return athleticHistoryDetails;
    }

    public void setAthleticHistoryDetails(String athleticHistoryDetails) {
        this.athleticHistoryDetails = athleticHistoryDetails;
    }

    public Set<Medication> getMedications() {
        return medications;
    }

    public void setMedications(Set<Medication> medications) {
        this.medications = medications;
    }

    public boolean isGdprConsent() {
        return gdprConsent;
    }

    public void setGdprConsent(boolean gdprConsent) {
        this.gdprConsent = gdprConsent;
    }

    public boolean isDisclaimerAccepted() {
        return disclaimerAccepted;
    }

    public void setDisclaimerAccepted(boolean disclaimerAccepted) {
        this.disclaimerAccepted = disclaimerAccepted;
    }

    public boolean isEmergencyEntryPermission() {
        return emergencyEntryPermission;
    }

    public void setEmergencyEntryPermission(boolean emergencyEntryPermission) {
        this.emergencyEntryPermission = emergencyEntryPermission;
    }

    // Adăugăm getters și setters pentru user
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}