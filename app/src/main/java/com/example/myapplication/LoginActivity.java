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

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private TextInputEditText usernameInput;
    private TextInputEditText passwordInput;
    private Button loginButton;
    private AdminCredentialsHelper adminCredentialsHelper;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        adminCredentialsHelper = new AdminCredentialsHelper(this);
        databaseHelper = new DatabaseHelper(this);

        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();

                Log.d(TAG, "Login attempt - Username: " + username + ", Password: " + password);

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if it's admin login
                if (adminCredentialsHelper.checkAdminCredentials(username, password)) {
                    Log.d(TAG, "Admin login successful");
                    startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                    finish();
                    return;
                }

                // Check if it's a patient ID
                String patientName = findPatientName(username);
                if (patientName != null) {
                    Log.d(TAG, "Patient login successful - ID: " + username + ", Name: " + patientName);
                    Intent intent = new Intent(LoginActivity.this, PatientActivity.class);
                    intent.putExtra("PATIENT_ID", username);
                    intent.putExtra("PATIENT_NAME", patientName);
                    startActivity(intent);
                    finish();
                    return;
                }

                // Check if it's a doctor ID
                String doctorName = findDoctorName(username);
                if (doctorName != null) {
                    Log.d(TAG, "Doctor login successful - ID: " + username + ", Name: " + doctorName);
                    Intent intent = new Intent(LoginActivity.this, DoctorActivity.class);
                    intent.putExtra("DOCTOR_ID", username);
                    intent.putExtra("DOCTOR_NAME", doctorName);
                    startActivity(intent);
                    finish();
                    return;
                }

                // If none of the above, invalid credentials
                Log.d(TAG, "Login failed - No matching user found");
                Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String findPatientName(String patientId) {
        Log.d(TAG, "Searching for patient with ID: " + patientId);
        List<Appointment> appointments = databaseHelper.getAllAppointments();
        Log.d(TAG, "Total appointments in database: " + appointments.size());
        
        for (Appointment appointment : appointments) {
            Log.d(TAG, "Checking appointment - Patient ID: " + appointment.getPatientId() + 
                  ", Patient Name: " + appointment.getPatientName());
            if (appointment.getPatientId().equals(patientId)) {
                Log.d(TAG, "Found matching patient: " + appointment.getPatientName());
                return appointment.getPatientName();
            }
        }
        
        Log.d(TAG, "No patient found with ID: " + patientId);
        return null;
    }

    private String findDoctorName(String doctorId) {
        Log.d(TAG, "Searching for doctor with ID: " + doctorId);
        List<Appointment> appointments = databaseHelper.getAllAppointments();
        Log.d(TAG, "Total appointments in database: " + appointments.size());
        
        for (Appointment appointment : appointments) {
            Log.d(TAG, "Checking appointment - Doctor ID: " + appointment.getDoctorId() + 
                  ", Doctor Name: " + appointment.getDoctorName());
            if (appointment.getDoctorId().equals(doctorId)) {
                Log.d(TAG, "Found matching doctor: " + appointment.getDoctorName());
                return appointment.getDoctorName();
            }
        }
        
        Log.d(TAG, "No doctor found with ID: " + doctorId);
        return null;
    }

    @Override
    protected void onDestroy() {
        adminCredentialsHelper.close();
        databaseHelper.close();
        super.onDestroy();
    }
} 