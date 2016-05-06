package com.example.jk.bluetoothapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jk on 2016/4/20 0020.
 */
public class PermissionManager {

    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;

    private static PermissionManager permissionManager = null;
    private List<String> permissionsList, permissionsNeeded;
  //  private String[] ps={Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.CALL_PHONE,
  //          Manifest.permission.SEND_SMS,Manifest.permission.BLUETOOTH };
    private Context ctx;
    private Activity activity;


    private PermissionManager(Activity activity) {
        this.ctx=activity;
        this.activity=activity;
    }

    public static PermissionManager getInstance(Activity activity) {
        if (permissionManager == null) {
            permissionManager = new PermissionManager(activity);
        }
        return permissionManager;
    }

    public int init() {
        permissionsList = new ArrayList<String>();
        permissionsNeeded = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
            permissionsNeeded.add("GPS");
        if (!addPermission(permissionsList, Manifest.permission.CALL_PHONE))
            permissionsNeeded.add("Call");
        if (!addPermission(permissionsList, Manifest.permission.SEND_SMS))
            permissionsNeeded.add("SMS");
        if (!addPermission(permissionsList, Manifest.permission.WRITE_CONTACTS))
            permissionsNeeded.add("Write Contacts");
        if (!addPermission(permissionsList, Manifest.permission.READ_CONTACTS))
            permissionsNeeded.add("Read Contacts");
        if (!addPermission(permissionsList, Manifest.permission.BLUETOOTH))
            permissionsNeeded.add("Bluetooth");
        if (!addPermission(permissionsList, Manifest.permission.BLUETOOTH_ADMIN))
            permissionsNeeded.add("BLUETOOTH_ADMIN");
        return permissionsList.size();
    }


    public void checkpermission(){
        if (permissionsNeeded.size() > 0) {
            // Need Rationale
            String message = "You need to grant access to " + permissionsNeeded.get(0);
            for (int i = 1; i < permissionsNeeded.size(); i++)
                message = message + ", " + permissionsNeeded.get(i);
            showMessageOKCancel(message,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(activity,
                                    new String[]{Manifest.permission.WRITE_CONTACTS},
                                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                        }
                    });
            return;

        }

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(ctx)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }


    private boolean addPermission(List<String> permissionsList, String permission) {
        if (ActivityCompat.checkSelfPermission(ctx, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity,permission))
                return false;
        }
        return true;
    }

}
