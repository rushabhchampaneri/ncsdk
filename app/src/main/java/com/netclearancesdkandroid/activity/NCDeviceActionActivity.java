package com.netclearancesdkandroid.activity;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.netclearance_ble_sdk.model.NCDevice;
import com.netclearance_ble_sdk.model.NCError;
import com.netclearance_ble_sdk.observer.CompleteCallback;
import com.netclearance_ble_sdk.observer.DiscoverChannelsCallback;
import com.netclearance_ble_sdk.observer.DiscoverDescriptorCallback;
import com.netclearance_ble_sdk.observer.DiscoverServicesCallback;
import com.netclearance_ble_sdk.observer.LogObserver;
import com.netclearance_ble_sdk.observer.RSSIObserver;
import com.netclearance_ble_sdk.observer.ReadValueCallback;
import com.netclearance_ble_sdk.observer.ReadValueObserver;
import com.netclearance_ble_sdk.observer.WriteValueObserver;
import com.netclearancesdkandroid.R;
import com.netclearancesdkandroid.utils.AppMethods;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.netclearancesdkandroid.activity.GlobalApplication.sharedInstance;


public class NCDeviceActionActivity extends AppCompatActivity implements View.OnClickListener {
    private int position = 0;
    private TextView tv_action;
    private TextView tv_discover;
    private TextView tv_discoverService;
    private TextView tv_discoverCharactics;
    private TextView tv_discoverDescriptor;
    private TextView tv_readValue;
    private TextView tv_getAll_data;
    private TextView tv_readDesc;
    private TextView tv_write;
    private TextView tv_writeDescriptor;
    private TextView tv_subscribe;
    private EditText et_serviceUUid;
    private EditText et_charUUid;
    private EditText et_descUUid;
    private RadioButton rb_true, rb_debug_true;
    private EditText et_writeValue;
    public static String logs = "";
    private TextView tv_debug;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nc_device_action);

        if (getIntent().getExtras() != null && getIntent().hasExtra("Postion")) {
            position = getIntent().getIntExtra("Postion", 0);
        }

        initUi();
//        et_serviceUUid.setText("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
//        et_charUUid.setText("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
//        et_descUUid.setText("00002902-0000-1000-8000-00805f9b34fb");
        sharedInstance.register(logObserver);
        sharedInstance.register(readValueObserver);
        sharedInstance.register(writeValueObserver);
        sharedInstance.register(rssiObserver);
    }

    private void initUi() {
        tv_action = findViewById(R.id.tv_action);
        tv_discover = findViewById(R.id.tv_discover);
        tv_discoverService = findViewById(R.id.tv_discoverService);
        tv_discoverCharactics = findViewById(R.id.tv_discoverCharactics);
        tv_discoverDescriptor = findViewById(R.id.tv_discoverDescriptor);
        tv_readValue = findViewById(R.id.tv_readValue);
        tv_readDesc = findViewById(R.id.tv_readDesc);
        tv_write = findViewById(R.id.tv_write);
        tv_writeDescriptor = findViewById(R.id.tv_writeDescriptor);
        tv_subscribe = findViewById(R.id.tv_subscribe);
        et_serviceUUid = findViewById(R.id.et_serviceUUid);
        et_charUUid = findViewById(R.id.et_charUUid);
        et_descUUid = findViewById(R.id.et_descUUid);
        rb_true = findViewById(R.id.rb_true);
        rb_debug_true = findViewById(R.id.rb_debug_true);
        et_writeValue = findViewById(R.id.et_writeValue);
        tv_getAll_data = findViewById(R.id.tv_getAll_data);
        tv_debug = findViewById(R.id.tv_debug);

        tv_discover.setOnClickListener(this::onClick);
        tv_discoverService.setOnClickListener(this::onClick);
        tv_discoverCharactics.setOnClickListener(this::onClick);
        tv_discoverDescriptor.setOnClickListener(this::onClick);
        tv_readValue.setOnClickListener(this::onClick);
        tv_readDesc.setOnClickListener(this::onClick);
        tv_write.setOnClickListener(this::onClick);
        tv_writeDescriptor.setOnClickListener(this::onClick);
        tv_subscribe.setOnClickListener(this::onClick);
        tv_getAll_data.setOnClickListener(this::onClick);
        tv_debug.setOnClickListener(this::onClick);
        tv_action.setText(ScanDeviceActivity.ncDeviceArrayList.get(position).getPeripheral_uuid() + " (" + ScanDeviceActivity.ncDeviceArrayList.get(position).getDevice_name() + ")");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_discover:
                if (ScanDeviceActivity.ncDeviceArrayList.get(position).is_connected()) {
                    tv_action.setText("Discovering...");
                    ScanDeviceActivity.ncDeviceArrayList.get(position).discover(new DiscoverServicesCallback() {
                        @Override
                        public void successBlock(List<BluetoothGattService> services) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    for (int i = 0; i < services.size(); i++) {
                                        tv_action.append("\n service: " + services.get(i).getUuid().toString());
                                        List<BluetoothGattCharacteristic> characteristicList = services.get(i).getCharacteristics();
                                        for (int j = 0; j < characteristicList.size(); j++) {
                                            tv_action.append("\n     char: " + characteristicList.get(j).getUuid().toString());
                                        }
                                    }
                                }
                            });
                        }

                        @Override
                        public void errorBlock(NCError error) {
                            setActionList("\n    error: " + error.getMessage());
                        }
                    });
                } else {
                    Toast.makeText(this, "Device disconnected", Toast.LENGTH_SHORT).show();
                    tv_action.append("\n\nDevice Disconnected");
                }
                break;

            case R.id.tv_discoverService:
                String serviceUuid = et_serviceUUid.getText().toString().trim();
                if (serviceUuid.equals("")){
                    Toast.makeText(this, "Please enter service uuid", Toast.LENGTH_SHORT).show();
                    return;
                }

                ArrayList<UUID> arrayList = AppMethods.getServiceUUIDList(serviceUuid,NCDeviceActionActivity.this);
                if (arrayList==null || arrayList.size()==0){
                    return;
                }
                if (ScanDeviceActivity.ncDeviceArrayList.get(position).is_connected()) {
                    tv_action.setText("Discovering Services...");
                    ScanDeviceActivity.ncDeviceArrayList.get(position).discoverServices(arrayList,new DiscoverServicesCallback() {
                        @Override
                        public void successBlock(List<BluetoothGattService> services) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    for (int i = 0; i < services.size(); i++) {
                                        tv_action.append("\n service: " + services.get(i).getUuid().toString());
                                        List<BluetoothGattCharacteristic> characteristicList = services.get(i).getCharacteristics();
                                        for (int j = 0; j < characteristicList.size(); j++) {
                                            tv_action.append("\n     char: " + characteristicList.get(j).getUuid().toString());
                                        }
                                    }
                                }
                            });
                        }

                        @Override
                        public void errorBlock(NCError error) {
                            setActionList("\n    error: " + error.getMessage());
                        }
                    });
                } else {
                    Toast.makeText(this, "Device disconnected", Toast.LENGTH_SHORT).show();
                    tv_action.append("\n\nDevice Disconnected");
                }
                break;

            case R.id.tv_discoverCharactics:
                String serviceUuid1 = et_serviceUUid.getText().toString().trim();
                if (serviceUuid1.equals("")){
                    Toast.makeText(this, "Please enter service uuid", Toast.LENGTH_SHORT).show();
                    return;
                }
                ArrayList<UUID> arrayList1 = AppMethods.getServiceUUIDList(serviceUuid1,NCDeviceActionActivity.this);
                if (arrayList1==null || arrayList1.size()==0){
                    return;
                }
                tv_action.setText("Discover characteristic");
                ScanDeviceActivity.ncDeviceArrayList.get(position).discoverChannels(arrayList1.get(0), new DiscoverChannelsCallback() {
                    @Override
                    public void successBlock(List<BluetoothGattCharacteristic> characteristics) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for (int j = 0; j < characteristics.size(); j++) {
                                    tv_action.append("\n     char: " + characteristics.get(j).getUuid().toString());
                                }
                            }
                        });
                    }

                    @Override
                    public void errorBlock(NCError error) {
                        setActionList("\n    error: " + error.getMessage());
                    }
                });
                break;

            case R.id.tv_discoverDescriptor:
                String charUuid2 = et_charUUid.getText().toString().trim();
                 if (charUuid2.equals("")){
                    Toast.makeText(this, "Please enter characteristic uuid", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!AppMethods.checkIsValidUUID(charUuid2,NCDeviceActionActivity.this)){
                    return;
                }
                tv_action.setText("Discover descriptor...");
                ScanDeviceActivity.ncDeviceArrayList.get(position).discoverDescriptors(UUID.fromString(charUuid2), new DiscoverDescriptorCallback() {
                    @Override
                    public void successBlock(List<BluetoothGattDescriptor> descriptors) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for (int j = 0; j < descriptors.size(); j++) {
                                    tv_action.append("\n     desc: " + descriptors.get(j).getUuid().toString());
                                }
                            }
                        });
                    }

                    @Override
                    public void errorBlock(NCError error) {
                        setActionList("\n    error: " + error.getMessage());
                    }
                });
                break;

            case R.id.tv_readValue:
                String serviceUuid3 = et_serviceUUid.getText().toString().trim();
                String charUuid3 = et_charUUid.getText().toString().trim();
                if (serviceUuid3.equals("")){
                    Toast.makeText(this, "Please enter service uuid", Toast.LENGTH_SHORT).show();
                    return;
                }else if (charUuid3.equals("")){
                    Toast.makeText(this, "Please enter characteristic uuid", Toast.LENGTH_SHORT).show();
                    return;
                }

                ArrayList<UUID> arrayList3 = AppMethods.getServiceUUIDList(serviceUuid3,NCDeviceActionActivity.this);
                if (arrayList3==null || arrayList3.size()==0){
                    return;
                }

                if (!AppMethods.checkIsValidUUID(charUuid3,NCDeviceActionActivity.this)){
                    return;
                }

                tv_action.setText("read value...");
                ScanDeviceActivity.ncDeviceArrayList.get(position).readValue(arrayList3.get(0),
                        UUID.fromString(charUuid3), new ReadValueCallback() {
                    @Override
                    public void successBlock(byte[] value) {
                        setActionList("\n    value = "+ Arrays.toString(value));
                    }

                    @Override
                    public void errorBlock(NCError error) {
                        setActionList("\n    error: " + error.getMessage());
                    }
                });
                break;

            case R.id.tv_readDesc:
                String serviceUuid5 = et_serviceUUid.getText().toString().trim();
                String charUuid5 = et_charUUid.getText().toString().trim();
                String descUuid5 = et_descUUid.getText().toString().trim();
                if (serviceUuid5.equals("")){
                    Toast.makeText(this, "Please enter service uuid", Toast.LENGTH_SHORT).show();
                    return;
                }else if (charUuid5.equals("")){
                    Toast.makeText(this, "Please enter characteristic uuid", Toast.LENGTH_SHORT).show();
                    return;
                }else if (descUuid5.equals("")){
                    Toast.makeText(this, "Please enter descriptor uuid", Toast.LENGTH_SHORT).show();
                    return;
                }
                ArrayList<UUID> arrayList5 = AppMethods.getServiceUUIDList(serviceUuid5,NCDeviceActionActivity.this);
                if (arrayList5==null || arrayList5.size()==0){
                    return;
                }
                if (!AppMethods.checkIsValidUUID(charUuid5,NCDeviceActionActivity.this)){
                    return;
                }
                if (!AppMethods.checkIsValidUUID(descUuid5,NCDeviceActionActivity.this)){
                    return;
                }
                tv_action.setText("read descriptor...");
                ScanDeviceActivity.ncDeviceArrayList.get(position).readDescriptorValue(arrayList5.get(0),
                        UUID.fromString(charUuid5), UUID.fromString(descUuid5),
                        new ReadValueCallback() {
                            @Override
                            public void successBlock(byte[] value) {
                                setActionList("\n    value = "+ Arrays.toString(value));
                            }

                            @Override
                            public void errorBlock(NCError error) {
                                setActionList("\n    error: " + error.getMessage());
                            }
                        });
                break;

            case R.id.tv_write:
                String serviceUuid4 = et_serviceUUid.getText().toString().trim();
                String charUuid4 = et_charUUid.getText().toString().trim();
                String writeValue4 = et_writeValue.getText().toString().trim();
                if (serviceUuid4.equals("")){
                    Toast.makeText(this, "Please enter service uuid", Toast.LENGTH_SHORT).show();
                    return;
                }else if (charUuid4.equals("")){
                    Toast.makeText(this, "Please enter characteristic uuid", Toast.LENGTH_SHORT).show();
                    return;
                }else if (writeValue4.equals("")){
                    Toast.makeText(this, "Please enter write value", Toast.LENGTH_SHORT).show();
                    return;
                }
                ArrayList<UUID> arrayList4 = AppMethods.getServiceUUIDList(serviceUuid4,NCDeviceActionActivity.this);
                if (arrayList4==null || arrayList4.size()==0){
                    return;
                }
                if (!AppMethods.checkIsValidUUID(charUuid4,NCDeviceActionActivity.this)){
                    return;
                }

                byte[] writeValue = new byte[]{};
                try {
                    writeValue   = writeValue4.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                tv_action.setText("write...");
                ScanDeviceActivity.ncDeviceArrayList.get(position).write(arrayList4.get(0),
                        UUID.fromString(charUuid4),writeValue,true, new CompleteCallback() {
                            @Override
                            public void successBlock() {
                                setActionList("success");
                            }

                            @Override
                            public void errorBlock(NCError error) {
                                setActionList("\n    error: " + error.getMessage());

                            }
                        });
                break;

            case R.id.tv_writeDescriptor:
                String serviceUuid6 = et_serviceUUid.getText().toString().trim();
                String charUuid6 = et_charUUid.getText().toString().trim();
                String descUuid6 = et_descUUid.getText().toString().trim();
                String writeValue6 = et_writeValue.getText().toString().trim();
                if (serviceUuid6.equals("")){
                    Toast.makeText(this, "Please enter service uuid", Toast.LENGTH_SHORT).show();
                    return;
                }else if (charUuid6.equals("")){
                    Toast.makeText(this, "Please enter characteristic uuid", Toast.LENGTH_SHORT).show();
                    return;
                }else if (descUuid6.equals("")){
                    Toast.makeText(this, "Please enter descriptor uuid", Toast.LENGTH_SHORT).show();
                    return;
                }else if (writeValue6.equals("")){
                    Toast.makeText(this, "Please enter write value", Toast.LENGTH_SHORT).show();
                    return;
                }
                ArrayList<UUID> arrayList6 = AppMethods.getServiceUUIDList(serviceUuid6,NCDeviceActionActivity.this);
                if (arrayList6==null || arrayList6.size()==0){
                    return;
                }
                if (!AppMethods.checkIsValidUUID(charUuid6,NCDeviceActionActivity.this)){
                    return;
                }
                if (!AppMethods.checkIsValidUUID(descUuid6,NCDeviceActionActivity.this)){
                    return;
                }
                byte[] writeValue1 = new byte[]{};
                try {
                    writeValue1   = writeValue6.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                tv_action.setText("write descriptor...");
                ScanDeviceActivity.ncDeviceArrayList.get(position).writeDescriptorValue(arrayList6.get(0),
                        UUID.fromString(charUuid6), UUID.fromString(descUuid6),
                        writeValue1
                        , new CompleteCallback() {
                            @Override
                            public void successBlock() {
                                setActionList("success");
                            }

                            @Override
                            public void errorBlock(NCError error) {
                                setActionList("\n    error: " + error.getMessage());
                            }
                        });
                break;

            case R.id.tv_subscribe:
                String serviceUuid7 = et_serviceUUid.getText().toString().trim();
                String charUuid7 = et_charUUid.getText().toString().trim();
                String descUuid7 = et_descUUid.getText().toString().trim();
                if (serviceUuid7.equals("")){
                    Toast.makeText(this, "Please enter service uuid", Toast.LENGTH_SHORT).show();
                    return;
                }else if (charUuid7.equals("")){
                    Toast.makeText(this, "Please enter characteristic uuid", Toast.LENGTH_SHORT).show();
                    return;
                }else if (descUuid7.equals("")){
                    Toast.makeText(this, "Please enter descriptor uuid", Toast.LENGTH_SHORT).show();
                    return;
                }
                ArrayList<UUID> arrayList7 = AppMethods.getServiceUUIDList(serviceUuid7,NCDeviceActionActivity.this);
                if (arrayList7==null || arrayList7.size()==0){
                    return;
                }
                if (!AppMethods.checkIsValidUUID(charUuid7,NCDeviceActionActivity.this)){
                    return;
                }
                if (!AppMethods.checkIsValidUUID(descUuid7,NCDeviceActionActivity.this)){
                    return;
                }
                boolean isEnable = false;
                if (rb_true.isChecked()){
                    isEnable = true;
                }
                tv_action.setText("subscribe...");
                ScanDeviceActivity.ncDeviceArrayList.get(position).subscribe(arrayList7.get(0),
                        UUID.fromString(charUuid7), UUID.fromString(descUuid7),
                        isEnable, new CompleteCallback() {
                            @Override
                            public void successBlock() {
                                setActionList("success");
                            }

                            @Override
                            public void errorBlock(NCError error) {
                                setActionList("\n    error: " + error.getMessage());
                            }
                        });
                break;

            case R.id.tv_getAll_data:
                NCDevice ncDevice = ScanDeviceActivity.ncDeviceArrayList.get(position);
                tv_action.setText("NCDevice Data...");
                setActionList("\nDevice Name:"+ncDevice.getDevice_name());
                setActionList("\nDevice State:"+ncDevice.getDevice_state());
                setActionList("\nPeripheral UUID:"+ncDevice.getPeripheral_uuid());
                setActionList("\nRssi:"+ncDevice.getRssi());
                setActionList("\nAverage Rssi:"+ncDevice.getAverage_rssi());
                setActionList("\nTime Out:"+ncDevice.getTimeout());
                setActionList("\nIsDeviceConnected:"+ncDevice.is_connected());
                for (int i=0;i<ncDevice.getServices().size();i++){
                    tv_action.append("\nservice: " + ncDevice.getServices().get(i).getUuid().toString());
                    List<BluetoothGattCharacteristic> characteristicList = ncDevice.getServices().get(i).getCharacteristics();
                    for (int j = 0; j < characteristicList.size(); j++) {
                        tv_action.append("\n     char: " + characteristicList.get(j).getUuid().toString());
                    }
                }
                break;

            case R.id.tv_debug:
                Intent mIntent = new Intent(NCDeviceActionActivity.this, DebugActivity.class);
                mIntent.putExtra("Logs",logs);
                if(rb_debug_true.isChecked()){
                    mIntent.putExtra("isDebug",1);
                }else {
                    mIntent.putExtra("isDebug",0);
                }
                startActivity(mIntent);
                break;
        }
    }

    private void setActionList(String string){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_action.append(string);
            }
        });
    }

    private LogObserver logObserver = new LogObserver() {
        @Override
        public void debug(String text) {
            if (rb_debug_true.isChecked()) {
                logs =logs+"\n"+text;
            }
        }
    };


    private ReadValueObserver readValueObserver = new ReadValueObserver() {
        @Override
        public void didUpdateValue(NCDevice device, byte[] Value, BluetoothGattCharacteristic characteristic, NCError error) {
            Log.d("readValueObserver==>", device.getDevice_name()+", "+characteristic.getUuid()+", "+ Arrays.toString(Value));
        }
    };

    private WriteValueObserver writeValueObserver = new WriteValueObserver() {
        @Override
        public void didWriteValue(NCDevice device,byte[] Value, BluetoothGattCharacteristic characteristic, NCError error) {
            Log.d("writeValueObserver==>", device.getDevice_name()+", "+characteristic.getUuid()+", "+ Arrays.toString(Value));
        }
    };

    private RSSIObserver rssiObserver = new RSSIObserver() {
        @Override
        public void didReadRSSI(NCDevice ncDevice, int rssi, NCError error) {
            Log.d("rssiObserver==>", ncDevice.getDevice_name()+", "+rssi);
            Log.d("rssiObserver==>avg", ncDevice.getAverage_rssi()+"");
        }
    };

    @Override
    protected void onDestroy() {
        sharedInstance.unregister(logObserver);
        sharedInstance.unregister(readValueObserver);
        sharedInstance.unregister(writeValueObserver);
        sharedInstance.unregister(rssiObserver);
        super.onDestroy();
    }
}