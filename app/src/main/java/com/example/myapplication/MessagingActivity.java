package com.example.myapplication;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MessagingActivity extends AppCompatActivity {
    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private Button sendButton;
    private Button createAppointmentButton;
    private MessageAdapter messageAdapter;
    private DatabaseHelper dbHelper;
    private String currentUserId;
    private String currentUserName;
    private String receiverId;
    private String receiverName;
    private String selectedDate = "";
    private String selectedTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        // Get user information from intent
        currentUserId = getIntent().getStringExtra("currentUserId");
        currentUserName = getIntent().getStringExtra("currentUserName");
        receiverId = getIntent().getStringExtra("receiverId");
        receiverName = getIntent().getStringExtra("receiverName");

        // Initialize views
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        createAppointmentButton = findViewById(R.id.createAppointmentButton);

        // Show create appointment button only for admin
        if (currentUserId.equals("ADMIN")) {
            createAppointmentButton.setVisibility(View.VISIBLE);
            createAppointmentButton.setOnClickListener(v -> showCreateAppointmentDialog());
        }

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Setup RecyclerView
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessageAdapter(dbHelper.getMessagesBetweenUsers(currentUserId, receiverId), currentUserId);
        messagesRecyclerView.setAdapter(messageAdapter);

        // Send button click listener
        sendButton.setOnClickListener(v -> {
            String messageText = messageInput.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendMessage(messageText);
                messageInput.setText("");
            }
        });
    }

    private void showCreateAppointmentDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_create_appointment);
        dialog.setCancelable(true);

        Spinner doctorSpinner = dialog.findViewById(R.id.doctorSpinner);
        Button dateButton = dialog.findViewById(R.id.dateButton);
        Button timeButton = dialog.findViewById(R.id.timeButton);
        Button cancelButton = dialog.findViewById(R.id.cancelButton);
        Button createButton = dialog.findViewById(R.id.createButton);

        // Setup doctor spinner
        List<String> doctors = new ArrayList<>();
        doctors.add("D001 - Dr. Smith");
        doctors.add("D002 - Dr. Johnson");
        doctors.add("D003 - Dr. Williams");
        ArrayAdapter<String> doctorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, doctors);
        doctorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        doctorSpinner.setAdapter(doctorAdapter);

        // Date picker
        dateButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    dateButton.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePicker.show();
        });

        // Time picker
        timeButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            TimePickerDialog timePicker = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    timeButton.setText(selectedTime);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            );
            timePicker.show();
        });

        // Cancel button
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        // Create button
        createButton.setOnClickListener(v -> {
            if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
                Toast.makeText(this, "Please select both date and time", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedDoctor = (String) doctorSpinner.getSelectedItem();
            String[] doctorInfo = selectedDoctor.split(" - ");
            String doctorId = doctorInfo[0];
            String doctorName = doctorInfo[1];

            createAndExportAppointment(doctorId, doctorName);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void createAndExportAppointment(String doctorId, String doctorName) {
        // Create appointment (ID will be generated by DatabaseHelper)
        Appointment appointment = new Appointment(
            "", // Empty ID - will be generated by DatabaseHelper
            receiverId, // patient ID
            receiverName, // patient name
            doctorId,
            doctorName,
            selectedDate,
            selectedTime
        );

        // Add to database
        dbHelper.addAppointment(appointment);

        // Export to CSV
        exportAppointmentToCSV(appointment);

        Toast.makeText(this, "Appointment created and exported", Toast.LENGTH_SHORT).show();
    }

    private void exportAppointmentToCSV(Appointment appointment) {
        try {
            File file = new File(getExternalFilesDir(null), "appointmentDoing.csv");
            boolean fileExists = file.exists();

            // Use UTF-8 encoding and proper line endings
            FileWriter writer = new FileWriter(file, true);

            // Write header if file is new
            if (!fileExists) {
                writer.write("appointment_id,patient_id,patient_name,doctor_id,doctor_name,date,time\r\n");
            }

            // Write appointment data with proper escaping
            writer.write(String.format("%s,%s,%s,%s,%s,%s,%s\r\n",
                escapeCsvField(appointment.getAppointmentId()),
                escapeCsvField(appointment.getPatientId()),
                escapeCsvField(appointment.getPatientName()),
                escapeCsvField(appointment.getDoctorId()),
                escapeCsvField(appointment.getDoctorName()),
                escapeCsvField(appointment.getDate()),
                escapeCsvField(appointment.getTime())
            ));

            writer.flush();
            writer.close();

            // Notify the system about the new file
            MediaScannerConnection.scanFile(MessagingActivity.this, 
                new String[] { file.getAbsolutePath() }, 
                null, 
                null);

            Log.d("MessagingActivity", "Appointment exported to: " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e("MessagingActivity", "Error exporting appointment", e);
            Toast.makeText(this, "Error exporting appointment", Toast.LENGTH_SHORT).show();
        }
    }

    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        // If the field contains comma, newline, or double quote, wrap it in quotes
        if (field.contains(",") || field.contains("\n") || field.contains("\r") || field.contains("\"")) {
            // Replace any double quotes with two double quotes
            field = field.replace("\"", "\"\"");
            // Wrap the field in double quotes
            field = "\"" + field + "\"";
        }
        return field;
    }

    private void sendMessage(String messageText) {
        // Generate unique message ID
        String messageId = UUID.randomUUID().toString();
        
        // Get current timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String timestamp = sdf.format(new Date());

        // Create and save message
        Message message = new Message(
            messageId,
            currentUserId,
            currentUserName,
            currentUserId.equals("ADMIN") ? receiverId : "ADMIN",
            messageText,
            timestamp
        );

        Log.d("MessagingActivity", "Sending message - Sender: " + currentUserId + 
              ", Receiver: " + message.getReceiverId() + ", Text: " + messageText);

        dbHelper.addMessage(message);
        
        // Update RecyclerView
        List<Message> updatedMessages = dbHelper.getMessagesBetweenUsers(currentUserId, receiverId);
        Log.d("MessagingActivity", "Retrieved " + updatedMessages.size() + " messages after sending");
        messageAdapter.updateMessages(updatedMessages);
        messagesRecyclerView.scrollToPosition(updatedMessages.size() - 1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh messages when activity resumes
        List<Message> updatedMessages = dbHelper.getMessagesBetweenUsers(currentUserId, receiverId);
        messageAdapter.updateMessages(updatedMessages);
        messagesRecyclerView.scrollToPosition(updatedMessages.size() - 1);
    }
} 