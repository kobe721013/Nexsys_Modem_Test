package com.verifone.sample.testSDLC;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class KrNrService extends Service {

        private String TAG = "MainService";

        public MyBinder myBinder = new MyBinder();
        public int currentCount = 0;

        // 綁定此 Service 的物件
        public class MyBinder extends Binder {
            public KrNrService getService() {
                return KrNrService.this;
            }
        }

        // 綁定
        @Override
        public IBinder onBind(Intent intent) {
            Log.d(TAG, "MainService onBind");

            return myBinder;
        }

        // 解除綁定
        @Override
        public boolean onUnbind(Intent intent) {
            Log.d(TAG, "MainService onUnbind");
            return super.onUnbind(intent);
        }

        @Override
        public void onCreate() {
            super.onCreate();
            Log.d(TAG, "MainService onCreate");
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            Log.d(TAG, "MainService onStartCommand");
            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            Log.d(TAG, "MainService onDestroy");
        }

        // Service 測試用的 Method
        public void testFunction(){
            Log.d(TAG, "call testFunction()");
        }

        Boolean stopTimer = false;
        public void timerStart()
        {
           new Thread(new Runnable() {
               @Override
               public void run() {
                    int i=1;
                    while(true)
                    {
                        if(stopTimer == true)
                            break;
                        try {
                            currentCount = i;
                            Log.d(TAG, "count=" + i );
                            i++;
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
               }
           }).start();

        }

}
