package com.example.geoquiz.presentation.feature_chat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geoquiz.R;

import com.example.geoquiz.presentation.adapter.ChatMessageAdapter;
import com.example.geoquiz.presentation.feature_role.RoleManager;

import dagger.hilt.android.AndroidEntryPoint;


/**
 * Displays chat messages filtered by a specific contact (rider).
 * Messages are loaded via ViewModel and shown in threaded format.
 */
@AndroidEntryPoint
public class chatDetailActivity extends AppCompatActivity {
    private static final String TAG = "ChatDetailActivity";

    private ChatMessageAdapter adapter  = new ChatMessageAdapter();

    private String contactPhone;
    private chatDetailViewModel viewModel;

    private TextView tvChatWith;
    private EditText etChatMessage;
    private Button btnSendChat;
    private RecyclerView rvChatDetail;

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        // 🔌 UI bindings
        this.tvChatWith = findViewById(R.id.tvChatWith);
        this.etChatMessage = findViewById(R.id.etChatMessage);
        this.btnSendChat = findViewById(R.id.btnSendChat);
        this.rvChatDetail = findViewById(R.id.rvChatDetail);


        // 🔄 Get rider name from Intent
        // 🧠 Get rider data
         final String riderName = getIntent().getStringExtra("riderName");
         this.contactPhone =getIntent().getStringExtra("riderPhone");
        if (contactPhone == null) {
            Toast.makeText(this, "No contact phone passed", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (riderName != null) {
            this.tvChatWith.setText(getString(R.string.chat_with, riderName));
        }

        this.rvChatDetail.setLayoutManager(new LinearLayoutManager(this));
        this.rvChatDetail.setAdapter(adapter);

        // 🧠 ViewModel
        this.viewModel = new ViewModelProvider(this).get(chatDetailViewModel.class);

        this.viewModel.getMessagesForContact(this.contactPhone).observe(this, messages -> {

            this.adapter.submitList(messages);
            this.rvChatDetail.scrollToPosition(messages.size() - 1);
        });
        //Rider cannot send messages
        if (RoleManager.Role.RIDER == RoleManager.getRole(this)) {
            this.etChatMessage.setEnabled(false);
            this.etChatMessage.setHint("You can view messages only");
            this.btnSendChat.setEnabled(false);
        }

        // Send chat
        this.btnSendChat.setOnClickListener(v -> {
           final String msg = etChatMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(msg)) {
                this.viewModel.sendMessage("You", contactPhone, msg);
                this.etChatMessage.setText("");
                Log.d(TAG, "Sent message: " + msg + " to " + this.contactPhone);
            }
        });



    }


}
