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

    private TextInputEditText firstNameInput;
    private TextInputEditText lastNameInput;
    private TextInputEditText relationshipInput;
    private TextInputEditText phoneInput;
    private TextInputEditText emailInput;
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
        firstNameInput = view.findViewById(R.id.firstNameInput);
        lastNameInput = view.findViewById(R.id.lastNameInput);
        relationshipInput = view.findViewById(R.id.relationshipInput);
        phoneInput = view.findViewById(R.id.phoneInput);
        emailInput = view.findViewById(R.id.emailInput);
    }

    private void populateFields() {
        firstNameInput.setText(existingContact.getFirstNameContact());
        lastNameInput.setText(existingContact.getLastNameContact());
        relationshipInput.setText(existingContact.getRelationship());
        phoneInput.setText(existingContact.getPhoneNumberContact());
        emailInput.setText(existingContact.getEmailContact());
    }

    private void saveContact() {
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String relationship = relationshipInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || relationship.isEmpty() || phone.isEmpty()) {
            return;
        }

        EmergencyContact contact = new EmergencyContact();
        if (existingContact != null) {
            contact.setIdContact(existingContact.getIdContact());
            contact.setUser(existingContact.getUser());
            contact.setAddressContact(existingContact.getAddressContact());
        } else {
            contact.setAddressContact(new AddressContact());
        }

        contact.setFirstNameContact(firstName);
        contact.setLastNameContact(lastName);
        contact.setRelationship(relationship);
        contact.setPhoneNumberContact(phone);
        contact.setEmailContact(email);

        listener.onContactSaved(contact);
    }
}
