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

public class DoctorLoginActivity extends AppCompatActivity {
    private static final String TAG = "DoctorLoginActivity";
    private TextInputEditText doctorIdInput;
    private Button loginButton;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_login);

        databaseHelper = new DatabaseHelper(this);
        doctorIdInput = findViewById(R.id.doctorIdInput);
        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String doctorId = doctorIdInput.getText().toString().trim();
                Log.d(TAG, "Login attempted with doctor ID: " + doctorId);
                
                if (!doctorId.isEmpty()) {
                    // Check if doctor exists in appointments
                    String doctorName = findDoctorName(doctorId);
                    if (doctorName != null) {
                        Log.d(TAG, "Doctor found - ID: " + doctorId + ", Name: " + doctorName);
                        // Doctor found, proceed to dashboard
                        Intent intent = new Intent(DoctorLoginActivity.this, DoctorActivity.class);
                        intent.putExtra("DOCTOR_ID", doctorId);
                        intent.putExtra("DOCTOR_NAME", doctorName);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(DoctorLoginActivity.this, "Invalid Doctor ID", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DoctorLoginActivity.this, "Please enter Doctor ID", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String findDoctorName(String doctorId) {
        List<Appointment> appointments = databaseHelper.getAllAppointments();
        Log.d(TAG, "Total appointments in database: " + appointments.size());
        
        if (appointments.isEmpty()) {
            Toast.makeText(this, "No appointments found in database", Toast.LENGTH_LONG).show();
            return null;
        }
        
        for (Appointment appointment : appointments) {
            Log.d(TAG, "Checking appointment - Doctor ID: " + appointment.getDoctorId() + 
                  ", Doctor Name: " + appointment.getDoctorName());
            if (appointment.getDoctorId().equals(doctorId)) {
                return appointment.getDoctorName();
            }
        }
        
        Toast.makeText(this, "No appointments found for doctor ID: " + doctorId, Toast.LENGTH_LONG).show();
        return null;
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }
} 