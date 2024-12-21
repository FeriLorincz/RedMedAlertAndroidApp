package com.feri.redmedalertandroidapp.api.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class User {

    private String idUser;
    private String firstNameUser;
    private String lastNameUser;
    private LocalDate dateOfBirthUser;
    private UserType userType;
    private AddressUser addressUser;
    private String phoneNumberUser;
    private String emailUser;
    private String password;
    private String resetPasswordToken;
    private LocalDateTime resetPasswordTokenExpiry;
    private Set<EmergencyContact> emergencyContacts = new HashSet<>();
    private MedicalProfile medicalProfile;
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private boolean enabled = true;

    // Constructor
    public User() {
        this.emergencyContacts = new HashSet<>();
    }

    // Getters and Setters
    public String getIdUser() { return idUser; }
    public void setIdUser(String idUser) { this.idUser = idUser; }

    public String getFirstNameUser() { return firstNameUser; }
    public void setFirstNameUser(String firstNameUser) { this.firstNameUser = firstNameUser; }

    public String getLastNameUser() { return lastNameUser; }
    public void setLastNameUser(String lastNameUser) { this.lastNameUser = lastNameUser; }

    public LocalDate getDateOfBirthUser() { return dateOfBirthUser; }
    public void setDateOfBirthUser(LocalDate dateOfBirthUser) { this.dateOfBirthUser = dateOfBirthUser; }

    public UserType getUserType() { return userType; }
    public void setUserType(UserType userType) { this.userType = userType; }

    public AddressUser getAddressUser() { return addressUser; }
    public void setAddressUser(AddressUser addressUser) { this.addressUser = addressUser; }

    public String getPhoneNumberUser() { return phoneNumberUser; }
    public void setPhoneNumberUser(String phoneNumberUser) { this.phoneNumberUser = phoneNumberUser; }

    public String getEmailUser() { return emailUser; }
    public void setEmailUser(String emailUser) { this.emailUser = emailUser; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Set<EmergencyContact> getEmergencyContacts() {
        return emergencyContacts;
    }

    public void setEmergencyContacts(Set<EmergencyContact> emergencyContacts) {
        this.emergencyContacts = emergencyContacts;
    }

    public MedicalProfile getMedicalProfile() {
        return medicalProfile;
    }

    public void setMedicalProfile(MedicalProfile medicalProfile) {
        this.medicalProfile = medicalProfile;
    }

    public String getResetPasswordToken() {
        return resetPasswordToken;
    }

    public void setResetPasswordToken(String resetPasswordToken) {
        this.resetPasswordToken = resetPasswordToken;
    }

    public LocalDateTime getResetPasswordTokenExpiry() {
        return resetPasswordTokenExpiry;
    }

    public void setResetPasswordTokenExpiry(LocalDateTime resetPasswordTokenExpiry) {
        this.resetPasswordTokenExpiry = resetPasswordTokenExpiry;
    }

    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    public void setAccountNonExpired(boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    public void setCredentialsNonExpired(boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
