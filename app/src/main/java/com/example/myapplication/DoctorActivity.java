package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class DoctorActivity extends AppCompatActivity {
    private static final String TAG = "DoctorActivity";
    private RecyclerView recyclerView;
    private AppointmentAdapter adapter;
    private TextView doctorNameText;
    private TextView doctorIdText;
    private DatabaseHelper databaseHelper;
    private String currentDoctorId;
    private String currentDoctorName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor);

        // Get doctor information from intent
        Intent intent = getIntent();
        currentDoctorId = intent.getStringExtra("DOCTOR_ID");
        currentDoctorName = intent.getStringExtra("DOCTOR_NAME");
        
        Log.d(TAG, "Received doctor info - ID: " + currentDoctorId + ", Name: " + currentDoctorName);

        // Initialize database
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        doctorNameText = findViewById(R.id.doctorName);
        doctorIdText = findViewById(R.id.doctorId);

        // Set doctor information
        doctorNameText.setText("Name: " + currentDoctorName);
        doctorIdText.setText("ID: " + currentDoctorId);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppointmentAdapter(new ArrayList<>(), currentDoctorId, "doctor", databaseHelper);
        recyclerView.setAdapter(adapter);

        // Load doctor's appointments
        loadDoctorAppointments();
    }

    private void loadDoctorAppointments() {
        List<Appointment> allAppointments = databaseHelper.getAllAppointments();
        Log.d(TAG, "Total appointments in database: " + allAppointments.size());
        
        List<Appointment> doctorAppointments = new ArrayList<>();
        for (Appointment appointment : allAppointments) {
            Log.d(TAG, "Checking appointment - Doctor ID: " + appointment.getDoctorId() + 
                  ", Doctor Name: " + appointment.getDoctorName());
            if (appointment.getDoctorId().equals(currentDoctorId)) {
                Log.d(TAG, "Found matching appointment for doctor");
                doctorAppointments.add(appointment);
            }
        }
        
        Log.d(TAG, "Found " + doctorAppointments.size() + " appointments for doctor " + currentDoctorId);
        adapter.updateAppointments(doctorAppointments);
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }
} 