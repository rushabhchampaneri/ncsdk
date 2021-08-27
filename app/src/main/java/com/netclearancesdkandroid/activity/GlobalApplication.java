package com.netclearancesdkandroid.activity;

import android.app.Application;

import com.netclearance_ble_sdk.NCBluetoothManager;

public class GlobalApplication extends Application {
    public static NCBluetoothManager sharedInstance = null;
    @Override
    public void onCreate() {
        super.onCreate();
        GlobalApplication.sharedInstance = NCBluetoothManager.sharedInstance(this);
    }
}
