package com.example.jk.permit;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

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

        addPermission(Manifest.permission.ACCESS_FINE_LOCATION,"GPS");
   //     addPermission( Manifest.permission.CALL_PHONE,"Call");
   //     addPermission(Manifest.permission.CALL_PHONE,"SMS");
;
        return permissionsList.size();
    }


    public void checkpermission(){
        if (permissionsNeeded.size() > 0) {
            // Need Rationale
            String message = "You need to grant access to " + permissionsNeeded.get(0);
            for (int i = 0; i < permissionsNeeded.size(); i++){
//                showMessageOKCancel(message,
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                ActivityCompat.requestPermissions(activity,
//                                        ,
//                                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
//                            }
//                        });
                final int j=i;
                showMessageOKCancel("You need to allow access to"+permissionsNeeded.get(i),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(activity,
                                        new String[]{permissionsList.get(j)},
                                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                            }
                        });
            }
               // message = message + ", " + permissionsNeeded.get(i);

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


    private boolean addPermission(String permission, String permission_Name) {
        final String[] p=new  String[]{
                Manifest.permission.READ_CONTACTS,Manifest.permission.WRITE_CONTACTS
        };
        if (ActivityCompat.checkSelfPermission(ctx, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity,permission))
                showMessageOKCancel("You need to allow access to "+permission_Name,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(activity,
                                        p,
                                        REQUEST_CODE_ASK_PERMISSIONS);
                            }
                        });
                showLog("1111");
                return false;
        }
        return true;
    }
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private void showLog(String s) {
        Log.i("lzk", s);
    }
}
