package com.modou.bledemo.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.modou.bledemo.R;
import com.modou.bledemo.entity.EntityDevice;

import java.util.ArrayList;

/**
 * ¿∂—¿  ≈‰∆˜
 *
 * @author lloyd
 */
public class DeviceAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<EntityDevice> list;
    private LayoutInflater inflater;

    public DeviceAdapter(Context context, ArrayList<EntityDevice> list) {
        inflater = LayoutInflater.from(context);
        this.list = list;
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int arg0) {
        return list.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int arg0, View convertView, ViewGroup arg2) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_device, null);
            holder.name =  convertView.findViewById(R.id.device_name);
            holder.address =  convertView.findViewById(R.id.device_address);
            holder.rssi =  convertView.findViewById(R.id.signal);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        EntityDevice device = list.get(arg0);
        Log.d("###","device name is "+device.getName());
        holder.name.setText(device.getName());
        holder.address.setText(device.getAddress());
        holder.rssi.setText(device.getRssi()+"");
        return convertView;
    }

    class ViewHolder {
        TextView name;
        TextView address;
        TextView rssi;
    }

}
