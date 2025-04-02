package com.example.geoquiz.presentation.feature_map;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
public class MapGridAdapter extends BaseAdapter {

    private final Context context;
    private  List<String> items;

    public MapGridAdapter(Context context, List<String> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView view = new TextView(context);
        view.setText(items.get(position));
        view.setBackgroundColor(0xFFCCCCCC); // light gray grid box
        view.setHeight(60);
        view.setWidth(60);
        view.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        return view;
    }

    public void updateData(List<String> newData) {
        items.clear();
        items.addAll(newData);
        notifyDataSetChanged();
    }
}
