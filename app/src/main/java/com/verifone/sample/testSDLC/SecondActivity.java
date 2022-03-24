package com.verifone.sample.testSDLC;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.verifone.sample.testSDLC.utils.HexDump;
import com.verifone.sample.testSDLC.utils.KrNrModemManager;

public class SecondActivity extends AppCompatActivity {
    private String TAG="SecondActivity";
    private TextView logMessage;
    private KrNrModemManager krnrmodemManager = null;
    private KrNrModemManager.KrNrModemManagerDelegate modemDelegate = new KrNrModemManager.KrNrModemManagerDelegate() {
        @Override
        public void onDialConnected() {
            log_output("onDialConnected() event.");
            log_output("send test data");
            krnrmodemManager.send(MainActivity_Demo.sam_msg_out);
        }

        @Override
        public void onDialDisconnected() {
            log_output("onDialDisconnected() event.");
        }

        @Override
        public void onDialFailed(int code, String message) {
            log_output(String.format("onDialFailed() event. code=%d. message=%s",code, message));
        }

        @Override
        public void onSendDone(int resultCode) {
            log_output(String.format("onSendDone() event. resultCode=%d.",resultCode));
        }


        @Override
        public void onReceive(byte[] data) {
            String receiveHexString = HexDump.dumpHexString(data);
            log_output(String.format("onReceive() event. receiveHexString=%s.",receiveHexString));
        }

        @Override
        public void onErrors(String errorMessage) {
            log_output(String.format("onErrors() event. errorMessage=%s.",errorMessage));
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        logMessage = findViewById(R.id.logmessage);
        krnrmodemManager = KrNrModemManager.getInstance(getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "register modem delegate");
        krnrmodemManager.delegate = modemDelegate;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy(), unRegister modem delegate.");
        krnrmodemManager.delegate = null;
    }

    void log_output(String message){

        Log.d(TAG, message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage.append("\n");
                logMessage.append( message );

            }
        });
    }

    public  void btnHangup(View view)
    {
        Log.d(TAG, "Hangup buton click");
        krnrmodemManager.hangup();
    }

    public  void btnClose(View view)
    {
        Log.d(TAG, "btnClose()  click");
        finish();
    }
}