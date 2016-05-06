package com.example.jk.bluetoothapp;


import android.Manifest;
import android.app.Activity;


import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;

import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.lzk.bluetooth.BLEManager;
import com.lzk.bluetooth.CharacteristicActivity;
import com.lzk.bluetooth.ScanActivity;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.utils.ConnectionSharingAdapter;


import java.util.Timer;
import java.util.TimerTask;


import bluetooth.AddressModel;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

import static com.trello.rxlifecycle.ActivityEvent.DESTROY;
import static com.trello.rxlifecycle.ActivityEvent.PAUSE;


public abstract class AMainActivity extends AAMainActivity {

    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;

    private String username, usernumber,locationmsg;
    public String usermessage;
    protected int timecount = 0;
    protected int receivecount = 0;
    protected int property = 0x00;
    public boolean isSendMessage=false;

    //蓝牙相关
    private RxBleDevice bleDevice;
    private BLEManager bleManager;
    protected Observable<RxBleConnection> connectionObservable;
    private PublishSubject<Void> disconnectTriggerSubject = PublishSubject.create();
//    protected  UUID characteristicUuid;
    protected AddressModel addressModel;


    // 定位相关声明
    public LocationClient locationClient = null;
    private SDKReceiver mReceiver;

    //定时器
    private Timer mTimer;
    private TimerTask mTimerTask;

    //权限相关
    protected PermissionManager permissionManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionManager=PermissionManager.getInstance(AMainActivity.this);

        bleManager=BLEManager.getInstance(getApplication());
        new Thread(){
            public void run(){
                while(!bleManager.isBluetoothOpen()){
                    bleManager.enableBluetooth();
                }
            }
        }.start();

        addressModel=getAddrConfig();
        if(!addressModel.isNULL()){
            bleDevice = bleManager.getRxBleClient().getBleDevice(addressModel.getMacAddress());
            // How to listen for connection state changes
            bleDevice.observeConnectionStateChanges()
                    .compose(bindUntilEvent(DESTROY))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onConnectionStateChange);
            connectionObservable = bleDevice
                    .establishConnection(this, false)
                    .takeUntil(disconnectTriggerSubject)
                    .compose(bindUntilEvent(PAUSE))
                    .doOnUnsubscribe(this::clearSubscription)
                    .compose(new ConnectionSharingAdapter());
        }

        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        mReceiver = new SDKReceiver();
        registerReceiver(mReceiver, iFilter);
        locationClient = new LocationClient(getApplicationContext()); // 实例化LocationClient类
        locationClient.registerLocationListener(myListener); // 注册监听函数
        setLocationOption();    //设置定位参数
        locationClient.start(); // 开始定位

       // task = new MyTimerTask();
       // timer = new Timer();
        /* 表示0毫秒之後，每隔2000毫秒執行一次 */
       // timer.schedule(task, 0, 2000);



    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("lzk", "destroy");
        stopTimer();
        //退出时销毁定位
        unregisterReceiver(mReceiver);
        locationClient.stop();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                if (resultCode == Activity.RESULT_OK) {
                    ContentResolver reContentResolverol = getContentResolver();
                    Uri contactData = data.getData();
                    @SuppressWarnings("deprecation")
                    Cursor cursor = managedQuery(contactData, null, null, null, null);
                    cursor.moveToFirst();
                    username = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    Cursor phone = reContentResolverol.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                            null,
                            null);
                    while (phone.moveToNext()) {
                        usernumber = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    }
                } else {
                    showToast("not select");
                }
                showContactor(username, usernumber);
                break;

            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    addressModel.setMacAddress(data.getStringExtra(ScanActivity.EXTRA_MAC_ADDRESS));
                    addressModel.setCharacteristicUuid(data.getStringExtra(CharacteristicActivity.EXTRA_CHARACTERISTIC_UUID));

                    Log.i("lzk","macAddress: "+ addressModel.getMacAddress());
                    Log.i("lzk","characteristicUuid :"+addressModel.getCharacteristicUuid() );
                    saveAddrConfig(addressModel);

                    bleDevice = bleManager.getRxBleClient().getBleDevice(addressModel.getMacAddress());

                    // How to listen for connection state changes
                    bleDevice.observeConnectionStateChanges()
                            .compose(bindUntilEvent(DESTROY))
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this::onConnectionStateChange);

                    connectionObservable = bleDevice
                            .establishConnection(this, false)
                            .takeUntil(disconnectTriggerSubject)
                            .compose(bindUntilEvent(PAUSE))
                            .doOnUnsubscribe(this::clearSubscription)
                            .compose(new ConnectionSharingAdapter());
                    updateUI(isConnected());
                }
                else {
                    Log.i("lzk","not ble");
                }
                break;

            default:
                break;
        }





    }

    protected boolean isConnected() {
        return bleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED;
    }

    private void onConnectionStateChange(RxBleConnection.RxBleConnectionState newState){
        updateUI(isConnected());
    }


    void triggerDisconnect() {
        disconnectTriggerSubject.onNext(null);
    }

    private void clearSubscription() {
        connectionObservable = null;
        updateUI(isConnected());
    }


    /**
     * 设置定位参数
     */
    private void setLocationOption() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        locationClient.setLocOption(option);
    }



    //选择蓝牙模块
    public void SelectBluetooth() {

        if(bleManager.isBluetoothOpen()){
            final Intent intent = new Intent(this, ScanActivity.class);
            //serverIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(intent, 1);
        }
        else {
            showToast("wait until bluetooth enable");
            new Thread(){
                public void run(){
                    while(!bleManager.isBluetoothOpen()){
                        bleManager.enableBluetooth();
                    }
                }
            }.start();
        }




    }



    public void AutoDialog() {

        if (usernumber != null && !usernumber.equals("") &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) ==
                        PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + usernumber));
            AMainActivity.this.startActivity(intent);
            }
        else {
            showToast("Please select contactor");
        }
    }


    public void sendTextMsg() {
        String SENT = "sms_sent";
        String DELIVERED = "sms_delivered";

        if(usernumber==null || usernumber.equals("") ||
                usermessage==null || usermessage.equals("") ||
                locationmsg==null || locationmsg.equals("")){
            showToast("号码或者信息不正确");
            Log.i("lzk", "send ssm msg");
            return;
        }

        Log.i("lzk","send ssm msg");

        PendingIntent sentPI = PendingIntent.getActivity(this, 0, new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getActivity(this, 0, new Intent(DELIVERED), 0);

        registerReceiver(new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {
                switch(getResultCode())
                {
                    case Activity.RESULT_OK:
                        Log.i("lzk", "Activity.RESULT_OK");
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Log.i("lzk", "RESULT_ERROR_GENERIC_FAILURE");
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Log.i("lzk", "RESULT_ERROR_NO_SERVICE");
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Log.i("lzk", "RESULT_ERROR_NULL_PDU");
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Log.i("lzk", "RESULT_ERROR_RADIO_OFF");
                        break;
                }
            }
        }, new IntentFilter(SENT));

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Log.i("====>", "RESULT_OK");
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i("=====>", "RESULT_CANCELED");
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager smsm = SmsManager.getDefault();
        smsm.sendTextMessage(usernumber, null, usermessage+locationmsg, sentPI, deliveredPI);
    }


    public class SDKReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR))
            {
                Log.i("lzk","fail----");
                // key 验证失败，相应处理
            }else {
                Log.i("lzk","success----");
            }

        }
    }



    public BDLocationListener myListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            //Receive Location
            int type=location.getLocType();
           // Log.i("lzk", "type" + type);
            if(type==BDLocation.TypeGpsLocation || type==BDLocation.TypeNetWorkLocation ||
                    type==BDLocation.TypeOffLineLocation){
                locationmsg="http://api.map.baidu.com/marker?location=" +
                        location.getLatitude() + "," + location.getLongitude() +
                        "&title=我的位置&content=百度奎科大厦" +
                        "&output=html&src=yourComponyName|yourAppName";

                updateLocation("Address:"+location.getAddrStr());
            }
            else {
                locationmsg=null;
                updateLocation("not located");
            }



        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
            {
                showLog("22");
                showToast("get Permission!");
            }
            break;
            default:
                showLog("33");
                showToast("quit");
                break;

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private class MyTimerTask extends TimerTask    {
        @Override
        public void run()
        {
            timecount++;
            if(timecount>=3){
                if(receivecount>2 ||
                        (receivecount==1 && property==1)){
                    Log.i("lzk","many");
                    showToast("a lot touch");
                    //diag number
                    if(isSendMessage)
                        sendTextMsg();
                    AutoDialog();
                }
                else if(receivecount==2){
                    Log.i("lzk","2222");
                    showToast("two touch");
                    //send msg
                    sendTextMsg();
                }
                else {
                    showToast("other situation");
                }
                stopUITimer();
            }

        }
    };

    protected void showLog(String s) {
        Log.i("lzk",s);
    }

    protected void stopTimer(){
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        receivecount=0;
        timecount=0;
    }

    protected void startTimer(){
        if (mTimer == null) {
            mTimer = new Timer();
        }
        if (mTimerTask == null) {
            mTimerTask = new  MyTimerTask();
        }

        if(mTimer != null && mTimerTask != null )
            mTimer.schedule(mTimerTask, 0, 1000);

    }

    protected abstract void updateLocation(String address);
    protected abstract void showContactor(String username,String usernumber);
    protected abstract void showToast(String text);
    protected abstract void onConnectionFailure(Throwable throwable);
    protected abstract void updateUI(boolean isconnect);
    protected abstract void stopUITimer();

}
	

