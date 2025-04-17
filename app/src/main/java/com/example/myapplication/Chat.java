package com.example.myapplication;

public class Chat {
    private String patientId;
    private String patientName;
    private String lastMessage;
    private String timestamp;
    private boolean hasUnreadMessages;

    public Chat(String patientId, String patientName, String lastMessage, String timestamp, boolean hasUnreadMessages) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.hasUnreadMessages = hasUnreadMessages;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean hasUnreadMessages() {
        return hasUnreadMessages;
    }

    public void updateMessage(String lastMessage, String timestamp) {
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
    }
} 