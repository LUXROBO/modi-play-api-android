package com.luxrobo.modiplay.example.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.luxrobo.modiplay.example.R;

import java.util.ArrayList;


public class DeviceListAdapter extends BaseAdapter {

    private Context _context;
    private ArrayList<DeviceItem> _list;
    private int _layout;
    private LayoutInflater _inflater;

    public DeviceListAdapter() {
    }

    public DeviceListAdapter(Context context, int layout, ArrayList<DeviceItem> list) {
        _context = context;
        _list = list;
        _layout = layout;
        _inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return _list.size();
    }

    @Override
    public Object getItem(int position) {
        return _list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = _inflater.inflate(R.layout.item_device_search_list, parent, false);
        }

        TextView nameTextView = (TextView) convertView.findViewById(R.id.tv_name);
        TextView addressTextView = (TextView) convertView.findViewById(R.id.tv_address);

        DeviceItem listViewItem = _list.get(position);

        nameTextView.setText(listViewItem.getDeviceName().toUpperCase());
        addressTextView.setText(listViewItem.getDeviceAddress());

        return convertView;
    }
}
