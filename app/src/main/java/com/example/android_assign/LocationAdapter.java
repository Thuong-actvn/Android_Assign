package com.example.android_assign;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

final class LocationAdapter extends BaseAdapter {
    private final LayoutInflater inflater;
    private final List<LocationRecord> records;

    LocationAdapter(Context context, List<LocationRecord> records) {
        inflater = LayoutInflater.from(context);
        this.records = records;
    }

    @Override
    public int getCount() {
        return records.size();
    }

    @Override
    public LocationRecord getItem(int position) {
        return records.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_location, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        LocationRecord record = getItem(position);
        holder.coordinateText.setText(parent.getContext().getString(
                R.string.coordinate_format, record.latitude, record.longitude));
        holder.addressText.setText(parent.getContext().getString(
                R.string.address_format, record.address));
        holder.timeText.setText(parent.getContext().getString(
                R.string.time_format, record.time));
        return convertView;
    }

    private static final class ViewHolder {
        final TextView coordinateText;
        final TextView addressText;
        final TextView timeText;

        ViewHolder(View view) {
            coordinateText = view.findViewById(R.id.coordinateText);
            addressText = view.findViewById(R.id.addressText);
            timeText = view.findViewById(R.id.timeText);
        }
    }
}
