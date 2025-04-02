package com.example.geoquiz;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geoquiz.domain.RiderHistoryAdapter;
import com.example.geoquiz.domain.model.RiderInfo;

import java.util.ArrayList;
import java.util.List;

public class chatDetailActivity extends AppCompatActivity {

    private List<RiderInfo> messages;
    private RiderHistoryAdapter adapter;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        TextView tvChatWith = findViewById(R.id.tvChatWith);
        RecyclerView rvChatDetail = findViewById(R.id.rvChatDetail);
        EditText etChatMessage = findViewById(R.id.etChatMessage);
        Button btnSendChat = findViewById(R.id.btnSendChat);

        // 🔄 Get rider name from Intent
        String riderName = getIntent().getStringExtra("riderName");
        tvChatWith.setText(getString(R.string.chat_with) + riderName);

        // 🗨️ Load fake messages for demo
        messages = new ArrayList<>();
        messages.add(new RiderInfo(riderName, "Hi, how can I help?", true, R.drawable.ic_profile_placeholder,"012345678"));

        adapter = new RiderHistoryAdapter(this, messages, riderInfo -> { });
        rvChatDetail.setLayoutManager(new LinearLayoutManager(this));
        rvChatDetail.setAdapter(adapter);

        // Send chat
        btnSendChat.setOnClickListener(v -> {
            String msg = etChatMessage.getText().toString().trim();
            if (!msg.isEmpty()) {
        messages.add(new RiderInfo("You", msg, false, R.drawable.ic_profile_placeholder,
                "012345678"));
                adapter.notifyItemInserted(messages.size() - 1);
                rvChatDetail.scrollToPosition(messages.size() - 1);
                etChatMessage.setText("");
            }
        });



    }


}
