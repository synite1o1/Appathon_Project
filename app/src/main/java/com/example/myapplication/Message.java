package com.example.myapplication;

public class Message {
    private String messageId;
    private String senderId;
    private String senderName;
    private String receiverId;
    private String messageText;
    private String timestamp;

    public Message(String messageId, String senderId, String senderName, String receiverId, String messageText, String timestamp) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.messageText = messageText;
        this.timestamp = timestamp;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getTimestamp() {
        return timestamp;
    }
} 