package com.modou.bledemo.activity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.modou.bledemo.R;


public class DeviceListActivity extends BaseActivity {
    private TextView tv_tip;
    private View line;
    private ListView listview;
    private TextView tv_title;
    private ImageView iv_back;
    private ImageView iv_icon;
    private GetDataTask task;
    private static final int REQUEST_CODE_BLUETOOTH_ON = 1313;
    private boolean isSupportBlueTooth;
    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        initView();
        initBlueTooth();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//??? API level ???????? 23(Android 6.0) ?
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
                }
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_CODE_ACCESS_COARSE_LOCATION);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    /**
     * ?????????
     */
    public void initBlueTooth() {
        isSupportBlueTooth = bleInstance.initBLE();
        if (isSupportBlueTooth) {
            if (bleInstance.isBleOpen()) {
                startScan();
            } else {
                turnOnBlueTooth();
            }
        } else {
            Toast.makeText(this, "改设备不支持蓝牙！", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * ??????????
     */
    public void startScan() {
        tv_tip.setText("正在扫描...");
        tv_tip.setVisibility(View.VISIBLE);
        line.setVisibility(View.VISIBLE);
        bleInstance.disconnect();
        list.clear();
        adapter.notifyDataSetChanged();
        task = null;
        task = new GetDataTask();
        task.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_ACCESS_COARSE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initBlueTooth();
            } else {
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    private void initView() {
        tv_tip =  findViewById(R.id.tv_tip);
        line = findViewById(R.id.line);
        tv_tip.setText("正在扫描...");
        listview =  findViewById(R.id.list_devices);
        iv_icon = findViewById(R.id.iv_icon);
        tv_title =  findViewById(R.id.tv_title);
        iv_back =  findViewById(R.id.iv_back);
        iv_icon.setVisibility(View.VISIBLE);
        tv_title.setText(R.string.title_device_list);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int index,
                                    long arg3) {
                tv_tip.setText("正在连接...");
                tv_tip.setVisibility(View.VISIBLE);
                line.setVisibility(View.VISIBLE);
                bleInstance.connect(list.get(index));
            }
        });
        iv_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initBlueTooth();
            }
        });
    }

    /**
     * ??????
     */
    public void turnOnBlueTooth() {
        Intent requestBluetoothOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        this.startActivityForResult(requestBluetoothOn, REQUEST_CODE_BLUETOOTH_ON);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_BLUETOOTH_ON) {
            switch (resultCode) {
                case Activity.RESULT_OK: {
                    startScan();
                }
                break;
                case Activity.RESULT_CANCELED: {
                    tv_tip.setText("蓝牙未开启！");
                }
                break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void tipTextChange(String text) {
        Log.d("###","text is "+text);
        tv_tip.setText(text);
        if ("扫描结束".equals(text)) {
            tv_tip.setVisibility(View.GONE);
            line.setVisibility(View.GONE);
        } else {
            tv_tip.setVisibility(View.VISIBLE);
            line.setVisibility(View.VISIBLE);
        }
        if (isConnect) {
            startActivity(new Intent(DeviceListActivity.this, BlueDebugActivity.class));
        }
    }

    @Override
    protected void receiveTextChange(String text) {
    }

    private class GetDataTask extends AsyncTask<Void, Void, String[]> {
        @Override
        protected String[] doInBackground(Void... params) {
            if (bleInstance.isBleOpen()) {
                bleInstance.scanLeDevice(true);
            }
            return null;
        }
        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);
        }
    }

    @Override
    protected void onDestroy() {
        bleInstance.disconnect();
        super.onDestroy();

    }
}
