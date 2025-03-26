package com.example.geoquiz.adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.geoquiz.R;

import java.util.List;
public class RiderHistoryAdapter extends RecyclerView.Adapter<RiderHistoryAdapter.ViewHolder>{
    private final List<RiderInfo> riderList;
    private final Context context;

    public RiderHistoryAdapter(Context context, List<RiderInfo> riderList) {
        this.context = context;
        this.riderList = riderList;

}
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the item layout for each rider history entry
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RiderInfo rider = riderList.get(position);
        // Bind rider's name and message
        holder.tvRiderName.setText(rider.getRiderName());
        holder.tvLatestMessage.setText(rider.getLatestMessage());
        // Set the profile image using the resource id
        holder.imgProfile.setImageResource(rider.getProfileImageResId());

        // Set status sticker based on rider's availability
        if (rider.isAvailable()) {
            holder.imgStatusSticker.setImageResource(R.drawable.ic_available_status);
        } else {
            holder.imgStatusSticker.setImageResource(R.drawable.ic_unavailable_status);
        }
    }

    @Override
    public int getItemCount() {
        return riderList.size();
    }

    // ViewHolder inner class to hold references to the views for each item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProfile;
        TextView tvRiderName;
        TextView tvLatestMessage;
        ImageView imgStatusSticker;

        public ViewHolder(View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            tvRiderName = itemView.findViewById(R.id.tvRiderName);
            tvLatestMessage = itemView.findViewById(R.id.tvLatestMessage);
            imgStatusSticker = itemView.findViewById(R.id.imgStatusSticker);
        }
    }
}