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

public class MainActivity_2 extends AppCompatActivity {

    private KrNrService krnrService;
    private String TAG="MainActivity_2";
    TextView edLogOutput;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        edLogOutput = findViewById(R.id.textViewMessage);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG,"onDestroy");
        super.onDestroy();
        unbindService(connection);
    }

    public void btnBind(View view) {
//        if( sdlcServiceManager.getStatus() != ServiceManager.Status.connected ){
//            sdlcServiceManager.connect();
//        }

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
}