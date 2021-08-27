package com.netclearancesdkandroid.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.netclearance_ble_sdk.observer.LogObserver;
import com.netclearancesdkandroid.R;

import static com.netclearancesdkandroid.activity.NCDeviceActionActivity.logs;

public class DebugActivity extends AppCompatActivity {
    private TextView tv_debug, tv_clearLogs;
    private int islog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        tv_debug = findViewById(R.id.tv_debug);
        tv_clearLogs = findViewById(R.id.tv_clearLogs);

        if (getIntent().getExtras() != null && getIntent().hasExtra("isDebug")) {
            tv_debug.setText(logs);
            islog = getIntent().getIntExtra("isDebug", 0);
        }

        GlobalApplication.sharedInstance.register(logObserver);

        tv_clearLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logs = "";
                tv_debug.setText("");
            }
        });
    }

    private LogObserver logObserver = new LogObserver() {
        @Override
        public void debug(String text) {
            if (islog == 1) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_debug.append("\n" + text);
                    }
                });
            }
        }
    };

    @Override
    protected void onDestroy() {
        GlobalApplication.sharedInstance.unregister(logObserver);
        super.onDestroy();
    }
}