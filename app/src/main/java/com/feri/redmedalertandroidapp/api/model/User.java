package com.feri.redmedalertandroidapp.api.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
}
