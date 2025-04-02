package com.example.geoquiz.presentation.feature_chat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geoquiz.R;
import com.example.geoquiz.data.local.database.MessageEntity;
import com.example.geoquiz.presentation.adapter.ChatMessageAdapter;


import java.util.List;

/**
 * Displays chat messages filtered by a specific contact (rider).
 * Messages are loaded via ViewModel and shown in threaded format.
 */
public class chatDetailActivity extends AppCompatActivity {

    private ChatMessageAdapter adapter;

    private String contactPhone;
    private ChatDetailViewModel viewModel;


    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        // 🔌 UI bindings
        TextView tvChatWith = findViewById(R.id.tvChatWith);
        EditText etChatMessage = findViewById(R.id.etChatMessage);
        Button btnSendChat = findViewById(R.id.btnSendChat);
        RecyclerView rvChatDetail = findViewById(R.id.rvChatDetail);


        // 🔄 Get rider name from Intent
        // 🧠 Get rider data
        String riderName = getIntent().getStringExtra("riderName");
        contactPhone = getIntent().getStringExtra("riderPhone");

        tvChatWith.setText(getString(R.string.chat_with, riderName));



        // ♻️ Adapter setup
        adapter = new ChatMessageAdapter();
        rvChatDetail.setLayoutManager(new LinearLayoutManager(this));
        rvChatDetail.setAdapter(adapter);

        // 🧠 ViewModel
        viewModel = new ViewModelProvider(this).get(ChatDetailViewModel.class);

        viewModel.getMessagesForContact(contactPhone).observe(this, messages -> {

            adapter.submitList(messages);
            rvChatDetail.scrollToPosition(messages.size() - 1);
        });


        // Send chat
        btnSendChat.setOnClickListener(v -> {
            String msg = etChatMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(msg)) {
                viewModel.sendMessage("You", contactPhone, msg);
                etChatMessage.setText("");
            }
        });



    }


}
