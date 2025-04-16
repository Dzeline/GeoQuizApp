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
import com.example.geoquiz.data.local.database.GeoQuizDatabase;
import com.example.geoquiz.data.local.database.MessageEntity;
import com.example.geoquiz.data.local.repository.MessageRepositoryImpl;
import com.example.geoquiz.domain.RiderHistoryAdapter;
import com.example.geoquiz.domain.model.RiderInfo;
import com.example.geoquiz.presentation.feature_role.RoleManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

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

    private final List<RiderInfo> currentChatList = new ArrayList<>();
    private RiderHistoryAdapter chatAdapter;

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
        RiderHistoryAdapter requestAdapter = new RiderHistoryAdapter(this, requestList, riderInfo -> { });
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
            if (!messageText.isEmpty()) {
                insertChatMessage(messageText);
                etMessage.setText(""); // Clear input
            }
        });
    }

    private void insertChatMessage(String message) {
        GeoQuizDatabase db = GeoQuizDatabase.getInstance(getApplication());
        MessageRepositoryImpl messageRepo = new MessageRepositoryImpl(db);
         messageRepo.insertMessage(
                 new MessageEntity("You", message, "0712345678",System.currentTimeMillis())
         );

    }

    @SuppressLint("NotifyDataSetChanged")
    private void observeChatMessages() {

        GeoQuizDatabase db = GeoQuizDatabase.getInstance(getApplication());
        MessageRepositoryImpl messageRepo = new MessageRepositoryImpl(db);


        messageRepo.getAllMessages().observe(this, messageEntities -> {
        //Convert to RiderInfo
         currentChatList.clear();
        for (MessageEntity entity : messageEntities) {
            currentChatList.add(new RiderInfo(
                    entity.sender,
                    entity.message,
                    true, // isAvailable — set true or pull from elsewhere
                    R.drawable.ic_profile_placeholder,
                    entity.sender
            ));
        }
            chatAdapter.notifyDataSetChanged();
            if (!currentChatList.isEmpty()) {
                rvChatMessages.scrollToPosition(currentChatList.size() - 1);
            }
        });


    }


}


