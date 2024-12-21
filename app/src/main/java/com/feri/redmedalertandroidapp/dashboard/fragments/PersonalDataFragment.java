package com.feri.redmedalertandroidapp.dashboard.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.feri.redmedalertandroidapp.R;
import com.feri.redmedalertandroidapp.api.model.AddressUser;
import com.feri.redmedalertandroidapp.api.model.User;
import com.feri.redmedalertandroidapp.viewmodel.UserViewModel;
import com.google.android.material.textfield.TextInputEditText;

public class PersonalDataFragment extends Fragment{

    private UserViewModel userViewModel;
    private TextInputEditText firstNameInput;
    private TextInputEditText lastNameInput;
    private TextInputEditText phoneInput;
    private TextInputEditText emailInput;
    private TextInputEditText streetInput;
    private Button saveButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_personal_data, container, false);
        initializeViews(view);
        setupObservers();
        setupListeners();
        return view;
    }

    private void initializeViews(View view) {
        firstNameInput = view.findViewById(R.id.firstNameInput);
        lastNameInput = view.findViewById(R.id.lastNameInput);
        phoneInput = view.findViewById(R.id.phoneInput);
        emailInput = view.findViewById(R.id.emailInput);
        streetInput = view.findViewById(R.id.streetInput);
        saveButton = view.findViewById(R.id.saveButton);
    }

    private void setupObservers() {
        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                populateFields(user);
            }
        });
    }

    private void setupListeners() {
        saveButton.setOnClickListener(v -> saveUserData());
    }

    private void populateFields(User user) {
        firstNameInput.setText(user.getFirstNameUser());
        lastNameInput.setText(user.getLastNameUser());
        phoneInput.setText(user.getPhoneNumberUser());
        emailInput.setText(user.getEmailUser());

        AddressUser address = user.getAddressUser();
        if (address != null) {
            streetInput.setText(address.getStreetUser());
            // Populate other address fields
        }
    }

    private void saveUserData() {
        User updatedUser = new User();
        updatedUser.setFirstNameUser(firstNameInput.getText().toString());
        updatedUser.setLastNameUser(lastNameInput.getText().toString());
        updatedUser.setPhoneNumberUser(phoneInput.getText().toString());
        updatedUser.setEmailUser(emailInput.getText().toString());

        AddressUser address = new AddressUser();
        address.setStreetUser(streetInput.getText().toString());
        // Set other address fields
        updatedUser.setAddressUser(address);

        userViewModel.updateUser(updatedUser, new UserViewModel.UpdateCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(), "Datele au fost salvate cu succes", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), "Eroare: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
