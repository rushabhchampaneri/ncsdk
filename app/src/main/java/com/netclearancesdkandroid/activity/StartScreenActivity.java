package com.netclearancesdkandroid.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.netclearancesdkandroid.R;
import com.netclearancesdkandroid.utils.AppMethods;

import java.util.ArrayList;

public class StartScreenActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = StartScreenActivity.class.getSimpleName();
    private Button btBTOn;
    private Button btBTSupport;
    private Button btStartScan;
    private EditText et_uuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);

        btBTOn = findViewById(R.id.btBTOn);
        btBTSupport = findViewById(R.id.btBTSupport);
        btStartScan = findViewById(R.id.btStartScan);
        et_uuid = findViewById(R.id.et_uuid);

        btStartScan.setOnClickListener(this::onClick);
        btBTSupport.setOnClickListener(this::onClick);
        btBTOn.setOnClickListener(this::onClick);

        AppMethods.checkLocationPermission(StartScreenActivity.this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btBTOn:
                Toast.makeText(this, GlobalApplication.sharedInstance.isBluetoothON()+"", Toast.LENGTH_SHORT).show();
                break;

            case R.id.btBTSupport:
                Toast.makeText(this, GlobalApplication.sharedInstance.isBLESupported()+"", Toast.LENGTH_SHORT).show();
                break;

            case R.id.btStartScan:
                String dataUUID = et_uuid.getText().toString().trim();
                if (dataUUID.equals("")) {
                    Intent intentStartScan = new Intent(StartScreenActivity.this, ScanDeviceActivity.class);
                    startActivity(intentStartScan);
                    finish();
                }else {
                    ArrayList<ParcelUuid> serviceList = AppMethods.getServicePrcelUUIDList(dataUUID,StartScreenActivity.this);
                    if (serviceList != null){
                        Intent intentStartScan = new Intent(StartScreenActivity.this, ScanDeviceActivity.class);
                        intentStartScan.putExtra("FilterUUID",dataUUID);
                        startActivity(intentStartScan);
                        finish();
                    }
                }
                break;
        }
    }
}