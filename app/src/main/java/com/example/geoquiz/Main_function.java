
package com.example.geoquiz;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.geoquiz.adapters.RiderHistoryAdapter;
import com.example.geoquiz.adapters.RiderInfo;

import java.util.ArrayList;
import java.util.List;

public class Main_function extends AppCompatActivity {
    private RecyclerView rvChatMessages;
    private RiderHistoryAdapter adapter;
    private List<RiderInfo> riderList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize RecyclerView
        rvChatMessages = findViewById(R.id.rvChatMessages);
        rvChatMessages.setLayoutManager(new LinearLayoutManager(this));

        // Load dummy data
        riderList = new ArrayList<>();
        riderList.add(new RiderInfo("Alice", "Hi, can we meet at 5?", true, R.drawable.ic_profile_placeholder));
        riderList.add(new RiderInfo("Bob", "I'm on my way.", false, R.drawable.ic_profile_placeholder));
        riderList.add(new RiderInfo("Charlie", "Ride confirmed!", true, R.drawable.ic_profile_placeholder));

        // Create and set adapter
        adapter = new RiderHistoryAdapter(this, riderList);
        rvChatMessages.setAdapter(adapter);



    }
}