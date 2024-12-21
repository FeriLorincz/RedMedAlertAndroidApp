package com.feri.redmedalertandroidapp.api.model;

public class EmergencyContact {

    private String idContact;
    private User user;
    private AddressContact addressContact;
    private String firstNameContact;
    private String lastNameContact;
    private String relationship;
    private String phoneNumberContact;
    private String emailContact;

    // Getters and Setters
    public String getIdContact() {
        return idContact;
    }

    public void setIdContact(String idContact) {
        this.idContact = idContact;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public AddressContact getAddressContact() {
        return addressContact;
    }

    public void setAddressContact(AddressContact addressContact) {
        this.addressContact = addressContact;
    }

    public String getFirstNameContact() {
        return firstNameContact;
    }

    public void setFirstNameContact(String firstNameContact) {
        this.firstNameContact = firstNameContact;
    }

    public String getLastNameContact() {
        return lastNameContact;
    }

    public void setLastNameContact(String lastNameContact) {
        this.lastNameContact = lastNameContact;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getPhoneNumberContact() {
        return phoneNumberContact;
    }

    public void setPhoneNumberContact(String phoneNumberContact) {
        this.phoneNumberContact = phoneNumberContact;
    }

    public String getEmailContact() {
        return emailContact;
    }

    public void setEmailContact(String emailContact) {
        this.emailContact = emailContact;
    }
}