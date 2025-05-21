package com.example.geoquiz.presentation.feature_rider;

import android.annotation.SuppressLint;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geoquiz.R;
//import com.example.geoquiz.data.local.database.GeoQuizDatabase;
import com.example.geoquiz.data.local.database.MessageEntity;
import com.example.geoquiz.domain.repository.MessageRepository;
//import com.example.geoquiz.data.local.repository.MessageRepositoryImpl;
import com.example.geoquiz.domain.RiderHistoryAdapter;
import com.example.geoquiz.domain.model.RiderInfo;
import com.example.geoquiz.presentation.feature_role.RoleManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import jakarta.inject.Inject;

/**
 *RiderActivity manages rider chat, ride requests, and availability status.
 */
@AndroidEntryPoint
public class RiderActivity extends AppCompatActivity {

    private TextView tvRiderStatus;
    private EditText etMessage;
    private RecyclerView rvChatMessages, rvRideRequests;
    private Button btnViewRequests;


    private boolean isAvailable = true;
    private boolean showingRequests = false;
    private String selectedUserPhone = null;  // phone number of the user currently in chat


    private final List<RiderInfo> currentChatList = new ArrayList<>();
    private RiderHistoryAdapter chatAdapter;

    @Inject
    MessageRepository messageRepository;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (RoleManager.getRole(this) != RoleManager.Role.RIDER) {
            Toast.makeText(this, "This screen is for riders only.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setContentView(R.layout.riderider);

        // 🔗 UI references
        tvRiderStatus = findViewById(R.id.tvRiderStatus);
        etMessage = findViewById(R.id.etMessage);
        rvChatMessages = findViewById(R.id.rvChatMessages);
        rvRideRequests = findViewById(R.id.rvRideRequests);
        Button btnUpdateStatus = findViewById(R.id.btnUpdateStatus);
        btnViewRequests = findViewById(R.id.btnViewRequests);
        MaterialButton btnSend = findViewById(R.id.btnSend);


        // If coming from a ride request notification/Intent, set the chat target
        if (getIntent().hasExtra("riderPhone")) {
            selectedUserPhone = getIntent().getStringExtra("riderPhone");
            Toast.makeText(this, "Chatting with user " + selectedUserPhone, Toast.LENGTH_SHORT).show();
        }
        // 🟢 Set up Chat RecyclerView

        chatAdapter= new RiderHistoryAdapter(this, currentChatList, riderInfo -> { });
        rvChatMessages.setLayoutManager(new LinearLayoutManager(this));
        rvChatMessages.setAdapter(chatAdapter);
        observeChatMessages();

        // Initially hide ride requests.
        rvRideRequests.setVisibility(View.GONE);

        // 🟡 Set up Ride Requests RecyclerView (initially hidden)
        List<RiderInfo> requestList = new ArrayList<>();
        requestList.add(new RiderInfo("Request #1", "Pickup at Station Rd", true, R.drawable.ic_profile_placeholder,"0712345678"));
        requestList.add(new RiderInfo("Request #2", "To Campus A", true, R.drawable.ic_profile_placeholder,"0723456789"));
        RiderHistoryAdapter requestAdapter = new RiderHistoryAdapter(this, requestList, riderInfo -> {

            // When a request is selected, choose that user for chatting
            selectedUserPhone = riderInfo.getPhoneNumber();
            Toast.makeText(this, "Selected request from " + selectedUserPhone, Toast.LENGTH_SHORT).show();
            // Switch views: hide requests, show chat messages
            showingRequests = false;
            rvRideRequests.setVisibility(View.GONE);
            rvChatMessages.setVisibility(View.VISIBLE);
            btnViewRequests.setText("View Requests");
            // (Messages will be filtered to this user in observeChatMessages on the next update)
        });


        rvRideRequests.setLayoutManager(new LinearLayoutManager(this));
        rvRideRequests.setAdapter(requestAdapter);


        // 🔁 Toggle Status
        btnUpdateStatus.setOnClickListener(v -> {
            isAvailable = !isAvailable;
            tvRiderStatus.setText(isAvailable ? "Available" : "Busy");
            tvRiderStatus.setTextColor(isAvailable ? Color.parseColor("#388E3C") : Color.RED);
        });

        // 👁️ Toggle Ride Requests View
        btnViewRequests.setOnClickListener(v -> {
            showingRequests = !showingRequests;
            rvRideRequests.setVisibility(showingRequests ? View.VISIBLE : View.GONE);
            rvChatMessages.setVisibility(showingRequests ? View.GONE : View.VISIBLE);
            btnViewRequests.setText(showingRequests ? "Hide Requests" : "View Requests");
        });
        // Chat send Logic
        btnSend.setOnClickListener(v -> {
            String messageText = etMessage.getText().toString().trim();
            if (selectedUserPhone == null) {
                // No user selected to chat with
                Toast.makeText(this, "Select a request to reply to.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (messageText.isEmpty()) {
                Toast.makeText(this, "Please enter a message.", Toast.LENGTH_SHORT).show();
                return;
            }
            // Insert the new message into the database (this simulates sending an SMS to the user)
            insertChatMessage(messageText);
            etMessage.setText(""); // Clear the input field after sending
        });
    }

    private void insertChatMessage(String message) {
        //MessageRepositoryImpl messageRepo = new MessageRepositoryImpl(db);
         messageRepository.insertMessage(
                 new MessageEntity("You", message, "0712345678",System.currentTimeMillis())
         );

    }

    @SuppressLint("NotifyDataSetChanged")
    private void observeChatMessages() {

       // GeoQuizDatabase db = GeoQuizDatabase.getInstance(getApplication());
        //MessageRepositoryImpl messageRepo = new MessageRepositoryImpl(db);


        messageRepository   .getAllMessages().observe(this, messageEntities -> {
        //Convert to RiderInfo
         currentChatList.clear();
            if (selectedUserPhone != null) {
                // If a specific user is selected, filter messages to only those with that user
                for (MessageEntity entity : messageEntities) {
                    String otherParty = entity.getSender().equals("You") ? entity.getReceiver() : entity.getSender();
                    if (otherParty.equals(selectedUserPhone)) {
                        currentChatList.add(new RiderInfo(
                                entity.getSender(),
                                entity.getMessage(),
                                true,
                                R.drawable.ic_profile_placeholder,
                                entity.getSender()
                        ));
                    }
                }
            } else {
                // No specific user selected: show all messages (may include multiple conversations mixed)
                for (MessageEntity entity : messageEntities) {
                    currentChatList.add(new RiderInfo(
                            entity.getSender(),
                            entity.getMessage(),
                            true,
                            R.drawable.ic_profile_placeholder,
                            entity.getSender()
                    ));
                }
            }
            chatAdapter.notifyDataSetChanged();
            if (!currentChatList.isEmpty()) {
                rvChatMessages.scrollToPosition(currentChatList.size() - 1);
            }
        });


    }


}


