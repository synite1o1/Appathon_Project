package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {
    private static final String TAG = "AdminActivity";
    private RecyclerView recyclerView;
    private AppointmentAdapter adapter;
    private EditText searchInput;
    private List<Appointment> allAppointments = new ArrayList<>();
    private List<Appointment> displayedAppointments = new ArrayList<>();
    private DatabaseHelper databaseHelper;
    private final ActivityResultLauncher<String> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    readCSVFile(uri);
                }
            });
    private Button viewMessagesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        databaseHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.appointmentsRecyclerView);
        searchInput = findViewById(R.id.searchInput);
        Button uploadButton = findViewById(R.id.uploadCsvButton);
        Button searchButton = findViewById(R.id.searchButton);
        Button viewAllButton = findViewById(R.id.viewAllButton);
        viewMessagesButton = findViewById(R.id.viewMessagesButton);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppointmentAdapter(displayedAppointments, "admin", "admin", databaseHelper);
        recyclerView.setAdapter(adapter);

        // Initially clear the displayed appointments
        displayedAppointments.clear();
        adapter.notifyDataSetChanged();

        // Setup view all button
        viewAllButton.setOnClickListener(v -> {
            loadAllAppointments();
            Toast.makeText(this, "Showing all appointments", Toast.LENGTH_SHORT).show();
        });

        // Setup search button
        searchButton.setOnClickListener(v -> {
            String searchId = searchInput.getText().toString().trim();
            if (searchId.isEmpty()) {
                Toast.makeText(this, "Please enter an appointment ID", Toast.LENGTH_SHORT).show();
            } else {
                searchAppointment(searchId);
            }
        });

        // Setup upload button
        uploadButton.setOnClickListener(v -> filePickerLauncher.launch("text/*"));

        viewMessagesButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminMessagesActivity.class);
            startActivity(intent);
        });
    }

    private void loadAllAppointments() {
        allAppointments.clear();
        allAppointments.addAll(databaseHelper.getAllAppointments());
        displayedAppointments.clear();
        displayedAppointments.addAll(allAppointments);
        adapter.notifyDataSetChanged();
        Log.d(TAG, "Loaded " + allAppointments.size() + " appointments");
    }

    private void searchAppointment(String appointmentId) {
        displayedAppointments.clear();
        for (Appointment appointment : allAppointments) {
            if (appointment.getAppointmentId().equals(appointmentId)) {
                displayedAppointments.add(appointment);
                break;
            }
        }
        adapter.notifyDataSetChanged();
        
        if (displayedAppointments.isEmpty()) {
            Toast.makeText(this, "No appointment found with ID: " + appointmentId, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Found appointment", Toast.LENGTH_SHORT).show();
        }
    }

    private void readCSVFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                allAppointments.clear();
                databaseHelper.clearAllAppointments(); // Clear existing data

                // Skip header line
                String header = reader.readLine();
                Log.d(TAG, "CSV Header: " + header);

                int count = 0;
                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(",");
                    if (values.length >= 7) {
                        Appointment appointment = new Appointment(
                                values[0].trim(), // appointment_id
                                values[1].trim(), // patient_id
                                values[2].trim(), // patient_name
                                values[3].trim(), // doctor_id
                                values[4].trim(), // doctor_name
                                values[5].trim(), // date
                                values[6].trim()  // time
                        );
                        allAppointments.add(appointment);
                        databaseHelper.addAppointment(appointment);
                        count++;
                    } else {
                        Log.e(TAG, "Invalid row format: " + line);
                        Toast.makeText(this, "Invalid row format: " + line, Toast.LENGTH_LONG).show();
                    }
                }
                reader.close();
                
                Log.d(TAG, "Loaded " + count + " appointments successfully");
                Toast.makeText(this, "Loaded " + count + " appointments successfully. Click 'View All' to see them.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading file: " + e.getMessage());
            Toast.makeText(this, "Error reading file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }
} 