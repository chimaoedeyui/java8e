package com.example.jk.bluetoothapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lzk.bluetooth.HexString;
import com.polidea.rxandroidble.RxBleConnection;

import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;


public class MainActivity extends AMainActivity {
    @Bind(R.id.scanbtn)
    Button scanbtn;
    @Bind(R.id.connectbtn)
    Button connectbtn;
    @Bind(R.id.bluetoothstatus)
    TextView connecttv;
    @Bind(R.id.property)
    TextView propertytv;
    @Bind(R.id.write)
    Button sendbtn;
    @Bind(R.id.namebtn)
    Button addContactor;
    @Bind(R.id.contactor)
    TextView contactortv;
    @Bind(R.id.checkMessage)
    CheckBox CheckMessage;
    @Bind(R.id.messageinfo)
    EditText messageEdit;
    @Bind(R.id.savemeaasge)
    Button saveMeaasge;
    @Bind(R.id.location)
    TextView locationtv;
    @Bind(R.id.read)
    Button read;
    @Bind(R.id.notify)
    Button notify;
    @Bind(R.id.write)
    Button write;


    @OnClick(R.id.scanbtn)
    public void onScanClick() {
        SelectBluetooth();
    }

    @OnClick(R.id.connectbtn)
    public void onConnectClick() {

        if (addressModel.isNULL()) {
            showToast("Please scan to select the target!");
        } else {
            if (isConnected()) {
                triggerDisconnect();
            } else {
                connectionObservable.subscribe(this::onConnectionReceived, this::onConnectionFailure);
            }

            updateUI(isConnected());
        }

    }

    @OnClick(R.id.read)
    public void onSendClick() {
        if (isConnected()) {
            connectionObservable
                    .flatMap(rxBleConnection -> rxBleConnection.readCharacteristic(UUID.fromString(addressModel.getCharacteristicUuid())))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bytes -> {
                        propertytv.setText(new String(bytes) + "   " + HexString.bytesToHex(bytes));
                        Log.i("lzk", "lzk:" + new String(bytes) + ":" + HexString.bytesToHex(bytes));



                    }, this::onReadFailure);
        }
    }

    @OnClick(R.id.write)
    public void onWriteClick() {

        if (isConnected()) {
            connectionObservable
                    .flatMap(rxBleConnection -> rxBleConnection.writeCharacteristic(UUID.fromString(addressModel.getCharacteristicUuid()), new byte[]{(byte) (0xff & 0x01)}))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bytes -> {
                        onWriteSuccess();
                    }, this::onWriteFailure);
        }
    }

    @OnClick(R.id.notify)
    public void onNotifyClick() {

        if (isConnected()) {
            connectionObservable
                    .flatMap(rxBleConnection -> rxBleConnection.setupNotification(UUID.fromString(addressModel.getCharacteristicUuid())))
                    .doOnNext(notificationObservable -> runOnUiThread(this::notificationHasBeenSetUp))
                    .flatMap(notificationObservable -> notificationObservable)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onNotificationReceived, this::onNotificationSetupFailure);
        }
    }

    @OnCheckedChanged(R.id.checkMessage)
    public void onCheckClick(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {

            case R.id.checkMessage:
                if (isChecked) {
                    isSendMessage = true;
                    Toast.makeText(getBaseContext(), "CheckBox 01 check ", Toast.LENGTH_SHORT).show();
                } else {
                    isSendMessage = false;
                    Toast.makeText(getBaseContext(), "CheckBox 01 ucheck ", Toast.LENGTH_SHORT).show();
                }

        }
    }

    @OnClick(R.id.savemeaasge)
    public void onSaveClick() {
        if (saveMeaasge.getText().toString().equals("保存")) {
            saveMeaasge.setText("编辑");
            usermessage = messageEdit.getText().toString();
            messageEdit.setFocusable(false);
            messageEdit.setFocusableInTouchMode(false);
        } else {
            messageEdit.setFocusableInTouchMode(true);
            messageEdit.setFocusable(true);
            messageEdit.requestFocus();
            saveMeaasge.setText("保存");
        }
    }


    private Handler updatehandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //自动连接
        if (!addressModel.isNULL()) {
            connectbtn.setEnabled(true);
        }


        propertytv.setText(" " + property + " ");
        contactortv.setOnClickListener(contactorlistener);
        addContactor.setOnClickListener(contactorlistener);
        messageEdit.setFocusable(false);
        messageEdit.setFocusableInTouchMode(false);
        usermessage = messageEdit.getText().toString();
        locationtv.setText("not located");

        updatehandler = new myHandler();
        int kk = permissionManager.init();
        showLog("length:" + kk);
        permissionManager.checkpermission();

    }

    private View.OnClickListener contactorlistener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivityForResult(new Intent(
                    Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), 0);
        }
    };

    @Override
    protected void updateLocation(String address) {
//        Message message = new Message();
//        message.what = 3;
//        message.obj=address;
//        updatehandler.sendMessage(message);
        locationtv.setText(address);

    }

    @Override
    protected void showContactor(String username, String usernumber) {
        if (usernumber != null && !usernumber.equals("") &&
                username != null && !username.equals(""))
            contactortv.setText(usernumber + " (" + username + ")");
    }

    @Override
    protected void showToast(String text) {
        Message message = new Message();
        message.what = 5;
        message.obj = text;
        updatehandler.sendMessage(message);
    }

    @Override
    protected void stopUITimer() {
        Message message = new Message();
        message.what = 4;
        updatehandler.sendMessage(message);
    }

    private void onReadFailure(Throwable throwable) {

        showToast("Read error: " + throwable);
    }

    private void onWriteSuccess() {
        //noinspection ConstantConditions
        // Snackbar.make(findViewById(R.id.main), "Write success", Snackbar.LENGTH_SHORT).show();
        showToast("Write success");
    }

    private void onWriteFailure(Throwable throwable) {
        //noinspection ConstantConditions
        // Snackbar.make(findViewById(R.id.main), "Write error: " + throwable, Snackbar.LENGTH_SHORT).show();
        showToast("Write error: " + throwable);
    }

    private void onNotificationReceived(byte[] bytes) {
        //noinspection ConstantConditions
        // Snackbar.make(findViewById(R.id.main), "Change: " + HexString.bytesToHex(bytes), Snackbar.LENGTH_SHORT).show();
        showToast("Change: " + HexString.bytesToHex(bytes));
        property=bytes[0] & 0xFF;
        propertytv.setText(" " + property + " ");
        if( property!=0){
            if(receivecount==0){
                startTimer();
            }
            receivecount=receivecount+1;
        }
    }

    private void onNotificationSetupFailure(Throwable throwable) {
        //noinspection ConstantConditions
        //  Snackbar.make(findViewById(R.id.main), "Notifications error: " + throwable, Snackbar.LENGTH_SHORT).show();
        showToast("Notifications error: " + throwable);
    }

    private void notificationHasBeenSetUp() {
        //noinspection ConstantConditions
        //Snackbar.make(findViewById(R.id.main), "Notifications has been set up", Snackbar.LENGTH_SHORT).show();
        showToast("Notifications has been set up");
    }

    private void onConnectionReceived(RxBleConnection connection) {
            updateUI(isConnected());
    }

    @Override
    protected void onConnectionFailure(Throwable throwable) {
        // connectbtn.setEnabled(false);
        connectbtn.setText("connect");
        connecttv.setText("disconnect because " + throwable);
    }

    @Override
    protected void updateUI(boolean isconnect) {
        // connectbtn.setEnabled(!isconnect);
        connectbtn.setText(isconnect ? "disconnect" : "connect");
        connecttv.setText(isconnect ? "disconnect" : "connect");
        read.setEnabled(isConnected());
        write.setEnabled(isConnected());
        notify.setEnabled(isConnected());

    }


    private class myHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
//                case 1:
//
//                    int[] readdata = (int[]) msg.obj;
//                    // readdata[0]为发送字母的ascii码
//                    property = readdata[0];
//                    propertytv.setText(" " + property + " ");
//                    break;
//                case 2:
//                    boolean status = (boolean) msg.obj;
//                    if (status) {
//                        connecttv.setText("Connected");
//                    } else {
//                        connecttv.setText("Not Connected");
//                    }
//                    break;
//                case 3:
//                    String address = (String) msg.obj;
//                    locationtv.setText(address);
//                    break;
                case 4:
                    stopTimer();
                    break;

                case 5:
                    String text = (String) msg.obj;
                    Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
                    break;


                default:
                    break;
            }
            super.handleMessage(msg);
        }

    }

}
