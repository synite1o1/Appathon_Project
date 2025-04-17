package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class PatientLoginActivity extends AppCompatActivity {
    private static final String TAG = "PatientLoginActivity";
    private TextInputEditText patientIdInput;
    private Button loginButton;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_login);

        databaseHelper = new DatabaseHelper(this);
        patientIdInput = findViewById(R.id.patientIdInput);
        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String patientId = patientIdInput.getText().toString().trim();
                Log.d(TAG, "Login attempted with patient ID: " + patientId);
                
                if (!patientId.isEmpty()) {
                    // Check if patient exists in appointments
                    String patientName = findPatientName(patientId);
                    if (patientName != null) {
                        Log.d(TAG, "Patient found - ID: " + patientId + ", Name: " + patientName);
                        // Patient found, proceed to dashboard
                        Intent intent = new Intent(PatientLoginActivity.this, PatientActivity.class);
                        intent.putExtra("PATIENT_ID", patientId);
                        intent.putExtra("PATIENT_NAME", patientName);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(PatientLoginActivity.this, "Invalid Patient ID", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PatientLoginActivity.this, "Please enter Patient ID", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String findPatientName(String patientId) {
        List<Appointment> appointments = databaseHelper.getAllAppointments();
        Log.d(TAG, "Total appointments in database: " + appointments.size());
        
        if (appointments.isEmpty()) {
            Toast.makeText(this, "No appointments found in database", Toast.LENGTH_LONG).show();
            return null;
        }
        
        for (Appointment appointment : appointments) {
            Log.d(TAG, "Checking appointment - Patient ID: " + appointment.getPatientId() + 
                  ", Patient Name: " + appointment.getPatientName());
            if (appointment.getPatientId().equals(patientId)) {
                return appointment.getPatientName();
            }
        }
        
        Toast.makeText(this, "No appointments found for patient ID: " + patientId, Toast.LENGTH_LONG).show();
        return null;
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }
} 