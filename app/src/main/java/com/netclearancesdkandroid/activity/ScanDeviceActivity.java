package com.netclearancesdkandroid.activity;

import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.netclearance_ble_sdk.model.NCDevice;
import com.netclearance_ble_sdk.model.NCError;
import com.netclearance_ble_sdk.observer.ConnectionObserver;
import com.netclearance_ble_sdk.observer.ScaningCallback;
import com.netclearance_ble_sdk.observer.ScanningObserver;
import com.netclearancesdkandroid.R;
import com.netclearancesdkandroid.adapter.ConnectDeviceAdapter;
import com.netclearancesdkandroid.utils.AppMethods;

import java.util.ArrayList;

import static com.netclearancesdkandroid.activity.GlobalApplication.sharedInstance;


public class ScanDeviceActivity extends AppCompatActivity implements ScaningCallback {
    public static ArrayList<NCDevice> ncDeviceArrayList = new ArrayList<>();
    private ConnectDeviceAdapter connectDeviceAdapter = null;
    private RecyclerView rv_devices;
    private Button btn_startScan;
    private ArrayList<ParcelUuid> scanFilterList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedInstance.register(scanningObserver);
        sharedInstance.register(connectionObserver);

        boolean isBluetoothOn = sharedInstance.isBluetoothON();
        boolean isBleSupported = sharedInstance.isBLESupported();
        boolean isScanning = sharedInstance.isScanning();
        Log.d("NCBluetoothManager==>", "isBluetoothOn:" + isBluetoothOn + ", isBleSupported:" + isBleSupported + ", isScanning:" + isScanning);

        initUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        connectDeviceAdapter.notifyDataSetChanged();
    }

    private void initUi() {
        rv_devices = findViewById(R.id.rv_devices);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(ScanDeviceActivity.this);
        rv_devices.setLayoutManager(mLayoutManager);
        connectDeviceAdapter = new ConnectDeviceAdapter(ScanDeviceActivity.this, ncDeviceArrayList);
        rv_devices.setAdapter(connectDeviceAdapter);

        btn_startScan = findViewById(R.id.btn_startScan);
        btn_startScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btn_startScan.getText().toString().trim().equals(getString(R.string.start_scan))) {
                    startScan();
                }else {
                    sharedInstance.stopScan();
                    btn_startScan.setText(getString(R.string.start_scan));
                }
            }
        });

        if (getIntent().getExtras()!=null && getIntent().hasExtra("FilterUUID")){
            scanFilterList = AppMethods.getServicePrcelUUIDList(getIntent().getStringExtra("FilterUUID"), ScanDeviceActivity.this);
        }
        startScan();
    }

    private void startScan(){
        ncDeviceArrayList = new ArrayList<>();
        connectDeviceAdapter = new ConnectDeviceAdapter(ScanDeviceActivity.this, ncDeviceArrayList);
        rv_devices.setAdapter(connectDeviceAdapter);
        if (scanFilterList!=null && scanFilterList.size()>0){
            sharedInstance.startScan(scanFilterList, true, 15, this);
        }else {
            sharedInstance.startScan(null, true, 15, this);
        }
    }

    @Override
    public void scanBlock(NCDevice device) {
        Log.d("NCBluetoothManager==>", "scanBlock: " + device.getAdvertisement_data().getDevice().getAddress());
        if (!isContainDeviceList(device.getPeripheral_uuid())) {
            ncDeviceArrayList.add(device);
            connectDeviceAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void errorBlock(NCError error) {
        Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
        Log.d("NCBluetoothManager==>", "errorBlock: " + error.getMessage());
    }

    private ScanningObserver scanningObserver = new ScanningObserver() {
        @Override
        public void deviceDiscovered(NCDevice device) {
            Log.d("scanningObserver==>", "deviceDiscovered: " + device.getAdvertisement_data().getDevice().getAddress());
        }

        @Override
        public void scanningEvent(boolean started) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (started){
                        btn_startScan.setText(getString(R.string.stop_scan));
                    }else {
                        btn_startScan.setText(getString(R.string.start_scan));
                    }
                    Log.d("scanningObserver==>", "scanningEvent: " + started);
                }
            });
        }
    };

    private ConnectionObserver connectionObserver = new ConnectionObserver() {
        @Override
        public void connected(NCDevice device) {
            Log.d("connectionObserver==>", "connected to: " + device.getAdvertisement_data().getDevice().getAddress());
        }

        @Override
        public void disconnected(NCDevice device) {
            Log.d("connectionObserver==>", "disconncted from: " + device.getAdvertisement_data().getDevice().getAddress());
            disconnectDeviceInList(device.getAdvertisement_data().getDevice().getAddress());
        }
    };

    private boolean isContainDeviceList(String macAddress) {
        for (int i = 0; i < ncDeviceArrayList.size(); i++) {
            if (ncDeviceArrayList.get(i).getPeripheral_uuid().equals(macAddress)) {
                return true;
            }
        }
        return false;
    }

    private void disconnectDeviceInList(String macAddress) {
        for (int i = 0; i < ncDeviceArrayList.size(); i++) {
            if (ncDeviceArrayList.get(i).getPeripheral_uuid().equals(macAddress)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connectDeviceAdapter.notifyDataSetChanged();
                    }
                });
                return;
            }
        }
    }

    @Override
    protected void onDestroy() {
        sharedInstance.unregister(scanningObserver);
        sharedInstance.unregister(connectionObserver);
        super.onDestroy();
    }
}