package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminMessagesActivity extends AppCompatActivity implements ChatAdapter.OnChatClickListener {
    private static final String TAG = "AdminMessagesActivity";
    private RecyclerView chatListRecyclerView;
    private ChatAdapter chatAdapter;
    private DatabaseHelper dbHelper;
    private List<Chat> chats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_messages);

        dbHelper = new DatabaseHelper(this);
        chatListRecyclerView = findViewById(R.id.chatListRecyclerView);
        chatListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        chats = new ArrayList<>();
        chatAdapter = new ChatAdapter(chats, this);
        chatListRecyclerView.setAdapter(chatAdapter);

        loadChats();
    }

    private void loadChats() {
        List<Message> allMessages = dbHelper.getAllMessagesForUser("ADMIN");
        List<Chat> uniqueChats = new ArrayList<>();

        // Create a map to track the latest message for each patient
        for (Message message : allMessages) {
            String patientId = message.getSenderId().equals("ADMIN") ? 
                             message.getReceiverId() : message.getSenderId();
            
            // Skip if this is a message to/from "ALL"
            if (patientId.equals("ALL")) continue;

            // Find or create chat for this patient
            Chat existingChat = null;
            for (Chat chat : uniqueChats) {
                if (chat.getPatientId().equals(patientId)) {
                    existingChat = chat;
                    break;
                }
            }

            if (existingChat == null) {
                // Create new chat
                uniqueChats.add(new Chat(
                    patientId,
                    message.getSenderName(),
                    message.getMessageText(),
                    message.getTimestamp(),
                    false // You can implement unread message tracking if needed
                ));
            } else {
                // Update existing chat with latest message
                // Only update if this message is newer than the current one
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    Date newMessageDate = sdf.parse(message.getTimestamp());
                    Date existingMessageDate = sdf.parse(existingChat.getTimestamp());
                    
                    if (newMessageDate.after(existingMessageDate)) {
                        existingChat.updateMessage(message.getMessageText(), message.getTimestamp());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing dates", e);
                }
            }
        }

        // Sort chats by timestamp in descending order (newest first)
        Collections.sort(uniqueChats, new Comparator<Chat>() {
            @Override
            public int compare(Chat chat1, Chat chat2) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    Date date1 = sdf.parse(chat1.getTimestamp());
                    Date date2 = sdf.parse(chat2.getTimestamp());
                    return date2.compareTo(date1); // Descending order
                } catch (Exception e) {
                    Log.e(TAG, "Error sorting chats", e);
                    return 0;
                }
            }
        });

        chatAdapter.updateChats(uniqueChats);
    }

    @Override
    public void onChatClick(String patientId, String patientName) {
        Intent intent = new Intent(this, MessagingActivity.class);
        intent.putExtra("currentUserId", "ADMIN");
        intent.putExtra("currentUserName", "Admin");
        intent.putExtra("receiverId", patientId);
        intent.putExtra("receiverName", patientName);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChats();
    }
} 