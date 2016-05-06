package com.lzk.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import bluetooth.BluetoothManager;
import bluetooth.FileUtils;


public class BluetoothService extends Service {

    private static final String SEND_BLUETOOTH_STATUS = "com.lzk.bluetooth.status";
    private static final String GET_BLUETOOTH_DATA = "com.lzk.bluetooth.data";
    private static final String Set_Address = "com.lzk.address";


    private ServiceBinder myBinder = new ServiceBinder();

    private BLEManager bleManager;

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // Log.i("lzk", "onUnbind(Intent intent)");
        return super.onUnbind(intent);
    }

    public void onCreate() {
        super.onCreate();
        bleManager = BLEManager.getInstance(getApplication());
        // 设置设备可以被搜索
        new Thread() {
            public void run() {
                while (!bleManager.isBluetoothOpen()) {
                    bleManager.enableBluetooth();
                }
            }
        }.start();


    }


    public void DisplayToast(String str) {
        Log.d("lzk", str);
    }


    public void startConnect() {

    }


    public void onDestroy() {

        super.onDestroy();
        Log.i("lzk", "bluetooth service destroy");
        closeBluetooth();

    }

    public void closeBluetooth() {
        bleManager.disableBluetooth();
    }



    //此方法是为了可以在Acitity中获得服务的实例
    public class ServiceBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }



    public boolean isConnect(){
        return bleManager.isConected();
    }


}




