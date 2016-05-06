package com.lzk.bluetooth;


import android.bluetooth.BluetoothAdapter;
import android.content.Context;

import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.internal.RxBleLog;

/**
 * Created by jk on 2016/4/25 0025.
 */
public class BLEManager {

    private static BLEManager bleManager = null;
    private static BluetoothAdapter bluetooth = null;
    private static RxBleClient rxBleClient=null;
    private boolean isConected=false;

    public boolean isConected() {
        return isConected;
    }

    public void setConected(boolean conected) {
        isConected = conected;
    }



    private Context ctx;

    private BLEManager(Context ctx) {
        bluetooth = BluetoothAdapter.getDefaultAdapter();
        this.ctx=ctx;
        rxBleClient = RxBleClient.create(ctx);
        RxBleClient.setLogLevel(RxBleLog.DEBUG);
    }

    public static BLEManager getInstance(Context ctx) {
        if (bleManager == null) {
            bleManager = new BLEManager(ctx);
        }
        return bleManager;
    }

    public BluetoothAdapter getBluetoothAdapter(){
        return bluetooth;
    }
    public RxBleClient getRxBleClient(){
        return rxBleClient;
    }

    //蓝牙是否打开
    public boolean isBluetoothOpen() {
       return bluetooth.isEnabled();
    }
    //打开蓝牙
    public void enableBluetooth() {
        if(!bluetooth.isEnabled()){
            bluetooth.enable();
        }

    }
    //关闭蓝牙
    public void disableBluetooth() {
        bluetooth.disable();
    }








}
