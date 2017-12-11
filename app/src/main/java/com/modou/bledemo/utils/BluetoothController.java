package com.modou.bledemo.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.MutableChar;

import com.modou.bledemo.entity.EntityDevice;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

/**
 * 蓝牙控制类
 *
 * @author lloyd
 */
public class BluetoothController {
    private static Context mContext;
    private static Handler mHandler;
    private String deviceAddress;
    private String deviceName;

    private BluetoothAdapter bleAdapter;
    private Handler serviceHandler;// 服务句柄

    static BluetoothGatt bleGatt;// 连接
    static BluetoothGattCharacteristic bleGattCharacteristic;

    /**
     * 单例模式
     */
    private static BluetoothController instance = null;
    private BluetoothGattService localBluetoothGattService;
    private boolean mScanning;
    private static long SCAN_PERIOD = 10000;

    private BluetoothController() {
    }

    public static BluetoothController getInstance(Context context) {
        mContext = context;
        mHandler = new Handler();
        if (instance == null) {
            instance = new BluetoothController();
        }
        return instance;
    }

    /**
     * 初始化蓝牙
     *
     * @return
     */
    public boolean initBLE() {
        // 检查当前手机是否支持ble 蓝牙,如果不支持退出程序
        // App.app可能会报错，清单文件中不要忘了配置application
        if (!mContext.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            return false;
        }
        // 初始化 Bluetooth adapter, 通过蓝牙管理器得到一个参考蓝牙适配器(API必须在以上android4.3或以上版本)
        final BluetoothManager bluetoothManager = (BluetoothManager) mContext
                .getSystemService(Context.BLUETOOTH_SERVICE);
        bleAdapter = bluetoothManager.getAdapter();
        // 检查设备上是否支持蓝牙
        return bleAdapter != null;
    }

    /**
     * 搜索蓝牙回调
     */
    BluetoothAdapter.LeScanCallback bleScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] arg2) {
            Log.d("###", "search device" + rssi +"::"+ arg2);
            // device就是搜索到的设备
            String name = device.getName();
            if (name == null||name.equals("")) {
                return;
            }else{
                Intent intent = new Intent(
                        ConstantUtils.ACTION_UPDATE_DEVICE_LIST);
                intent.putExtra("name", device.getName());
                intent.putExtra("address", device.getAddress());
                intent.putExtra("rssi",rssi);
                mContext.sendBroadcast(intent);
            }
        }
    };

    /**
     * 扫描蓝牙
     */
    public void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    Log.d("###","stop scan ");
                    bleAdapter.stopLeScan(bleScanCallback);
                    Intent stopConnect = new Intent(
                            ConstantUtils.ACTION_STOP_SCAN);
                    mContext.sendBroadcast(stopConnect);
                }
            }, SCAN_PERIOD);
            mScanning = true;
            bleAdapter.startLeScan(bleScanCallback);
        } else {
            mScanning = false;
            bleAdapter.stopLeScan(bleScanCallback);
        }
    }

    /**
     * 是否蓝牙打开
     *
     * @return
     */
    public boolean isBleOpen() {
        return bleAdapter.isEnabled();
    }

    /**
     * 连接蓝牙设备
     *
     * @param device 待连接的设备
     */
    public void connect(EntityDevice device) {
        deviceAddress = device.getAddress();
        deviceName = device.getName();
        final BluetoothDevice localBluetoothDevice = bleAdapter
                .getRemoteDevice(device.getAddress());
        disconnect();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                bleGatt = localBluetoothDevice.connectGatt(mContext, false,
                        bleGattCallback);
            }
        }, 250);
    }

    public void disconnect() {
        if (bleGatt != null) {
            bleGatt.disconnect();
            bleGatt.close();
            bleGatt = null;
        }
    }

    /**
     * 与蓝牙通信回调
     */
    public BluetoothGattCallback bleGattCallback = new BluetoothGattCallback() {
        /**
         * 收到消息
         */
        @Override
        public void onCharacteristicChanged(
                BluetoothGatt paramAnonymousBluetoothGatt,
                BluetoothGattCharacteristic paramAnonymousBluetoothGattCharacteristic) {
            Log.d("###", "blue state is onCharacteristicChanged");
            byte[] arrayOfByte = paramAnonymousBluetoothGattCharacteristic
                    .getValue();
            String msgs = null;
            try {
                msgs = new String(arrayOfByte, "gb2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Intent mesDevice = new Intent(
                    ConstantUtils.ACTION_RECEIVE_MESSAGE_FROM_DEVICE);
            mesDevice.putExtra("message", msgs);
            mContext.sendBroadcast(mesDevice);
            // 也可以先打印出来看看
            Log.i("TEST",
                    ConvertUtils.getInstance().bytesToHexString(arrayOfByte));
        }

        @Override
        public void onCharacteristicRead(

                BluetoothGatt paramAnonymousBluetoothGatt,
                BluetoothGattCharacteristic paramAnonymousBluetoothGattCharacteristic,
                int paramAnonymousInt) {

        }

        @Override
        public void onCharacteristicWrite(
                BluetoothGatt paramAnonymousBluetoothGatt,
                BluetoothGattCharacteristic paramAnonymousBluetoothGattCharacteristic,
                int paramAnonymousInt) {
        }

        /**
         * 连接状态改变
         */
        @Override
        public void onConnectionStateChange(
                BluetoothGatt paramAnonymousBluetoothGatt, int oldStatus,
                int newStatus) {
            Log.d("###", "blue state is onConnectionStateChange " + newStatus);
            if (newStatus == 2)// 已连接状态，表明连接成功
            {
                Bundle bundle = new Bundle();
                bundle.putString("address", deviceAddress);
                bundle.putString("name", deviceName);
                Intent intentDevice = new Intent(
                        ConstantUtils.ACTION_CONNECTED_ONE_DEVICE);
                intentDevice.putExtras(bundle);
                mContext.sendBroadcast(intentDevice);
                paramAnonymousBluetoothGatt.discoverServices();
                // 连接到蓝牙后查找可以读写的服务，蓝牙有很多服务
                return;
            }
            if (newStatus == 0)// 断开连接或未连接成功
            {
                Intent stopConnect = new Intent(
                        ConstantUtils.ACTION_STOP_CONNECT);
                mContext.sendBroadcast(stopConnect);
                return;
            }
            paramAnonymousBluetoothGatt.disconnect();
            paramAnonymousBluetoothGatt.close();
            return;
        }

        @Override
        public void onDescriptorRead(BluetoothGatt paramAnonymousBluetoothGatt,
                                     BluetoothGattDescriptor paramAnonymousBluetoothGattDescriptor,
                                     int paramAnonymousInt) {
            Log.d("###", "blue state is onDescriptorRead ");

        }

        @Override
        public void onDescriptorWrite(
                BluetoothGatt paramAnonymousBluetoothGatt,
                BluetoothGattDescriptor paramAnonymousBluetoothGattDescriptor,
                int paramAnonymousInt) {
            Log.d("###", "blue state is onDescriptorWrite ");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt paramAnonymousBluetoothGatt,
                                     int paramAnonymousInt1, int paramAnonymousInt2) {
            Log.d("###", "blue state is onReadRemoteRssi ");
        }

        @Override
        public void onReliableWriteCompleted(
                BluetoothGatt paramAnonymousBluetoothGatt, int paramAnonymousInt) {
            Log.d("###", "blue state is onReliableWriteCompleted ");
        }

        @Override
        public void onServicesDiscovered(
                BluetoothGatt paramAnonymousBluetoothGatt, int paramAnonymousInt) {
            Log.d("###", "blue state is onServicesDiscovered ");
            BluetoothController.this.findService(paramAnonymousBluetoothGatt
                    .getServices());

        }

    };

    /**
     * 传输数据
     *
     * @param byteArray
     * @return
     */
    public boolean write(byte byteArray[]) {
        if (bleGattCharacteristic == null) {
            return false;
        }
        if (bleGatt == null) {
            return false;
        }
        bleGattCharacteristic.setValue(byteArray);
        return bleGatt.writeCharacteristic(bleGattCharacteristic);
    }

    /**
     * 传输数据
     *
     * @param str
     * @return
     */
    public boolean write(String str) {
        if (bleGattCharacteristic == null) {
            return false;
        }
        if (bleGatt == null) {
            return false;
        }
        bleGattCharacteristic.setValue(str);
        return bleGatt.writeCharacteristic(bleGattCharacteristic);
    }

    public boolean read() {
        return bleGatt.readCharacteristic(bleGattCharacteristic);
    }

    /**
     * 搜索服务
     *
     * @param paramList
     */
    public void findService(List<BluetoothGattService> paramList) {

        Iterator localIterator1 = paramList.iterator();
        while (localIterator1.hasNext()) {
            localBluetoothGattService = (BluetoothGattService) localIterator1
                    .next();
            if (localBluetoothGattService.getUuid().toString()
                    .equalsIgnoreCase(ConstantUtils.UUID_SERVER)) {
                List localList = localBluetoothGattService.getCharacteristics();
                Iterator localIterator2 = localList.iterator();
                while (localIterator2.hasNext()) {
                    BluetoothGattCharacteristic localBluetoothGattCharacteristic = (BluetoothGattCharacteristic) localIterator2
                            .next();
                    if (localBluetoothGattCharacteristic.getUuid().toString()
                            .equalsIgnoreCase(ConstantUtils.UUID_NOTIFY)) {
                        bleGattCharacteristic = localBluetoothGattCharacteristic;
                        break;
                    }
                }
                break;
            }

        }
        bleGatt.setCharacteristicNotification(bleGattCharacteristic, true);
    }

}
