
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
import com.example.geoquiz.domain.RiderHistoryAdapter;
import com.example.geoquiz.domain.model.RiderInfo;
import com.example.geoquiz.presentation.feature_request.RequestRideActivity;
import com.example.geoquiz.presentation.feature_rider.RiderActivity;
import com.example.geoquiz.presentation.feature_role.RoleManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * MainFunctionActivity allows main chat window for users to chat with riders
 */
@AndroidEntryPoint
public class MainFunctionActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 101;
    private static final int LOCATION_PERMISSION_CODE = 102;
    //private static final int REQUEST_CHECK_SETTINGS = 103;


    private String selectedPhoneNumber = null;
    private EditText etMessage;
    private Button btnSend;
    private MainFunctionViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (RoleManager.getRole(this) != RoleManager.Role.USER) {
            Toast.makeText(this, "Only users can access the main function screen.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        Log.d("MainFunctionActivity", "Layout loaded");

        RecyclerView rvChatMessages = findViewById(R.id.rvChatMessages);
        rvChatMessages.setLayoutManager(new LinearLayoutManager(this));

        if (selectedPhoneNumber == null) {
                   List<RiderInfo> availableRiders = new ArrayList<>();
                   availableRiders.add(new RiderInfo(
                               "0712345678", "Alice", true,
                              R.drawable.ic_profile_placeholder, "0712345678"));
                  availableRiders.add(new RiderInfo(
                               "0723456789", "Bob", true,
                            R.drawable.ic_profile_placeholder, "0723456789"));
                 availableRiders.add(new RiderInfo(
                              "0734567890", "Charlie", true,
                              R.drawable.ic_profile_placeholder, "0734567890"));

                        // Adapter to let user pick a rider
                                 RiderHistoryAdapter selectAdapter = new RiderHistoryAdapter(
                             this,
                             availableRiders,
                             riderInfo -> {
                                      // Save the chosen phone number
                                              selectedPhoneNumber = riderInfo.getPhoneNumber();
                                     Toast.makeText(this,
                                                  "Rider selected: " + selectedPhoneNumber,
                                                  Toast.LENGTH_SHORT).show();
                                      // Re-run onCreate so the chat view logic kicks in
                                             recreate();
                                });

                         rvChatMessages.setAdapter(selectAdapter);
                  // Disable chat & ride buttons until a rider is chosen
                          findViewById(R.id.btnSend).setEnabled(false);
                 findViewById(R.id.btnRequestRide).setEnabled(false);
                 findViewById(R.id.btnShareLocation).setEnabled(false);
                  return;
               }
        if (getIntent().hasExtra("riderPhone")) {
            selectedPhoneNumber = getIntent().getStringExtra("riderPhone");
            Toast.makeText(this, "Reselected rider: " + selectedPhoneNumber, Toast.LENGTH_SHORT).show();
        }


        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        Button btnRequestRide = findViewById(R.id.btnRequestRide);
        Button btnShareLocation = findViewById(R.id.btnShareLocation);


        //Setup ViewModel
        viewModel = new ViewModelProvider(this).get(MainFunctionViewModel.class);

        //Observe messages
        viewModel.getMessages().observe(this, messageEntities -> {
            if (messageEntities == null) return;
            Map<String,List<MessageEntity>> riderMessages = new HashMap<>();
            for (MessageEntity entity : messageEntities) {
                String riderPhone = entity.getReceiver().equals("You") ? entity.getSender() : entity.getReceiver();
                if (!riderMessages.containsKey(riderPhone)) {
                    riderMessages.put(riderPhone, new ArrayList<>());
                }
                riderMessages.get(riderPhone).add(entity);
            }
            // Convert to RiderInfo list
            List<RiderInfo> riderList = new ArrayList<>();
            for (Map.Entry<String, List<MessageEntity>> entry : riderMessages.entrySet()) {
                MessageEntity lastMessage = entry.getValue().get(entry.getValue().size() - 1);
                riderList.add(new RiderInfo(
                        entry.getKey(), // phone number
                        lastMessage.getMessage(), // last message
                        true, // is online (you might want to change this)
                        R.drawable.ic_profile_placeholder,
                        entry.getKey() // phone number as ID
                ));
            }

            RiderHistoryAdapter adapter = new RiderHistoryAdapter(
                    this,
                    riderList,
                    riderInfo ->{
                        Intent chatIntent = new Intent(this, chatDetailActivity.class)
                                .putExtra("riderPhone", selectedPhoneNumber)
                                .putExtra("riderName", "Rider " + selectedPhoneNumber.substring(0,4));
                        startActivity(chatIntent);
                                    // Optionally open chat detail directly:
                        // startActivity(new Intent(this, chatDetailActivity.class)
                        //         .putExtra("riderPhone", selectedPhoneNumber)
                        //         .putExtra("riderName", "Rider " + selectedPhoneNumber.substring(0, 4)));
            });
            rvChatMessages.setAdapter(adapter);
        });

        btnRequestRide.setOnClickListener(v -> {
            if (selectedPhoneNumber == null) {
                Toast.makeText(this, "Select a rider before requesting a ride.", Toast.LENGTH_SHORT).show();
                return;
            }

            startActivity(new Intent(this, RequestRideActivity.class)
                    .putExtra("riderPhone", selectedPhoneNumber));
            finish();
        });

        btnShareLocation.setOnClickListener(v -> {
            if (selectedPhoneNumber == null) {
                Toast.makeText(this, "Select a rider before sharing location.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_CODE);
            } else {
                shareCurrentLocation();
            }
        });

        btnSend.setOnClickListener(v -> {
            String messageText = etMessage.getText().toString().trim();

            if (selectedPhoneNumber == null) {
                Toast.makeText(this, "Select a rider to send message.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (messageText.isEmpty()) {
                Toast.makeText(this, "Please enter a message.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
                return;
            }
            // Simulate SMS sending (add real SMSManager logic if needed)
            Toast.makeText(this, "SMS sent to " + selectedPhoneNumber, Toast.LENGTH_SHORT).show();

            viewModel.insertMessage(
                    new MessageEntity("You", selectedPhoneNumber, messageText, System.currentTimeMillis())
            );

            etMessage.setText(""); // Clear input
        });
    }

    private void shareCurrentLocation() {
        // Here you would implement actual location sharing logic
        // For now, we'll just show a toast
        Toast.makeText(this, "Sharing current location...", Toast.LENGTH_SHORT).show();

        // In a real app, you would:
        // 1. Get current location using FusedLocationProviderClient
        // 2. Send the location to your server or the selected rider
        // 3. Update UI accordingly
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
        } else if (requestCode == LOCATION_PERMISSION_CODE) {
            if (0 < grantResults.length && PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                shareCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied. Cannot share location.", Toast.LENGTH_LONG).show();
            }

        }

    }}