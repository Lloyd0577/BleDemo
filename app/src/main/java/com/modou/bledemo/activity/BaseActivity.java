package com.modou.bledemo.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;


import com.modou.bledemo.R;
import com.modou.bledemo.adapter.DeviceAdapter;
import com.modou.bledemo.entity.EntityDevice;
import com.modou.bledemo.utils.BluetoothController;
import com.modou.bledemo.utils.ConstantUtils;
import com.modou.bledemo.utils.ConvertUtils;

import java.util.ArrayList;

import static com.modou.bledemo.utils.ConstantUtils.isReceiveMsg;

public class BaseActivity extends AppCompatActivity {

    protected ArrayList<EntityDevice> list = new ArrayList<>();
    protected DeviceAdapter adapter;
    private MsgReceiver receiver;
    protected String tipText;
    protected String receiveText;
    protected boolean isConnect;
    private String totalText = "";
    protected BluetoothController bleInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_base);
        adapter = new DeviceAdapter(this, list);
        registerReceiver();
        bleInstance = BluetoothController.getInstance(this);
    }

    private void registerReceiver() {
        receiver = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConstantUtils.ACTION_UPDATE_DEVICE_LIST);
        intentFilter.addAction(ConstantUtils.ACTION_CONNECTED_ONE_DEVICE);
        intentFilter.addAction(ConstantUtils.ACTION_RECEIVE_MESSAGE_FROM_DEVICE);
        intentFilter.addAction(ConstantUtils.ACTION_STOP_CONNECT);
        intentFilter.addAction(ConstantUtils.ACTION_STOP_SCAN);
        registerReceiver(receiver, intentFilter);
    }

    /**
     * 广播接收器
     */
    public class MsgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("###","receive broadcast "+intent.getAction());
            if (intent.getAction().equalsIgnoreCase(
                    ConstantUtils.ACTION_UPDATE_DEVICE_LIST)) {
                String name = intent.getStringExtra("name");
                String address = intent.getStringExtra("address");
                int rssi = intent.getIntExtra("rssi",0);
                boolean found = false;//记录该条记录是否已在list中，
                for (EntityDevice device : list) {
                    if (device.getAddress().equals(address)) {
                        device.setRssi(rssi);
                        found = true;
                        break;
                    }
                }// for
                if (!found) {
                    EntityDevice temp = new EntityDevice();
                    temp.setName(name);
                    temp.setAddress(address);
                    temp.setRssi(rssi);
                    list.add(temp);
                }
                adapter.notifyDataSetChanged();
            } else if (intent.getAction().equalsIgnoreCase(ConstantUtils.ACTION_CONNECTED_ONE_DEVICE)) {
                tipText = "已连接设备：" + intent.getStringExtra("address");
                isConnect = true;
                tipTextChange(tipText);
            } else if (intent.getAction().equalsIgnoreCase(ConstantUtils.ACTION_STOP_CONNECT)) {
                tipText = "连接失败，请重试！";
                isConnect = false;
                tipTextChange(tipText);
//                toast("连接已断开");
            } else if (intent.getAction().equalsIgnoreCase(ConstantUtils.ACTION_RECEIVE_MESSAGE_FROM_DEVICE)) {
                receiveText = intent.getStringExtra("message");
                Log.d("###", "length is " + receiveText.length());
                totalText = totalText + receiveText;
                if (receiveText.length() >= 2) {
                    if ("\r\n".equals(receiveText.substring(receiveText.length() - 2, receiveText.length()))) {
                        Log.d("###", "is end " + receiveText);
                        totalText = ConvertUtils.getTime() + " 收<--: " + totalText + "\n";
                        if (isReceiveMsg){
                            receiveTextChange(totalText);
                        }
                        totalText = "";
                    }
                }
            }else if(intent.getAction().equalsIgnoreCase(ConstantUtils.ACTION_STOP_SCAN)){
                isConnect = false;
                tipTextChange("扫描结束");

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }


    protected void tipTextChange(String text) {
    }

    protected void receiveTextChange(String text) {
    }
}
