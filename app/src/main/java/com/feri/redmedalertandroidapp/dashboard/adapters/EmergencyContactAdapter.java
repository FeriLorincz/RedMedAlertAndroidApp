package com.feri.redmedalertandroidapp.dashboard.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.feri.redmedalertandroidapp.R;
import com.feri.redmedalertandroidapp.api.model.EmergencyContact;

import java.util.ArrayList;
import java.util.List;

public class EmergencyContactAdapter extends RecyclerView.Adapter<EmergencyContactAdapter.EmergencyContactViewHolder>{

    private List<EmergencyContact> contacts = new ArrayList<>();
    private final OnContactClickListener listener;

    public interface OnContactClickListener {
        void onContactEdit(EmergencyContact contact);
        void onContactDelete(EmergencyContact contact);
    }

    public EmergencyContactAdapter(OnContactClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public EmergencyContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emergency_contact, parent, false);
        return new EmergencyContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmergencyContactViewHolder holder, int position) {
        holder.bind(contacts.get(position));
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public void setContacts(List<EmergencyContact> contacts) {
        this.contacts = contacts;
        notifyDataSetChanged();
    }

    class EmergencyContactViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView relationshipText;
        private final TextView phoneText;
        private final ImageButton editButton;
        private final ImageButton deleteButton;

        EmergencyContactViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.contactName);
            relationshipText = itemView.findViewById(R.id.contactRelationship);
            phoneText = itemView.findViewById(R.id.contactPhone);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        void bind(EmergencyContact contact) {
            String fullName = contact.getFirstNameContact() + " " + contact.getLastNameContact();
            nameText.setText(fullName);
            relationshipText.setText(contact.getRelationship());
            phoneText.setText(contact.getPhoneNumberContact());

            editButton.setOnClickListener(v -> listener.onContactEdit(contact));
            deleteButton.setOnClickListener(v -> listener.onContactDelete(contact));
        }
    }
}
