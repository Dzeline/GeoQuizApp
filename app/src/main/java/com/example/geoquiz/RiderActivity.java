package com.example.geoquiz;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geoquiz.adapters.RiderHistoryAdapter;
import com.example.geoquiz.adapters.RiderInfo;
import com.example.geoquiz.databse.GeoQuizDatabaseHelper;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;


public class RiderActivity extends AppCompatActivity {

    private TextView tvRiderStatus;
    private EditText etMessage;
    private RecyclerView rvChatMessages, rvRideRequests;
    private Button btnViewRequests;

    private RiderHistoryAdapter chatAdapter , requestAdapter;
    private List<RiderInfo> chatList ,requestList ;

    private boolean isAvailable = true;
    private boolean showingRequests = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.riderider);

        // 🔗 UI references
        tvRiderStatus = findViewById(R.id.tvRiderStatus);
        etMessage =findViewById(R.id.etMessage);
        rvChatMessages = findViewById(R.id.rvChatMessages);
        rvRideRequests = findViewById(R.id.rvRideRequests);
        Button btnUpdateStatus = findViewById(R.id.btnUpdateStatus);
        btnViewRequests = findViewById(R.id.btnViewRequests);
        MaterialButton btnSend = findViewById(R.id.btnSend);


        // 🟢 Set up Chat RecyclerView
        List<RiderInfo> chatList = getAllChatMessages();
        chatAdapter = new RiderHistoryAdapter(this,chatList);
        RiderHistoryAdapter chatAdapter = new RiderHistoryAdapter(this, chatList);
        rvChatMessages.setLayoutManager(new LinearLayoutManager(this));
        rvChatMessages.setAdapter(chatAdapter);

        // 🟡 Set up Ride Requests RecyclerView (initially hidden)
        List<RiderInfo> requestList = new ArrayList<>();
        requestList.add(new RiderInfo("Request #1", "Pickup at Station Rd", true, R.drawable.ic_profile_placeholder));
        requestList.add(new RiderInfo("Request #2", "To Campus A", true, R.drawable.ic_profile_placeholder));
        RiderHistoryAdapter requestAdapter = new RiderHistoryAdapter(this, requestList);
        rvRideRequests.setLayoutManager(new LinearLayoutManager(this));
        rvRideRequests.setAdapter(requestAdapter);
        rvRideRequests.setVisibility(View.GONE);

        // 🔁 Toggle Status
        btnUpdateStatus.setOnClickListener(v -> {
            isAvailable = !isAvailable;
            if (isAvailable) {
                tvRiderStatus.setText("Available");
                tvRiderStatus.setTextColor(Color.parseColor("#388E3C")); // Green
            } else {
                tvRiderStatus.setText("Busy");
                tvRiderStatus.setTextColor(Color.RED);
            }
        });

        // 👁️ Toggle Ride Requests View
        btnViewRequests.setOnClickListener(v -> {
            showingRequests = !showingRequests;
            if (showingRequests) {
                rvRideRequests.setVisibility(View.VISIBLE);
                rvChatMessages.setVisibility(View.GONE);
                btnViewRequests.setText("Hide Requests");
            } else {
                rvRideRequests.setVisibility(View.GONE);
                rvChatMessages.setVisibility(View.VISIBLE);
                btnViewRequests.setText("View Requests");
            }
        });
        // Chat send Logic
        btnSend.setOnClickListener(v -> {
            String messageText = etMessage.getText().toString().trim();
            if (!messageText.isEmpty()) {
                insertChatMessage("You",messageText);//Save to DB
                RiderInfo newMessage = new RiderInfo("You", messageText, isAvailable, R.drawable.ic_profile_placeholder);
                chatList.add(newMessage);
                chatAdapter.notifyItemInserted(chatList.size() - 1);
                rvChatMessages.scrollToPosition(chatList.size() - 1);
                etMessage.setText(""); // Clear input
            }
        });
    }
        //Simulate syncing rider status to backend
    private void syncRiderStatusToServer(boolean status) {
        // Simulated backend sync
        Log.d("RiderStatus", "Rider is now: " + (status ? "Available" : "Busy"));
    }

    public RiderHistoryAdapter getChatAdapter() {
        return chatAdapter;
    }

    public void setChatAdapter(RiderHistoryAdapter chatAdapter) {
        this.chatAdapter = chatAdapter;
    }

    public RiderHistoryAdapter getRequestAdapter() {
        return requestAdapter;
    }

    public void setRequestAdapter(RiderHistoryAdapter requestAdapter) {
        this.requestAdapter = requestAdapter;
    }

    public List<RiderInfo> getChatList() {
        return chatList;
    }

    public void setChatList(List<RiderInfo> chatList) {
        this.chatList = chatList;
    }

    public List<RiderInfo> getRequestList() {
        return requestList;
    }

    public void setRequestList(List<RiderInfo> requestList) {
        this.requestList = requestList;
    }

    private void insertChatMessage(String sender, String message) {
        GeoQuizDatabaseHelper dbHelper = new GeoQuizDatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("sender", sender);
        values.put("message", message);

        db.insert("Messages", null, values);
        db.close();
    }

    private List<RiderInfo> getAllChatMessages() {
        List<RiderInfo> messages = new ArrayList<>();
        GeoQuizDatabaseHelper dbHelper = new GeoQuizDatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query("Messages", null, null, null, null, null, "timestamp ASC");

        while (cursor.moveToNext()) {
            String sender = cursor.getString(cursor.getColumnIndexOrThrow("sender"));
            String message = cursor.getString(cursor.getColumnIndexOrThrow("message"));
            messages.add(new RiderInfo(sender, message, true, R.drawable.ic_profile_placeholder));
        }

        cursor.close();
        db.close();
        return messages;
    }


}


