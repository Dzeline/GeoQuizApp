package com.example.geoquiz.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geoquiz.R;
import com.example.geoquiz.data.local.database.MessageEntity;

import java.util.List;
public class ChatMessageAdapter extends ListAdapter<MessageEntity,RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 0;
    private static final int TYPE_RECEIVED = 1;


    public ChatMessageAdapter() {
        super(DIFF_CALLBACK);
    }
    private static final DiffUtil.ItemCallback<MessageEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<MessageEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull MessageEntity oldItem, @NonNull MessageEntity newItem) {
            return oldItem.timestamp == newItem.timestamp;
        }

        @Override
        public boolean areContentsTheSame(@NonNull MessageEntity oldItem, @NonNull MessageEntity newItem) {
            return oldItem.getMessage().equals(newItem.getMessage()) &&
                    oldItem.getSender().equals(newItem.getSender());
        }
    };


    @Override
    public int getItemViewType(int position) {
        MessageEntity message = getItem(position);
        return "You".equalsIgnoreCase(message.getSender()) ? TYPE_SENT : TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageEntity message = getItem(position);

        if (message == null || message.getMessage() == null || message.getSender() == null) {
            return; // 🚫 Don't bind if data is broken
        }

        if (holder instanceof SentViewHolder) {
            ((SentViewHolder) holder).tvMessage.setText(message.getMessage());
        } else if (holder instanceof ReceivedViewHolder) {
            ((ReceivedViewHolder) holder).tvMessage.setText(message.getMessage());
        }
    }

    static class SentViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;

        SentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessageSent);
        }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;

        ReceivedViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessageReceived);
        }
    }


}
