package com.example.jk.bluetoothapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.util.UUID;

import bluetooth.AddressModel;
import bluetooth.Constant;

/**
 * Created by jk on 2016/5/6 0006.
 */
public class AAMainActivity extends RxAppCompatActivity {

    protected Context context = null;
    protected SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        preferences = getSharedPreferences(Constant.ADDRESS_SET, Activity.MODE_PRIVATE);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //得到macaddress和uuid
    public AddressModel getAddrConfig() {
        AddressModel addressModel = new AddressModel();
        addressModel.setMacAddress(preferences.getString(Constant.MACADDRESS,null));
        addressModel.setCharacteristicUuid(preferences.getString(Constant.UUIDADDRESS,null));
        return addressModel;
    }
    public void saveAddrConfig(AddressModel addressModel) {
        preferences.edit()
                .putString(Constant.MACADDRESS, addressModel.getMacAddress())
                .commit();
        preferences.edit()
                .putString(Constant.UUIDADDRESS, addressModel.getCharacteristicUuid())
                .commit();
    }


}
