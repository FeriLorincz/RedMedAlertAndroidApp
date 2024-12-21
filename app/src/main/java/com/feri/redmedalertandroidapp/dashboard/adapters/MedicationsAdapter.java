package com.feri.redmedalertandroidapp.dashboard.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.feri.redmedalertandroidapp.R;
import com.feri.redmedalertandroidapp.api.model.Medication;

import java.util.ArrayList;
import java.util.List;
import java.time.format.DateTimeFormatter;

public class MedicationsAdapter extends RecyclerView.Adapter<MedicationsAdapter.MedicationViewHolder> {

    private List<Medication> medications = new ArrayList<>();
    private final OnMedicationClickListener listener;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public interface OnMedicationClickListener {
        void onMedicationEdit(Medication medication);
        void onMedicationDelete(Medication medication);
    }

    public MedicationsAdapter(OnMedicationClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MedicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medication, parent, false);
        return new MedicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicationViewHolder holder, int position) {
        holder.bind(medications.get(position));
    }

    @Override
    public int getItemCount() {
        return medications.size();
    }

    public void setMedications(List<Medication> medications) {
        this.medications = medications;
        notifyDataSetChanged();
    }

    class MedicationViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView dosageText;
        private final TextView startDateText;
        private final ImageButton editButton;
        private final ImageButton deleteButton;

        MedicationViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.medicationName);
            dosageText = itemView.findViewById(R.id.medicationDosage);
            startDateText = itemView.findViewById(R.id.medicationStartDate);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        void bind(Medication medication) {
            nameText.setText(medication.getMedicationName());
            dosageText.setText(medication.getMedicationDosage());
            startDateText.setText(medication.getMedicationStartDate().format(dateFormatter));

            editButton.setOnClickListener(v -> listener.onMedicationEdit(medication));
            deleteButton.setOnClickListener(v -> listener.onMedicationDelete(medication));
        }
    }
}
