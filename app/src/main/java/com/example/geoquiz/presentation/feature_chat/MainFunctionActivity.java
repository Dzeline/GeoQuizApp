
package com.example.geoquiz.presentation.feature_chat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geoquiz.R;
import com.example.geoquiz.data.local.database.MessageEntity;
import com.example.geoquiz.data.local.database.RiderEntity;
import com.example.geoquiz.domain.RiderHistoryAdapter;
import com.example.geoquiz.domain.model.RiderInfo;
import com.example.geoquiz.domain.repository.RiderRepository;
import com.example.geoquiz.presentation.feature_request.RequestRideActivity;

import com.example.geoquiz.presentation.feature_role.RoleManager;
import com.example.geoquiz.util.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * MainFunctionActivity allows main chat window for users to chat with riders
 */
@AndroidEntryPoint
public class MainFunctionActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 101;
    private static final int LOCATION_PERMISSION_CODE = 102;
    //private static final int REQUEST_CHECK_SETTINGS = 103;
    @Inject
    public RiderRepository riderRepository;

    private MainFunctionViewModel viewModel;
    private String selectedPhoneNumber = null;
    private EditText etMessage;
    private Button btnSend;
    private Button btnRequestRide;
    private  Button btnShareLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (RoleManager.getRole(this) != RoleManager.Role.USER) {
            Toast.makeText(this, "Only users can access the main function screen.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_main_function);
        Log.d("MainFunctionActivity", "Layout loaded");

        // Rider selection UI until a rider is picked
        btnSend = findViewById(R.id.btnSend);
        btnRequestRide = findViewById(R.id.btnRequestRide);
        btnShareLocation = findViewById(R.id.btnShareLocation);
        etMessage = findViewById(R.id.etMessage);


        // Initialize ViewModel (after Hilt injection)
        viewModel = new ViewModelProvider(this).get(MainFunctionViewModel.class);

        RecyclerView rvChatMessages = findViewById(R.id.rvChatMessages);
        rvChatMessages.setLayoutManager(new LinearLayoutManager(this));

        // Set initial button states to disabled
        btnSend.setEnabled(false);
        btnRequestRide.setEnabled(false);
        btnShareLocation.setEnabled(false);
        
        // Check if a rider was passed in the intent or stored in the session
        if (getIntent().hasExtra("riderPhone")) {
            selectedPhoneNumber = getIntent().getStringExtra("riderPhone");
            SessionManager.getInstance().setSelectedRiderPhone(selectedPhoneNumber);
            Toast.makeText(this, "Reselected rider: " + selectedPhoneNumber, Toast.LENGTH_SHORT).show();
            
            // Load messages for this rider
            if (selectedPhoneNumber != null) {
                viewModel.loadMessagesForRider(selectedPhoneNumber);
            }
        } else {
            // Try to get the rider from the session
            selectedPhoneNumber = SessionManager.getInstance().getSelectedRiderPhone();
            
            // Load messages for this rider if available
            if (selectedPhoneNumber != null) {
                viewModel.loadMessagesForRider(selectedPhoneNumber);
            }
        }
        
        // Update UI based on rider selection
        updateButtonStates();
        // 1) Observe live riders list and seed if empty

        viewModel.getAvailableRiders().observe(this, ridersInfos -> {
            if (ridersInfos == null || ridersInfos.isEmpty()) {
                riderRepository.insertRider(new RiderEntity("0712345678", "Alice", true));
                riderRepository.insertRider(new RiderEntity("0723456789", "Bob", true));
                riderRepository.insertRider(new RiderEntity("0734567890", "Charlie", true));
                return;
            }
            rvChatMessages.setAdapter(new RiderHistoryAdapter(
                        this,
                        ridersInfos,
                        info -> {
                            selectedPhoneNumber = info.getPhoneNumber();
                            SessionManager.getInstance().setSelectedRiderPhone(selectedPhoneNumber);
                            
                            // Load messages for the selected rider
                            viewModel.loadMessagesForRider(selectedPhoneNumber);
                            
                            // Update UI based on rider selection
                            updateButtonStates();
                        }
                ));

        });

        // Observe chat messages for the selected rider
        viewModel.getMessages().observe(this, messageEntities -> {
            if (messageEntities == null) return;
            Map<String,List<MessageEntity>> riderMessages = new HashMap<>();
            List<RiderInfo> chatList = new ArrayList<>();

            for (MessageEntity entity : messageEntities) {
                String riderPhone = entity.getReceiver().equals("You") ?
                        entity.getSender() : entity.getReceiver();
                riderMessages
                        .computeIfAbsent(riderPhone, k -> new ArrayList<>())
                        .add(entity);
            }
            // Convert to RiderInfo list
            for (Map.Entry<String, List<MessageEntity>> entry : riderMessages.entrySet()) {
                List<MessageEntity> list = entry.getValue();
                MessageEntity last = list.get(list.size() - 1);
                chatList.add(new RiderInfo(
                        entry.getKey(),
                        last.getMessage(),
                        true,
                        R.drawable.ic_profile_placeholder,
                        entry.getKey()
                ));
            }

            rvChatMessages.setAdapter(new RiderHistoryAdapter(
                    this,
                    chatList,
                    riderInfo -> {
                        Intent chatIntent = new Intent(this, chatDetailActivity.class)
                                .putExtra("riderPhone", riderInfo.getPhoneNumber())
                                .putExtra("riderName", riderInfo.getRiderName());
                        startActivity(chatIntent);
                    }
            ));
        });

        // Request ride button
        btnRequestRide.setOnClickListener(v -> {
            if (selectedPhoneNumber == null) {
                Toast.makeText(this, "Select a rider before requesting a ride.", Toast.LENGTH_SHORT).show();
                // Highlight the rider list to indicate user needs to select a rider
                rvChatMessages.requestFocus();
                return;
            }

            // Visual feedback that button was pressed
            btnRequestRide.setPressed(true);
            
            // Show a toast to indicate the action is being processed
            Toast.makeText(this, "Requesting ride from " + selectedPhoneNumber + "...", Toast.LENGTH_SHORT).show();
            
            // Start the RequestRideActivity with the selected rider
            startActivity(new Intent(this, RequestRideActivity.class)
                    .putExtra("riderPhone", selectedPhoneNumber));
            finish();
        });

        // Share location button
        btnShareLocation.setOnClickListener(v -> {
            if (selectedPhoneNumber == null) {
                Toast.makeText(this, "Select a rider before sharing location.", Toast.LENGTH_SHORT).show();
                // Highlight the rider list to indicate user needs to select a rider
                rvChatMessages.requestFocus();
                return;
            }
            
            // Visual feedback that button was pressed
            btnShareLocation.setPressed(true);
            
            // Check for location permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request location permission
                Toast.makeText(this, "Location permission needed to share your location", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_CODE);
            } else {
                // Share location if permission is granted
                shareCurrentLocation();
                
                // Add a message to the chat indicating location was shared
                viewModel.insertMessage(
                        new MessageEntity("You", selectedPhoneNumber, "I've shared my location with you.", System.currentTimeMillis())
                );
            }
        });

        // Send chat message
        btnSend.setOnClickListener(v -> {
            String messageText = etMessage.getText().toString().trim();

            if (selectedPhoneNumber == null) {
                Toast.makeText(this, "Select a rider to send message.", Toast.LENGTH_SHORT).show();
                // Highlight the rider list to indicate user needs to select a rider
                rvChatMessages.requestFocus();
                return;
            }
            
            if (messageText.isEmpty()) {
                Toast.makeText(this, "Please enter a message.", Toast.LENGTH_SHORT).show();
                // Focus on the message input field
                etMessage.requestFocus();
                return;
            }
            
            // Visual feedback that button was pressed
            btnSend.setPressed(true);
            
            // Check for SMS permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission needed to send messages", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        SMS_PERMISSION_CODE);
                return;
            }
            
            // Simulate SMS sending (add real SMSManager logic if needed)
            Toast.makeText(this, "Message sent to " + selectedPhoneNumber, Toast.LENGTH_SHORT).show();

            // Insert the message into the database
            viewModel.insertMessage(
                    new MessageEntity("You", selectedPhoneNumber, messageText, System.currentTimeMillis())
            );

            // Clear input and reset focus
            etMessage.setText(""); 
            etMessage.requestFocus();
        });
    }


    /**
     * Shares the user's current location with the selected rider
     * This is a simulated implementation for demonstration purposes
     */
    private void shareCurrentLocation() {
        if (selectedPhoneNumber == null) {
            Toast.makeText(this, "Select a rider before sharing location.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show a progress indicator
        Toast.makeText(this, "Getting your location...", Toast.LENGTH_SHORT).show();
        
        // Simulate a delay to get the location (in a real app, you would use FusedLocationProviderClient)
        new android.os.Handler().postDelayed(() -> {
            // Simulate getting a location
            String simulatedLocation = "37.7749,-122.4194"; // Example coordinates (San Francisco)
            
            // Show success message
            Toast.makeText(this, "Location shared with " + selectedPhoneNumber, Toast.LENGTH_SHORT).show();
            
            // In a real app, you would:
            // 1. Get current location using FusedLocationProviderClient
            // 2. Send the location to your server or directly to the selected rider
            // 3. Update UI to show the shared location on a map
            
            // For this demo, we'll just add a message to the chat
            viewModel.insertMessage(
                    new MessageEntity("You", selectedPhoneNumber, 
                    "I've shared my location: " + simulatedLocation, 
                    System.currentTimeMillis())
            );
            
            // Update UI to indicate location was shared successfully
            btnShareLocation.setPressed(false);
        }, 1500); // Simulate 1.5 second delay to get location
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (SMS_PERMISSION_CODE == requestCode) {
            if (0 < grantResults.length && PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                Toast.makeText(this, "SMS permission granted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS permission denied. Cannot send messages.", Toast.LENGTH_LONG).show();
            }
        } else if (LOCATION_PERMISSION_CODE == requestCode) {
            if (0 < grantResults.length && PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                this.shareCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied. Cannot share location.", Toast.LENGTH_LONG).show();
            }

        }

    }
    /**
     * Updates the UI button states based on rider selection
     * Enables/disables buttons and updates UI elements to reflect the current selection
     */
    private void updateButtonStates() {
        boolean riderSelected = selectedPhoneNumber != null;
        
        // Enable/disable buttons based on rider selection
        btnSend.setEnabled(riderSelected);
        btnRequestRide.setEnabled(riderSelected);
        btnShareLocation.setEnabled(riderSelected);
        
        // Update UI to reflect selection status
        if (riderSelected) {
            // Update message input hint to show who the user is messaging
            etMessage.setHint("Type a message to " + selectedPhoneNumber + "...");
            
            // Change button appearance to indicate they're active
            btnSend.setAlpha(1.0f);
            btnRequestRide.setAlpha(1.0f);
            btnShareLocation.setAlpha(1.0f);
            
            // Set the toolbar title to indicate who the user is chatting with
            androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                toolbar.setTitle("Chat with " + selectedPhoneNumber);
            }
            
            // Show toast to confirm selection
            Toast.makeText(this, "Rider selected: " + selectedPhoneNumber, Toast.LENGTH_SHORT).show();
        } else {
            // Reset UI elements when no rider is selected
            etMessage.setHint("Select a rider to start messaging");
            
            // Dim buttons to indicate they're inactive
            btnSend.setAlpha(0.5f);
            btnRequestRide.setAlpha(0.5f);
            btnShareLocation.setAlpha(0.5f);
            
            // Reset toolbar title
            androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                toolbar.setTitle("Select a Rider");
            }
        }
    }



}