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
 * ����������
 *
 * @author lloyd
 */
public class BluetoothController {
    private static Context mContext;
    private static Handler mHandler;
    private String deviceAddress;
    private String deviceName;

    private BluetoothAdapter bleAdapter;
    private Handler serviceHandler;// ������

    static BluetoothGatt bleGatt;// ����
    static BluetoothGattCharacteristic bleGattCharacteristic;

    /**
     * ����ģʽ
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
     * ��ʼ������
     *
     * @return
     */
    public boolean initBLE() {
        // ��鵱ǰ�ֻ��Ƿ�֧��ble ����,�����֧���˳�����
        // App.app���ܻᱨ���嵥�ļ��в�Ҫ��������application
        if (!mContext.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            return false;
        }
        // ��ʼ�� Bluetooth adapter, ͨ�������������õ�һ���ο�����������(API����������android4.3�����ϰ汾)
        final BluetoothManager bluetoothManager = (BluetoothManager) mContext
                .getSystemService(Context.BLUETOOTH_SERVICE);
        bleAdapter = bluetoothManager.getAdapter();
        // ����豸���Ƿ�֧������
        return bleAdapter != null;
    }

    /**
     * ���������ص�
     */
    BluetoothAdapter.LeScanCallback bleScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] arg2) {
            Log.d("###", "search device" + rssi +"::"+ arg2);
            // device�������������豸
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
     * ɨ������
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
     * �Ƿ�������
     *
     * @return
     */
    public boolean isBleOpen() {
        return bleAdapter.isEnabled();
    }

    /**
     * ���������豸
     *
     * @param device �����ӵ��豸
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
     * ������ͨ�Żص�
     */
    public BluetoothGattCallback bleGattCallback = new BluetoothGattCallback() {
        /**
         * �յ���Ϣ
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
            // Ҳ�����ȴ�ӡ��������
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
         * ����״̬�ı�
         */
        @Override
        public void onConnectionStateChange(
                BluetoothGatt paramAnonymousBluetoothGatt, int oldStatus,
                int newStatus) {
            Log.d("###", "blue state is onConnectionStateChange " + newStatus);
            if (newStatus == 2)// ������״̬���������ӳɹ�
            {
                Bundle bundle = new Bundle();
                bundle.putString("address", deviceAddress);
                bundle.putString("name", deviceName);
                Intent intentDevice = new Intent(
                        ConstantUtils.ACTION_CONNECTED_ONE_DEVICE);
                intentDevice.putExtras(bundle);
                mContext.sendBroadcast(intentDevice);
                paramAnonymousBluetoothGatt.discoverServices();
                // ���ӵ���������ҿ��Զ�д�ķ��������кܶ����
                return;
            }
            if (newStatus == 0)// �Ͽ����ӻ�δ���ӳɹ�
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
     * ��������
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
     * ��������
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
     * ��������
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
