package com.netclearancesdkandroid.utils;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.netclearance_ble_sdk.model.NCError;
import com.netclearance_ble_sdk.observer.CompleteCallback;
import com.netclearancesdkandroid.R;
import com.netclearancesdkandroid.activity.NCDeviceActionActivity;
import com.netclearancesdkandroid.activity.ScanDeviceActivity;

public class CustomDialogClass extends Dialog implements
        View.OnClickListener {

    public Context context;
    public Dialog d;
    public Button btTimeOut;
    public ImageView ivClose;
    public EditText etTimeOut;
    private int position =0 ;


    public CustomDialogClass(@NonNull Context context, int position) {
        super(context);
        this.context = context;
        this.position = position;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View v = getWindow().getDecorView();
        v.setBackgroundResource(android.R.color.transparent);
        setContentView(R.layout.custom_alert_dialog);

        btTimeOut = (Button) findViewById(R.id.btTimeOut);
        ivClose = (ImageView) findViewById(R.id.ivClose);
        etTimeOut = (EditText) findViewById(R.id.etTimeout);
        btTimeOut.setOnClickListener(this);
        ivClose.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btTimeOut:
                String timeout = etTimeOut.getText().toString().trim();
                if (timeout.equals("")){
                    Toast.makeText(context, "Please enter timeout", Toast.LENGTH_SHORT).show();
                    return;
                }
                showProgressDialog();
                ScanDeviceActivity.ncDeviceArrayList.get(position).connect(context, Integer.parseInt(timeout), new CompleteCallback() {
                    @Override
                    public void successBlock() {
                        hideProgressDialog();
                        ScanDeviceActivity.ncDeviceArrayList.get(position).setTimeout(10);
                        Log.d("NCBluetoothManager==>", "connected");
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent mIntent = new Intent(context, NCDeviceActionActivity.class);
                                mIntent.putExtra("Postion", position);
                                context.startActivity(mIntent);
                                dismiss();
                            }
                        });
                    }

                    @Override
                    public void errorBlock(NCError error) {
                        ((Activity)context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideProgressDialog();
                                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        Log.d("NCBluetoothManager==>", error.getMessage());
                    }
                });
                break;
            case R.id.ivClose:
                dismiss();
                break;
        }
    }


    private ProgressDialog pDialog;

    private void showProgressDialog() {
        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Connecting...");
        pDialog.show();
    }

    private void hideProgressDialog() {
        try {
            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
