package com.feri.redmedalertandroidapp.dashboard.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.feri.redmedalertandroidapp.R;
import com.feri.redmedalertandroidapp.api.model.EmergencyContact;
import com.feri.redmedalertandroidapp.dashboard.adapters.EmergencyContactAdapter;
import com.feri.redmedalertandroidapp.dashboard.dialogs.EmergencyContactDialog;
import com.feri.redmedalertandroidapp.viewmodel.EmergencyContactViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class EmergencyContactsFragment extends Fragment implements EmergencyContactAdapter.OnContactClickListener{

    private EmergencyContactViewModel viewModel;
    private EmergencyContactAdapter adapter;
    private RecyclerView recyclerView;
    private FloatingActionButton addButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(EmergencyContactViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emergency_contacts, container, false);
        initializeViews(view);
        setupRecyclerView();
        setupObservers();
        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.contactsRecyclerView);
        addButton = view.findViewById(R.id.addContactButton);
        addButton.setOnClickListener(v -> showAddContactDialog());
    }

    private void setupRecyclerView() {
        adapter = new EmergencyContactAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getContacts().observe(getViewLifecycleOwner(), contacts -> {
            adapter.setContacts(contacts);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showAddContactDialog() {
        EmergencyContactDialog dialog = new EmergencyContactDialog(requireContext(),
                contact -> viewModel.createContact(contact, new EmergencyContactViewModel.UpdateCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(requireContext(), "Contact adăugat cu succes", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(requireContext(), "Eroare: " + message, Toast.LENGTH_LONG).show();
                    }
                }));
        dialog.show(null);
    }

    @Override
    public void onContactEdit(EmergencyContact contact) {
        EmergencyContactDialog dialog = new EmergencyContactDialog(requireContext(),
                updatedContact -> viewModel.updateContact(contact.getIdContact(), updatedContact,
                        new EmergencyContactViewModel.UpdateCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(requireContext(), "Contact actualizat cu succes", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(String message) {
                                Toast.makeText(requireContext(), "Eroare: " + message, Toast.LENGTH_LONG).show();
                            }
                        }));
        dialog.show(contact);
    }

    @Override
    public void onContactDelete(EmergencyContact contact) {
        viewModel.deleteContact(contact.getIdContact(), new EmergencyContactViewModel.UpdateCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(), "Contact șters cu succes", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), "Eroare: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
