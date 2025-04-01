
package com.example.geoquiz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geoquiz.adapters.RiderHistoryAdapter;
import com.example.geoquiz.adapters.RiderInfo;

import java.util.ArrayList;
import java.util.List;

public class Main_function extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize RecyclerView
        RecyclerView rvChatMessages = findViewById(R.id.rvChatMessages);
        rvChatMessages.setLayoutManager(new LinearLayoutManager(this));

        // Load dummy data
        List<RiderInfo> riderList = new ArrayList<>();
        riderList.add(new RiderInfo("Alice", "Hi, can we meet at 5?", true, R.drawable.ic_profile_placeholder));
        riderList.add(new RiderInfo("Bob", "I'm on my way.", false, R.drawable.ic_profile_placeholder));
        riderList.add(new RiderInfo("Charlie", "Ride confirmed!", true, R.drawable.ic_profile_placeholder));

        // Create and set adapter
        RiderHistoryAdapter adapter = new RiderHistoryAdapter(this, riderList);
        rvChatMessages.setAdapter(adapter);

        Button btnRequestRide = findViewById(R.id.btnRequestRide);
        btnRequestRide.setOnClickListener(v -> {
            Intent intent = new Intent(Main_function.this, RequestRideActivity.class);
            startActivity(intent);
        });

        Button btnShareLocation = findViewById(R.id.btnShareLocation);
        btnShareLocation.setOnClickListener(v -> {
            Intent intent = new Intent(Main_function.this, RiderActivity.class);
            startActivity(intent);
        });



    }
}