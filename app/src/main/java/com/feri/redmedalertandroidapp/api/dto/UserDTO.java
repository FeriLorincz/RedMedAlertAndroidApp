package com.feri.redmedalertandroidapp.api.dto;

import com.feri.redmedalertandroidapp.api.model.UserType;
import java.time.LocalDate;

public class UserDTO {

    private String idUser;
    private String firstNameUser;
    private String lastNameUser;
    private LocalDate dateOfBirthUser;
    private String phoneNumberUser;
    private String emailUser;
    private String password;
    private UserType userType;
    private AddressUserDTO addressUser;

    // Constructor
    public UserDTO() {
    }

    // Getters and Setters

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getFirstNameUser() {
        return firstNameUser;
    }

    public void setFirstNameUser(String firstNameUser) {
        this.firstNameUser = firstNameUser;
    }

    public String getLastNameUser() {
        return lastNameUser;
    }

    public void setLastNameUser(String lastNameUser) {
        this.lastNameUser = lastNameUser;
    }

    public LocalDate getDateOfBirthUser() {
        return dateOfBirthUser;
    }

    public void setDateOfBirthUser(LocalDate dateOfBirthUser) {
        this.dateOfBirthUser = dateOfBirthUser;
    }

    public String getPhoneNumberUser() {
        return phoneNumberUser;
    }

    public void setPhoneNumberUser(String phoneNumberUser) {
        this.phoneNumberUser = phoneNumberUser;
    }

    public String getEmailUser() {
        return emailUser;
    }

    public void setEmailUser(String emailUser) {
        this.emailUser = emailUser;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public AddressUserDTO getAddressUser() {
        return addressUser;
    }

    public void setAddressUser(AddressUserDTO addressUser) {
        this.addressUser = addressUser;
    }
}
