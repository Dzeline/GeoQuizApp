package com.example.geoquiz.presentation.feature_map;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.geoquiz.R;
import com.example.geoquiz.data.local.database.LocationLogEntity;

import org.osmdroid.util.GeoPoint;

import java.util.List;
public class MapGridAdapter extends BaseAdapter {

    private final Context context;
    private  List<LocationLogEntity> items;
    private GeoPoint currentCenter;
    // Define grid characteristics
    private static final double GRID_GEOGRAPHIC_SPAN = 0.1; // Grid covers 0.1x0.1 degrees
    private static final int GRID_DIMENSION = 10;           // Grid is 10x10 cells

    public MapGridAdapter(final Context context, final List<LocationLogEntity> items, final GeoPoint currentCenter) {
        this.context = context;
        this.items = items;
        this.currentCenter = currentCenter;
    }

    @Override
    public int getCount() {
        return MapGridAdapter.GRID_DIMENSION * MapGridAdapter.GRID_DIMENSION;
    }

    @Override
    public Object getItem(final int position) {
        return null;
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final TextView view = (null != convertView) ? (TextView) convertView : new TextView(this.context);

        // Calculate row and column
        final int row = position / MapGridAdapter.GRID_DIMENSION;
        final int col = position % MapGridAdapter.GRID_DIMENSION;

        final int parentWidth = parent.getWidth();
        final int parentHeight = parent.getHeight();

        // Calculate cell size dynamically based on GridView's actual dimensions
        // Ensure parent dimensions are available, otherwise default or delay
        int cellSize = 50; // Default small size
        if (0 < parentWidth && 0 < parentHeight) {
            // Make cells square based on the smaller dimension of the GridView
            cellSize = Math.min(parentWidth, parentHeight) / MapGridAdapter.GRID_DIMENSION;
        }
        view.setLayoutParams(new ViewGroup.LayoutParams(cellSize, cellSize));

        // Check if this cell contains any location points
        boolean hasLocationInCell = false;
        boolean isCurrentLatestInCell = false;

        if (items != null && !items.isEmpty() && currentCenter != null) {
            // Calculate the geographic boundaries of this specific cell
            double cellGeoSize = GRID_GEOGRAPHIC_SPAN / GRID_DIMENSION; // e.g., 0.01 degrees

            // Geo-coordinates of the bottom-left origin of the entire 0.1x0.1 degree grid
            double gridOriginLat = currentCenter.getLatitude() - (GRID_GEOGRAPHIC_SPAN / 2.0);
            double gridOriginLon = currentCenter.getLongitude() - (GRID_GEOGRAPHIC_SPAN / 2.0);

            // Geo-coordinates of the bottom-left corner of the current cell
            double cellMinLat = gridOriginLat + (row * cellGeoSize);
            double cellMinLon = gridOriginLon + (col * cellGeoSize);
            double cellMaxLat = cellMinLat + cellGeoSize;
            double cellMaxLon = cellMinLon + cellGeoSize;

            LocationLogEntity latestLogInThisCell = null;

            for (LocationLogEntity log : items) {
                if (log.latitude >= cellMinLat && log.latitude < cellMaxLat &&
                        log.longitude >= cellMinLon && log.longitude < cellMaxLon) {
                    hasLocationInCell = true;
                    // Check if this log is the latest among those found *in this cell so far*
                    if (latestLogInThisCell == null || log.timestamp > latestLogInThisCell.timestamp) {
                        latestLogInThisCell = log;
                    }
                }
            }

            // Now check if the latest item found in *this cell* is also the *overall latest* item
            if (hasLocationInCell && latestLogInThisCell != null && !items.isEmpty()) {
                if (latestLogInThisCell.timestamp == items.get(0).timestamp) { // items.get(0) is assumed overall latest
                    isCurrentLatestInCell = true;
                }
            }
        }


        if (isCurrentLatestInCell) {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.current_location));
            view.setText("📍"); // Current location emoji
        } else if (hasLocationInCell) {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.history_location));
            view.setText("•"); // History location dot
        } else {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.grid_empty));
            view.setText(""); // Empty cell
        }

        view.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        view.setTextSize(Math.max(10, cellSize / 4f)); // Adjust text size relative to cell size
        // view.setGravity(Gravity.CENTER); // Alternative for centering text in TextView

        return view;
    }

    public void updateData(List<LocationLogEntity> newData, GeoPoint newCenter) {
        this.items = newData; // Should be a new list or a defensive copy
        this.currentCenter = newCenter;
        notifyDataSetChanged();
    }
}