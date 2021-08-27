package com.netclearancesdkandroid.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.netclearance_ble_sdk.NCBluetoothManager;
import com.netclearance_ble_sdk.model.NCDevice;
import com.netclearance_ble_sdk.model.NCError;
import com.netclearance_ble_sdk.observer.CompleteCallback;
import com.netclearancesdkandroid.R;
import com.netclearancesdkandroid.activity.NCDeviceActionActivity;
import com.netclearancesdkandroid.utils.AppMethods;
import com.netclearancesdkandroid.utils.CustomDialogClass;

import java.util.ArrayList;

public class ConnectDeviceAdapter extends RecyclerView.Adapter<ConnectDeviceAdapter.MyViewHolder> {
    private ArrayList<NCDevice> deviceList;
    private Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_Device_mac, tv_device_name, tv_connect_btn, tv_Rssi, tv_Ad_data;

        public MyViewHolder(View view) {
            super(view);
            tv_device_name = (TextView) view.findViewById(R.id.tv_device_name);
            tv_Device_mac = (TextView) view.findViewById(R.id.tv_device_mac);
            tv_connect_btn = (Button) view.findViewById(R.id.tv_connect_btn);
            tv_Rssi = (TextView) view.findViewById(R.id.tv_Rssi);
            tv_Ad_data = (TextView) view.findViewById(R.id.tv_Ad_data);
        }
    }

    public ConnectDeviceAdapter(Context context, ArrayList<NCDevice> deviceList) {
        this.context = context;
        this.deviceList = deviceList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_connect_device, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        NCDevice model = deviceList.get(position);
        if (model.getDevice_name() != null && !model.getDevice_name().equals("")) {
            holder.tv_device_name.setText(model.getDevice_name());
        } else {
            holder.tv_device_name.setText("N/A");
        }

        holder.tv_Device_mac.setText(model.getPeripheral_uuid());
        holder.tv_Rssi.setText(model.getRssi() + " dBm");
        SparseArray<byte[]> manufacturerData = model.getAdvertisement_data().getScanRecord().getManufacturerSpecificData();
        if (manufacturerData != null && manufacturerData.size()>0) {
            try {
                byte[] data = manufacturerData.get(manufacturerData.keyAt(0));
                if (data != null) {
                    String manufacturerString = AppMethods.bytesToHex(data);
                    holder.tv_Ad_data.setText(manufacturerString);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        } else {
            holder.tv_Ad_data.setText("-");
        }

        if (model.is_connected()) {
            holder.tv_connect_btn.setText("Disconnect");
            holder.tv_connect_btn.setTextColor(context.getColor(R.color.black));
        } else {
            holder.tv_connect_btn.setText("Connect");
            holder.tv_connect_btn.setTextColor(context.getColor(R.color.white));
        }

        holder.tv_connect_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.tv_connect_btn.getText().toString().trim().equals("Connect")) {
                    NCBluetoothManager.sharedInstance(context).stopScan();
                    showAlertDialog(position);
                } else {
                    deviceList.get(position).disconnect(new CompleteCallback() {
                        @Override
                        public void successBlock() {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    notifyItemChanged(position);
                                }
                            });
                        }

                        @Override
                        public void errorBlock(NCError error) {
                            Log.d("NCBluetoothManager==>", error.getMessage());
                        }
                    });
                }
            }
        });

        holder.tv_device_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deviceList.get(position).is_connected()) {
                    Intent mIntent = new Intent(context, NCDeviceActionActivity.class);
                    mIntent.putExtra("Postion", position);
                    context.startActivity(mIntent);
                }
            }
        });
    }

    private void showAlertDialog(int position) {
        CustomDialogClass cdd = new CustomDialogClass(context, position);
        cdd.show();
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }
}
