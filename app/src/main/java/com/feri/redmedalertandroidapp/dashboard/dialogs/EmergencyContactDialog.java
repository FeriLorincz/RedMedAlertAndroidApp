package com.feri.redmedalertandroidapp.dashboard.dialogs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import com.feri.redmedalertandroidapp.R;
import com.feri.redmedalertandroidapp.api.model.EmergencyContact;
import com.feri.redmedalertandroidapp.api.model.AddressContact;
import com.google.android.material.textfield.TextInputEditText;

public class EmergencyContactDialog {

    private final Context context;
    private final OnContactSaveListener listener;

    // Contact fields
    private TextInputEditText firstNameInput;
    private TextInputEditText lastNameInput;
    private TextInputEditText relationshipInput;
    private TextInputEditText phoneInput;
    private TextInputEditText emailInput;

    // Address fields
    private TextInputEditText cityInput;
    private TextInputEditText villageInput;
    private TextInputEditText streetInput;
    private TextInputEditText numberInput;
    private TextInputEditText buildingInput;
    private TextInputEditText staircaseInput;
    private TextInputEditText floorInput;
    private TextInputEditText apartmentInput;
    private TextInputEditText postalCodeInput;
    private TextInputEditText countyInput;
    private TextInputEditText stateInput;
    private TextInputEditText countryInput;

    private EmergencyContact existingContact;

    public interface OnContactSaveListener {
        void onContactSaved(EmergencyContact contact);
    }

    public EmergencyContactDialog(Context context, OnContactSaveListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void show(EmergencyContact contact) {
        this.existingContact = contact;

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_emergency_contact, null);
        initializeViews(dialogView);

        if (existingContact != null) {
            populateFields();
        }

        new AlertDialog.Builder(context)
                .setTitle(existingContact != null ? "Editare contact" : "Adăugare contact")
                .setView(dialogView)
                .setPositiveButton("Salvează", (dialog, which) -> saveContact())
                .setNegativeButton("Anulează", null)
                .show();
    }

    private void initializeViews(View view) {
        // Contact fields
        firstNameInput = view.findViewById(R.id.firstNameInput);
        lastNameInput = view.findViewById(R.id.lastNameInput);
        relationshipInput = view.findViewById(R.id.relationshipInput);
        phoneInput = view.findViewById(R.id.phoneInput);
        emailInput = view.findViewById(R.id.emailInput);

        // Address fields
        cityInput = view.findViewById(R.id.cityInput);
        villageInput = view.findViewById(R.id.villageInput);
        streetInput = view.findViewById(R.id.streetInput);
        numberInput = view.findViewById(R.id.numberInput);
        buildingInput = view.findViewById(R.id.buildingInput);
        staircaseInput = view.findViewById(R.id.staircaseInput);
        floorInput = view.findViewById(R.id.floorInput);
        apartmentInput = view.findViewById(R.id.apartmentInput);
        postalCodeInput = view.findViewById(R.id.postalCodeInput);
        countyInput = view.findViewById(R.id.countyInput);
        stateInput = view.findViewById(R.id.stateInput);
        countryInput = view.findViewById(R.id.countryInput);
    }

    private void populateFields() {
        // Contact info
        firstNameInput.setText(existingContact.getFirstNameContact());
        lastNameInput.setText(existingContact.getLastNameContact());
        relationshipInput.setText(existingContact.getRelationship());
        phoneInput.setText(existingContact.getPhoneNumberContact());
        emailInput.setText(existingContact.getEmailContact());

        // Address info
        AddressContact address = existingContact.getAddressContact();
        if (address != null) {
            cityInput.setText(address.getCityContact());
            villageInput.setText(address.getVillageContact());
            streetInput.setText(address.getStreetContact());
            numberInput.setText(address.getNumberContact());
            buildingInput.setText(address.getBuildingContact());
            staircaseInput.setText(address.getStaircaseContact());
            floorInput.setText(address.getFloorContact());
            apartmentInput.setText(address.getApartmentContact());
            postalCodeInput.setText(address.getPostalCodeContact());
            countyInput.setText(address.getCountyContact());
            stateInput.setText(address.getStateContact());
            countryInput.setText(address.getCountryContact());
        }
    }

    private void saveContact() {
        if (!validateFields()) {
            return;
        }

        EmergencyContact contact = new EmergencyContact();
        if (existingContact != null) {
            contact.setIdContact(existingContact.getIdContact());
            contact.setUser(existingContact.getUser());
        }

        // Set contact info
        contact.setFirstNameContact(getTextFromInput(firstNameInput));
        contact.setLastNameContact(getTextFromInput(lastNameInput));
        contact.setRelationship(getTextFromInput(relationshipInput));
        contact.setPhoneNumberContact(getTextFromInput(phoneInput));
        contact.setEmailContact(getTextFromInput(emailInput));

        // Set address info
        AddressContact address = new AddressContact();
        address.setCityContact(getTextFromInput(cityInput));
        address.setVillageContact(getTextFromInput(villageInput));
        address.setStreetContact(getTextFromInput(streetInput));
        address.setNumberContact(getTextFromInput(numberInput));
        address.setBuildingContact(getTextFromInput(buildingInput));
        address.setStaircaseContact(getTextFromInput(staircaseInput));
        address.setFloorContact(getTextFromInput(floorInput));
        address.setApartmentContact(getTextFromInput(apartmentInput));
        address.setPostalCodeContact(getTextFromInput(postalCodeInput));
        address.setCountyContact(getTextFromInput(countyInput));
        address.setStateContact(getTextFromInput(stateInput));
        address.setCountryContact(getTextFromInput(countryInput));

        contact.setAddressContact(address);
        listener.onContactSaved(contact);
    }

    private boolean validateFields() {
        String firstName = getTextFromInput(firstNameInput);
        String lastName = getTextFromInput(lastNameInput);
        String relationship = getTextFromInput(relationshipInput);
        String phone = getTextFromInput(phoneInput);

        return !firstName.isEmpty() &&
                !lastName.isEmpty() &&
                !relationship.isEmpty() &&
                !phone.isEmpty();
    }

    private String getTextFromInput(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }
}