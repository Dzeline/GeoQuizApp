
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
import java.util.List;

/**
 * MainFunctionActivity allows main chat window for users to chat with riders
 */
public class MainFunctionActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 101;

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


        if (getIntent().hasExtra("riderPhone")) {
            selectedPhoneNumber = getIntent().getStringExtra("riderPhone");
            Toast.makeText(this, "Reselected rider: " + selectedPhoneNumber, Toast.LENGTH_SHORT).show();
        }


        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        Button btnRequestRide = findViewById(R.id.btnRequestRide);
        Button btnShareLocation = findViewById(R.id.btnShareLocation);
        RecyclerView rvChatMessages = findViewById(R.id.rvChatMessages);
        rvChatMessages.setLayoutManager(new LinearLayoutManager(this));

        //Setup ViewModel
        viewModel = new ViewModelProvider(this).get(MainFunctionViewModel.class);

        //Observe messages
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

        btnRequestRide.setOnClickListener(v -> {
            if (selectedPhoneNumber == null) {
                Toast.makeText(this, "Select a rider before requesting a ride.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(MainFunctionActivity.this, RequestRideActivity.class);
            intent.putExtra("riderPhone", selectedPhoneNumber);
            startActivity(intent);
        });

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

            MessageEntity sentMessage = new MessageEntity(
                    "You", selectedPhoneNumber, messageText, System.currentTimeMillis()
            );
            viewModel.insertMessage(sentMessage);

            etMessage.setText(""); // Clear input
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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