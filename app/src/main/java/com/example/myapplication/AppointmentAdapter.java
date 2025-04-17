package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {
    private List<Appointment> appointments;
    private String currentUserId;
    private String userType;
    private DatabaseHelper dbHelper;

    public AppointmentAdapter(List<Appointment> appointments, String currentUserId, String userType, DatabaseHelper dbHelper) {
        this.appointments = appointments;
        this.currentUserId = currentUserId;
        this.userType = userType;
        this.dbHelper = dbHelper;
    }

    public void updateAppointments(List<Appointment> newAppointments) {
        this.appointments = newAppointments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.appointmentIdText.setText("Appointment ID: " + appointment.getAppointmentId());
        holder.patientNameText.setText("Patient: " + appointment.getPatientName());
        holder.doctorInfoText.setText("Doctor: " + appointment.getDoctorName());
        holder.dateText.setText("Date: " + appointment.getDate());
        holder.timeText.setText("Time: " + appointment.getTime());

        // Show/hide cancel button based on user type and permissions
        boolean canCancel = false;
        switch (userType) {
            case "admin":
                canCancel = true;
                break;
            case "doctor":
                canCancel = appointment.getDoctorId().equals(currentUserId);
                break;
            case "patient":
                canCancel = appointment.getPatientId().equals(currentUserId);
                break;
        }

        holder.cancelButton.setVisibility(canCancel ? View.VISIBLE : View.GONE);
        holder.cancelButton.setOnClickListener(v -> {
            if (dbHelper.deleteAppointment(appointment.getAppointmentId())) {
                appointments.remove(position);
                notifyItemRemoved(position);
                Toast.makeText(v.getContext(), "Appointment cancelled successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(v.getContext(), "Failed to cancel appointment", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView appointmentIdText;
        TextView patientNameText;
        TextView doctorInfoText;
        TextView dateText;
        TextView timeText;
        Button cancelButton;

        AppointmentViewHolder(View itemView) {
            super(itemView);
            appointmentIdText = itemView.findViewById(R.id.appointmentIdText);
            patientNameText = itemView.findViewById(R.id.patientNameText);
            doctorInfoText = itemView.findViewById(R.id.doctorInfoText);
            dateText = itemView.findViewById(R.id.dateText);
            timeText = itemView.findViewById(R.id.timeText);
            cancelButton = itemView.findViewById(R.id.cancelButton);
        }
    }
} 