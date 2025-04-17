package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "appointments.db";
    private static final int DATABASE_VERSION = 4;
    private static final String TABLE_APPOINTMENTS = "appointments";
    
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_APPOINTMENT_ID = "appointment_id";
    private static final String COLUMN_PATIENT_ID = "patient_id";
    private static final String COLUMN_PATIENT_NAME = "patient_name";
    private static final String COLUMN_DOCTOR_ID = "doctor_id";
    private static final String COLUMN_DOCTOR_NAME = "doctor_name";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_TIME = "time";

    // Messages table constants
    private static final String TABLE_MESSAGES = "messages";
    private static final String COLUMN_MESSAGE_ID = "message_id";
    private static final String COLUMN_SENDER_ID = "sender_id";
    private static final String COLUMN_SENDER_NAME = "sender_name";
    private static final String COLUMN_RECEIVER_ID = "receiver_id";
    private static final String COLUMN_MESSAGE_TEXT = "message_text";
    private static final String COLUMN_TIMESTAMP = "timestamp";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_APPOINTMENTS_TABLE = "CREATE TABLE " + TABLE_APPOINTMENTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_APPOINTMENT_ID + " TEXT,"
                + COLUMN_PATIENT_ID + " TEXT,"
                + COLUMN_PATIENT_NAME + " TEXT,"
                + COLUMN_DOCTOR_ID + " TEXT,"
                + COLUMN_DOCTOR_NAME + " TEXT,"
                + COLUMN_DATE + " TEXT,"
                + COLUMN_TIME + " TEXT"
                + ")";
        db.execSQL(CREATE_APPOINTMENTS_TABLE);

        String CREATE_MESSAGES_TABLE = "CREATE TABLE " + TABLE_MESSAGES + "("
                + COLUMN_MESSAGE_ID + " TEXT PRIMARY KEY,"
                + COLUMN_SENDER_ID + " TEXT,"
                + COLUMN_SENDER_NAME + " TEXT,"
                + COLUMN_RECEIVER_ID + " TEXT,"
                + COLUMN_MESSAGE_TEXT + " TEXT,"
                + COLUMN_TIMESTAMP + " TEXT"
                + ")";
        db.execSQL(CREATE_MESSAGES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPOINTMENTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
            onCreate(db);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public String getLastAppointmentId() {
        SQLiteDatabase db = this.getReadableDatabase();
        String lastId = "A000"; // Default if no appointments exist
        
        // Get all appointment IDs and find the highest numeric value
        String query = "SELECT " + COLUMN_APPOINTMENT_ID + 
                      " FROM " + TABLE_APPOINTMENTS;
        
        Cursor cursor = db.rawQuery(query, null);
        int maxNumber = 0;
        
        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(0);
                if (id.startsWith("A")) {
                    try {
                        int number = Integer.parseInt(id.substring(1));
                        maxNumber = Math.max(maxNumber, number);
                    } catch (NumberFormatException e) {
                        // Skip IDs that don't match our format
                        continue;
                    }
                }
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        
        // If we found any valid IDs, use the highest one + 1
        if (maxNumber > 0) {
            return String.format("A%03d", maxNumber + 1);
        }
        
        return lastId;
    }

    public void addAppointment(Appointment appointment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        // Generate sequential ID if not provided
        if (appointment.getAppointmentId() == null || appointment.getAppointmentId().isEmpty()) {
            String newId = getLastAppointmentId();
            values.put(COLUMN_APPOINTMENT_ID, newId);
        } else {
            values.put(COLUMN_APPOINTMENT_ID, appointment.getAppointmentId());
        }
        
        values.put(COLUMN_PATIENT_ID, appointment.getPatientId());
        values.put(COLUMN_PATIENT_NAME, appointment.getPatientName());
        values.put(COLUMN_DOCTOR_ID, appointment.getDoctorId());
        values.put(COLUMN_DOCTOR_NAME, appointment.getDoctorName());
        values.put(COLUMN_DATE, appointment.getDate());
        values.put(COLUMN_TIME, appointment.getTime());
        db.insert(TABLE_APPOINTMENTS, null, values);
        db.close();
    }

    public List<Appointment> getAllAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_APPOINTMENTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Appointment appointment = new Appointment(
                    cursor.getString(cursor.getColumnIndex(COLUMN_APPOINTMENT_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_PATIENT_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_PATIENT_NAME)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_DOCTOR_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_DOCTOR_NAME)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_DATE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_TIME))
                );
                appointments.add(appointment);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return appointments;
    }

    public boolean deleteAppointment(String appointmentId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_APPOINTMENTS, COLUMN_APPOINTMENT_ID + " = ?", 
                             new String[]{appointmentId});
        db.close();
        return result > 0;
    }

    public boolean deleteAppointmentsByDoctor(String doctorId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_APPOINTMENTS, COLUMN_DOCTOR_ID + " = ?", 
                             new String[]{doctorId});
        db.close();
        return result > 0;
    }

    public boolean deleteAppointmentsByPatient(String patientId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_APPOINTMENTS, COLUMN_PATIENT_ID + " = ?", 
                             new String[]{patientId});
        db.close();
        return result > 0;
    }

    public void clearAllAppointments() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_APPOINTMENTS, null, null);
        db.close();
    }

    // New message methods
    public void addMessage(Message message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MESSAGE_ID, message.getMessageId());
        values.put(COLUMN_SENDER_ID, message.getSenderId());
        values.put(COLUMN_SENDER_NAME, message.getSenderName());
        values.put(COLUMN_RECEIVER_ID, message.getReceiverId());
        values.put(COLUMN_MESSAGE_TEXT, message.getMessageText());
        values.put(COLUMN_TIMESTAMP, message.getTimestamp());
        
        Log.d("DatabaseHelper", "Adding message to database - " +
              "ID: " + message.getMessageId() + ", " +
              "Sender: " + message.getSenderId() + ", " +
              "Receiver: " + message.getReceiverId() + ", " +
              "Text: " + message.getMessageText());
              
        db.insert(TABLE_MESSAGES, null, values);
        db.close();
    }

    // Get all messages between two users
    public List<Message> getMessagesBetweenUsers(String user1Id, String user2Id) {
        List<Message> messages = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query;
        String[] selectionArgs;
        
        Log.d("DatabaseHelper", "Getting messages between users - User1: " + user1Id + ", User2: " + user2Id);
        
        // For patient viewing messages with admin
        if (user1Id.equals("ADMIN") || user2Id.equals("ADMIN")) {
            String patientId = user1Id.equals("ADMIN") ? user2Id : user1Id;
            query = "SELECT * FROM " + TABLE_MESSAGES + 
                   " WHERE (" + COLUMN_SENDER_ID + " = ? AND " + COLUMN_RECEIVER_ID + " = 'ADMIN') OR " +
                   "(" + COLUMN_SENDER_ID + " = 'ADMIN' AND " + COLUMN_RECEIVER_ID + " = ?) " +
                   "ORDER BY " + COLUMN_TIMESTAMP + " ASC";
            selectionArgs = new String[]{patientId, patientId};
            Log.d("DatabaseHelper", "Patient-Admin query: " + query + ", Args: " + patientId);
        } else {
            // For direct chat between two users
            query = "SELECT * FROM " + TABLE_MESSAGES + 
                   " WHERE (" + COLUMN_SENDER_ID + " = ? AND " + COLUMN_RECEIVER_ID + " = ?) OR " +
                   "(" + COLUMN_SENDER_ID + " = ? AND " + COLUMN_RECEIVER_ID + " = ?) " +
                   "ORDER BY " + COLUMN_TIMESTAMP + " ASC";
            selectionArgs = new String[]{user1Id, user2Id, user2Id, user1Id};
            Log.d("DatabaseHelper", "Direct chat query: " + query + ", Args: " + user1Id + ", " + user2Id);
        }
        
        Cursor cursor = db.rawQuery(query, selectionArgs);
        Log.d("DatabaseHelper", "Query returned " + cursor.getCount() + " messages");
        
        if (cursor.moveToFirst()) {
            do {
                Message message = new Message(
                    cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_SENDER_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_SENDER_NAME)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_RECEIVER_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE_TEXT)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP))
                );
                messages.add(message);
                Log.d("DatabaseHelper", "Found message - Sender: " + message.getSenderId() + 
                      ", Receiver: " + message.getReceiverId() + ", Text: " + message.getMessageText());
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        Log.d("DatabaseHelper", "Total messages found: " + messages.size());
        return messages;
    }

    public List<Message> getAllMessagesForUser(String userId) {
        List<Message> messages = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT * FROM " + TABLE_MESSAGES + 
                      " WHERE " + COLUMN_SENDER_ID + " = ? OR " + COLUMN_RECEIVER_ID + " = ? " +
                      "ORDER BY " + COLUMN_TIMESTAMP + " ASC";
        
        Cursor cursor = db.rawQuery(query, new String[]{userId, userId});
        
        if (cursor.moveToFirst()) {
            do {
                Message message = new Message(
                    cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_SENDER_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_SENDER_NAME)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_RECEIVER_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE_TEXT)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP))
                );
                messages.add(message);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return messages;
    }
} 