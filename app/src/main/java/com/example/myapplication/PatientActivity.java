package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PatientActivity extends AppCompatActivity {
    private static final String TAG = "PatientActivity";
    private RecyclerView recyclerView;
    private AppointmentAdapter appointmentAdapter;
    private DatabaseHelper dbHelper;
    private String patientId;
    private String patientName;
    private Button messageAdminButton;
    private TextView patientNameText;
    private TextView patientIdText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        // Get patient information from intent
        patientId = getIntent().getStringExtra("PATIENT_ID");
        patientName = getIntent().getStringExtra("PATIENT_NAME");

        Log.d(TAG, "Received patient info - ID: " + patientId + ", Name: " + patientName);

        // Initialize views
        patientNameText = findViewById(R.id.patientName);
        patientIdText = findViewById(R.id.patientId);
        messageAdminButton = findViewById(R.id.messageAdminButton);
        
        // Set patient information
        patientNameText.setText("Name: " + patientName);
        patientIdText.setText("ID: " + patientId);
        
        messageAdminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMessagingActivity("ADMIN", "Admin");
            }
        });

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Load appointments
        loadPatientAppointments();
    }

    private void loadPatientAppointments() {
        List<Appointment> appointments = dbHelper.getAllAppointments();
        List<Appointment> patientAppointments = new ArrayList<>();
        
        Log.d(TAG, "Total appointments in database: " + appointments.size());
        
        for (Appointment appointment : appointments) {
            Log.d(TAG, "Checking appointment - Patient ID: " + appointment.getPatientId() + 
                  ", Patient Name: " + appointment.getPatientName());
            if (appointment.getPatientId().equals(patientId)) {
                Log.d(TAG, "Found matching appointment for patient");
                patientAppointments.add(appointment);
            }
        }
        
        Log.d(TAG, "Found " + patientAppointments.size() + " appointments for patient " + patientId);
        appointmentAdapter = new AppointmentAdapter(patientAppointments, patientId, "patient", dbHelper);
        recyclerView.setAdapter(appointmentAdapter);
    }

    private void startMessagingActivity(String receiverId, String receiverName) {
        Intent intent = new Intent(this, MessagingActivity.class);
        intent.putExtra("currentUserId", patientId);
        intent.putExtra("currentUserName", patientName);
        intent.putExtra("receiverId", "ADMIN");
        intent.putExtra("receiverName", "Admin");
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPatientAppointments();
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
} 