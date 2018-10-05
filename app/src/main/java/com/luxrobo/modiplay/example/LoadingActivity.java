package com.luxrobo.modiplay.example;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import java.util.ArrayList;

public class LoadingActivity extends Activity {

    private static ArrayList<String> permissionList = new ArrayList<>();

    static {
        permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkAllPermission();
    }

    public void initialize() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    /**
     * 권한 확인
     */
    private void checkAllPermission() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            initialize();
        } else {
            ArrayList<String> deniedPermissions = new ArrayList<>();

            for (int i = 0; i < permissionList.size(); i++) {

                if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(permissionList.get(i))) {

                    deniedPermissions.add(permissionList.get(i));
                }
            }

            if (deniedPermissions.size() > 0) {
                requestPermissions(deniedPermissions.toArray(new String[deniedPermissions.size()]), 0x20);
            } else {

                initialize();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == 0x20) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                int count = 0;

                for (int i = 0; i < permissionList.size(); i++) {

                    if (PackageManager.PERMISSION_GRANTED == checkSelfPermission(permissionList.get(i))) {

                        count++;
                    }
                }

                if (count == permissionList.size()) {
                    initialize();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Notice");
                    builder.setMessage("Please allow access to the service in [Settings> Application manager> Permission].");
                    builder.setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent i = new Intent();
                                    i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    i.addCategory(Intent.CATEGORY_DEFAULT);
                                    i.setData(Uri.parse("package:" + getPackageName()));
                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                    startActivity(i);
                                    finish();
                                }
                            });
                    builder.setNegativeButton("No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    initialize();
                                }
                            });
                    builder.show();
                }

            }
        }
    }
}
