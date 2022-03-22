package com.verifone.sample.testSDLC;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.verifone.sample.testSDLC.utils.HexDump;
import com.verifone.sample.testSDLC.utils.KrNrModemManager;

public class MainActivity_Demo extends AppCompatActivity {

    private KrNrModemManager krnrmodemManager;
    private KrNrService krnrService;
    private String TAG="MainActivity_Demo";
    TextView edLogOutput;
    TextView tvLogMessage;

    KrNrModemManager.KrNrModemManagerDelegate managerDelegate = new KrNrModemManager.KrNrModemManagerDelegate() {
        @Override
        public void onDialConnected() {
            log_output("onDialConnected() event.");
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

    void log_output(String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvLogMessage.append("\n");
                tvLogMessage.append( message );

            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        edLogOutput = findViewById(R.id.textViewMessage);
        tvLogMessage = findViewById(R.id.logmessage);
    }

    @Override
    protected void onResume() {
        super.onResume();
        krnrmodemManager = KrNrModemManager.getInstance(getApplicationContext());
        krnrmodemManager.delegate = managerDelegate;
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG,"onDestroy");
        super.onDestroy();
//        unbindService(connection);
    }

    public void btnBind(View view) {

        Log.d(TAG, "btnBind click.");
        // 綁定 Service
        Intent serviceIntent = new Intent(this, KrNrService.class);
        this.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

    }

   public void btnGetCount(View view)
   {
       if(krnrService != null)
       {
           edLogOutput.append(String.format("%d", krnrService.currentCount));
       }
   }

    public ServiceConnection connection = new ServiceConnection() {

        // 成功與 Service 建立連線
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            krnrService = ((KrNrService.MyBinder) service).getService();
            Log.d(TAG, "MainActivity_2 onServiceConnected");
        }

        // 與 Service 建立連線失敗
        @Override
        public void onServiceDisconnected(ComponentName name) {
            krnrService = null;
            Log.d(TAG, "MainActivity_2 onServiceDisconnected");
        }
    };

    public void btnClose(View view)
    {
        finish();
    }

    public  void btnDial(View view)
    {
        Log.d(TAG, "Dial buton click");
        log_output("Dial to [26310152]");
        krnrmodemManager.dialTo("26310152");
    }

    public  void btnHandup(View view)
    {
        Log.d(TAG, "Dial buton click");
        krnrmodemManager.hangup();
    }
}