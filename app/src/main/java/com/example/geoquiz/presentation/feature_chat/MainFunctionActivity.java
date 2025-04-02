
package com.example.geoquiz.presentation.feature_chat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

/**
 * MainFunctionActivity allows main chat window for users to chat with riders
 */
public class MainFunctionActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 101;

    private String selectedPhoneNumber = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String userRole = getIntent().getStringExtra("role");

        if (userRole != null && userRole.equals("requester")) {
            Toast.makeText(this, "Welcome back, Requester 👋", Toast.LENGTH_SHORT).show();
            // 💡 Customize UI here: hide unavailable options, highlight request tools
        }


        EditText etMessage = findViewById(R.id.etMessage);
        Button btnSend = findViewById(R.id.btnSend);

        RecyclerView rvChatMessages = findViewById(R.id.rvChatMessages);
        rvChatMessages.setLayoutManager(new LinearLayoutManager(this));




        MainFunctionViewModel viewModel = new ViewModelProvider(this).get(MainFunctionViewModel.class);

        viewModel.getMessages().observe(this, messageEntities -> {
            List<RiderInfo> riderList = new ArrayList<>();
            for (MessageEntity entity : messageEntities) {
                riderList.add(new RiderInfo(
                        entity.sender,
                        entity.message,
                        true,
                        R.drawable.ic_profile_placeholder,
                        entity.sender

                ));
            }

            RiderHistoryAdapter adapter = new RiderHistoryAdapter(this, riderList, riderInfo ->{
                selectedPhoneNumber = riderInfo.getPhoneNumber();
                Toast.makeText(this, "Rider selected: " + selectedPhoneNumber, Toast.LENGTH_SHORT).show();
            });
            rvChatMessages.setAdapter(adapter);
        });


        Button btnRequestRide = findViewById(R.id.btnRequestRide);
        btnRequestRide.setOnClickListener(v -> {
            Intent intent = new Intent(MainFunctionActivity.this, RequestRideActivity.class);
            startActivity(intent);
        });

        Button btnShareLocation = findViewById(R.id.btnShareLocation);
        btnShareLocation.setOnClickListener(v -> {
            Intent intent = new Intent(MainFunctionActivity.this, RiderActivity.class);
            startActivity(intent);
        });

        btnSend.setOnClickListener(v -> {
            String messageText = etMessage.getText().toString().trim();

            if (selectedPhoneNumber == null) {
                Toast.makeText(this, "Select a rider to send message.", Toast.LENGTH_SHORT).show();
                return;
            }


            if (!messageText.isEmpty()) {
                try {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
                        Toast.makeText(this, "SMS permission required to send messages.", Toast.LENGTH_SHORT).show();
                        return; // stop here until granted
                    }



                    Toast.makeText(this, "SMS sent!", Toast.LENGTH_SHORT).show();

                    // Save sent message to Room
                    MessageEntity sentMessage = new MessageEntity("You", selectedPhoneNumber , messageText, System.currentTimeMillis());
                    viewModel.insertMessage(sentMessage);

                    etMessage.setText(""); // clear after sending
                } catch (Exception e) {
                    Toast.makeText(this, "Send failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Please type a message first.", Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission granted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS permission denied. Cannot send messages.", Toast.LENGTH_LONG).show();
            }
        }
    }

}