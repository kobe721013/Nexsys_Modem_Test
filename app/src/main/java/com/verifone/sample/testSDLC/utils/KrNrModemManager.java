package com.verifone.sample.testSDLC.utils;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.verifone.sample.testSDLC.R;
import com.verifone.smartpos.sdlc.aidl.SdlcListener;
import com.verifone.smartpos.sdlc.constdefine.ConstSDLCService;

import java.util.Arrays;

public class KrNrModemManager {
    private static final String TAG = "KrNrModemManager";
    public interface KrNrModemManagerDelegate
    {
        void onDialConnected();
        void onDialDisconnected();
        void onDialFailed(int code, String message);
        void onSendDone(int resultCode);
        void onReceive(byte[] data);
        void onErrors(String errorMessage);
    }


    private SdlcListener sdlcListener;
    private SDLCServiceManager sdlcServiceManager;
    private static KrNrModemManager instance = null;
    private int loopCount = 0;

    //Singleton
    public KrNrModemManagerDelegate delegate = null;
    public static KrNrModemManager getInstance(Context context) {
        if (instance == null) {
            instance = new KrNrModemManager(context);
        }
        return(instance);
    }

    private KrNrModemManager(Context context)
    {
        Log.d(TAG, "KrNrModemManager Init()");
        sdlcServiceManager = new SDLCServiceManager(context, new ServiceManager.ServiceManagerIF() {
            @Override
            public void onBindSuccess() {
                //
                Log.d(TAG, "Service onBindSuccess" );
            }

            @Override
            public void onBindFails() {
                Log.e(TAG, "Service onBindFails" );
            }

            @Override
            public void onConnected(IBinder service) {
                Log.d(TAG, "Service onConnected" );
            }

            @Override
            public void onDisconnected() {
                Log.w(TAG, "Service onDisconnected" );
            }
        });

        //connect to VF service
        sdlcServiceManager.connect();
    }

    public void modemInit() {

        if( sdlcServiceManager.isConnected() ){
            Bundle bundle = new Bundle();
//            bundle.putInt(ConstSDLCService.init.logLevel, Log.VERBOSE );
            bundle.putInt(ConstSDLCService.init.logLevel, Log.WARN );
//            bundle.putString(ConstSDLCService.init.mdmFW, "/sdcard/verifone/sdlc/HAR_CHIN.HEX" );
//            bundle.putString(ConstSDLCService.init.mdmVer, "F2000B10260001" );

            try {
                sdlcServiceManager.getIsdlcService().init( bundle );


                if( sdlcListener == null ){
                    sdlcListener = new SdlcListener.Stub() {
                        @Override
                        public void onConnect() throws RemoteException {
                            Log.i(TAG, "onConnect: " );
                            Log.i(TAG, "Line - Connected");
//                            if( autoDialstep == 1 ){
//                                // call send
//                                Message msg = new Message();
//                                msg.getData().putString("type", "value" );
//                                sdlc_auto_test.sendMessage(msg);
//
//                            } else {
                                if(delegate != null)
                                {
                                    Log.d(TAG, "callback onDialConnected()");
                                    delegate.onDialConnected();
                                }
//                            }

                        }

                        @Override
                        public void onDisconect() throws RemoteException {
                            Log.i(TAG, "onDisconect, loop count: " + loopCount );
                            Log.i(TAG, "Line - hanguped");
                            //autoDialstep = 0;

//                            if( loopCount > 1 ) {
//                                try {
//                                    Thread.sleep( 1000 );
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                                autoDialstep = 1;
//
//                                dial_dial();
//
//                            } else {

                                Log.i(TAG, "Dial disconnected");
                                if(delegate != null)
                                {
                                    Log.i(TAG, "Callback, Dial disconnected()");
                                    delegate.onDialDisconnected();
                                }
//                            }
                        }

                        @Override
                        public void onFail(int code, String message) throws RemoteException {
                            Log.e(TAG, "onFail: " + code + ", "+ message );
                            if(delegate != null)
                            {
                                delegate.onDialFailed(code, message);
                            }
//                            autoDialstep = 0;
                        }
                    };
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "not connect with SDLC service");
        }
    }

    public void modemDeinit() {
        if( sdlcServiceManager.isConnected() ) {
            Bundle bundle = new Bundle();
            try {
                sdlcServiceManager.getIsdlcService().deinit(bundle);
            } catch (RemoteException e) {
                Log.e(TAG, String.format("modemDeinit error. Exception:%s", e.toString()));
                e.printStackTrace();
            }
        }
    }

    public void modemDial(String phoneNmmber) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                dial_dial(phoneNmmber);
            }
        }).start();

    }
    private void dial_dial(String phoneNmmber) {

        if( sdlcServiceManager.isConnected() ){
            Bundle bundle = new Bundle();
            bundle.putString( "number", phoneNmmber );
            bundle.putInt( "timeout", 60 );

//            bundle.putString(ConstSDLCService.dialup.bundle_fixed_atd_command, "ATD");
            bundle.putStringArray(ConstSDLCService.dialup.bundle_fixed_at_command_list, new String[] {
                    "AT&FE0Q0V0",
                    "AT-STE=1",
                    "AT-PV",
                    "ATV1",
                    "AT&F",
                    "ATE0",
                    "AT&K3",
                    "ATW2X4S25=1&D2%C0\\N0",
                    "AT+MS=v22",
                    "AT$F2S17=15+ES=6,,8;+ESA=,,,,1",
            });
            try {
                Log.d(TAG, "Dialing ...");
                sdlcServiceManager.getIsdlcService().dialup(bundle, sdlcListener );
            } catch (RemoteException e) {
                Log.e(TAG, String.format("dial_dial error. Exception:%s", e.toString()));
                e.printStackTrace();
            }

        }

    }

    byte[] msg_sent;

    public void dialSend(byte[] data) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                dialsend(data);
            }
        }).start();
    }
    void dialsend(byte[] data) {
        if( sdlcServiceManager.isConnected() ){
            try {
                Log.d(TAG,"Sending...");

                int resultCode = sdlcServiceManager.getIsdlcService().send( data );
                if(delegate != null)
                {
                    Log.d(TAG, "Callback onSendDone(). resultCode:"+resultCode);
                    delegate.onSendDone(resultCode);
                }

            } catch (RemoteException e) {
                Log.e(TAG, String.format("dial_send error. Exception:%s", e.toString()));
                e.printStackTrace();
            }
        }
    }

    public void dialReceive() {

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        dialreceive();
                    }
                }
        ).start();
    }

    private void dialreceive(){
        if( sdlcServiceManager.isConnected() ) {
            Log.d(TAG, "Receiving ...");

            Bundle bundle = new Bundle();
            try {
                byte[] buffer = new byte[4096];
                int ret = sdlcServiceManager.getIsdlcService().receive(buffer, buffer.length, 10);
                Log.d(TAG, "Line - received return: " + ret);
                if (ret > 0) {

                    if(delegate != null)
                    {
                        Log.d(TAG,"Callback onReceive()");
                        delegate.onReceive(buffer);
                    }
//                    Log.i(TAG, "receive length: " + ret);
//                    byte[] out = new byte[ret - 5];
//                    System.arraycopy(buffer, 5, out, 0, ret - 5);
//                    Log.d(TAG, "Buffer size: " + out.length);
//                    Log.v(TAG, "read text: " + HexDump.dumpHexString(out));
//
//                    byte[] msg = new byte[msg_sent.length - 5];
//                    System.arraycopy(msg_sent, 5, msg, 0, msg_sent.length - 5);
//
//                    if (Arrays.equals(out, msg)) {
//                        Log.d(TAG, "Received - pass");
//                        log_output("Received - pass");
//                    } else {
//                        Log.e(TAG, "Received - Data fails");
//                        log_output("Received - Data fails");
//                    }
                } else if (ret == 0) {
                    if(delegate != null)
                    {
                        delegate.onErrors("receive timeout");
                    }
                    Log.e(TAG, "receive timeout");
                } else {
                    if(delegate != null)
                    {
                        delegate.onErrors("Error happen:"+ret);
                    }
                    Log.e(TAG, "Error happen: " + ret);
                }
            } catch (RemoteException e) {
                Log.e(TAG, String.format("dialreceive error. Exception:%s", e.toString()));
                e.printStackTrace();
            }
        }
    }

    public void dialHangup() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                dialhangup();
            }
        }).start();

    }

    private void dialhangup() {
        if( sdlcServiceManager.isConnected() ){
            Log.d( TAG,"hang up ..." );
            Bundle bundle = new Bundle();
            try {
                sdlcServiceManager.getIsdlcService().hangup( sdlcListener );
            } catch (RemoteException e) {
                Log.e(TAG, String.format("dialhangup error. Exception:%s", e.toString()));
                e.printStackTrace();
            }
        }
    }

//    int autoDialstep = 0;
//    int repeat_count = 0;
//    public void btnDialAuto(View view) {
//
//        setEnabled( R.id.btnDial, false );
//        setEnabled( R.id.btnAuto, false );
//
//        loop_count = Integer.valueOf (  ((EditText)findViewById( R.id.edLoop)).getText().toString() );
//        repeat_count = Integer.valueOf (  ((EditText)findViewById( R.id.edRepeat)).getText().toString() );
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                dial_dial();
//                autoDialstep = 1;
//
//            }
//        }).start();
//    }
//
//
//    Handler sdlc_auto_test = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
//                    int repeat = repeat_count;
//                    do {
//
//                        Log.i(TAG, "call send");
//                        dial_send();
//                        autoDialstep = 2;
//
//                        int delay = Integer.valueOf(((EditText) findViewById(R.id.edDelay)).getText().toString());
//                        if (delay > 0) {
//                            try {
//                                Thread.sleep(delay * 1000);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        Log.i(TAG, "call recv");
//                        dial_recv();
//                        autoDialstep = 2;
//                    } while ((--repeat) > 0);
//
//                    autoDialstep = 3;
//                    dial_hangup();
//
//
//                    --loop_count;
//
//                }
//            }).start();
//        }
//    };
}
