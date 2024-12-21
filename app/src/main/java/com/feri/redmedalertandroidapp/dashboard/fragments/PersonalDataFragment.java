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
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class PersonalDataFragment extends Fragment{

    private UserViewModel userViewModel;
    private TextInputEditText firstNameInput;
    private TextInputEditText lastNameInput;
    private TextInputEditText phoneInput;
    private TextInputEditText emailInput;
    private TextInputEditText dateOfBirthInput;
    private TextInputEditText streetInput;
    private Button saveButton;
    private User currentUser;

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
        dateOfBirthInput = view.findViewById(R.id.dateOfBirthInput);
        streetInput = view.findViewById(R.id.streetInput);
        saveButton = view.findViewById(R.id.saveButton);

        // Setup date picker for date of birth
        setupDatePicker();
    }

    private void setupDatePicker() {
        dateOfBirthInput.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date of birth")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                LocalDate selectedDate = Instant.ofEpochMilli(selection)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                dateOfBirthInput.setText(selectedDate.toString());
            });

            datePicker.show(getParentFragmentManager(), "DATE_PICKER");
        });
    }

    private void setupObservers() {
        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                currentUser = user;
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
        if (user.getDateOfBirthUser() != null) {
            dateOfBirthInput.setText(user.getDateOfBirthUser().toString());
        }

        AddressUser address = user.getAddressUser();
        if (address != null) {
            streetInput.setText(address.getStreetUser());
        }
    }

    private void saveUserData() {
        if (currentUser == null) {
            currentUser = new User();
        }

        currentUser.setFirstNameUser(firstNameInput.getText().toString());
        currentUser.setLastNameUser(lastNameInput.getText().toString());
        currentUser.setPhoneNumberUser(phoneInput.getText().toString());
        currentUser.setEmailUser(emailInput.getText().toString());

        String dateOfBirthStr = dateOfBirthInput.getText().toString();
        if (!dateOfBirthStr.isEmpty()) {
            currentUser.setDateOfBirthUser(LocalDate.parse(dateOfBirthStr));
        }

        AddressUser address = currentUser.getAddressUser();
        if (address == null) {
            address = new AddressUser();
        }
        address.setStreetUser(streetInput.getText().toString());
        currentUser.setAddressUser(address);

        userViewModel.updateUser(currentUser, new UserViewModel.UpdateCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(),
                        "Datele au fost salvate cu succes",
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
}