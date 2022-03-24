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

    private final String TAG = "KrNrModemManager";
    public interface KrNrModemManagerDelegate
    {
        void onDialConnected();
        void onDialDisconnected();
        void onDialFailed(int code, String message);
        void onSendDone(int resultCode);
        void onReceive(byte[] data);
        void onErrors(String errorMessage);
    }


    private SdlcListener sdlcListener = null;
    private SDLCServiceManager sdlcServiceManager;
    private static KrNrModemManager instance = null;


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
                Log.d(TAG, "SDLC Service onBindSuccess() event." );
            }

            @Override
            public void onBindFails() {
                Log.e(TAG, "SDLC Service onBindFails() event." );
            }

            @Override
            public void onConnected(IBinder service) {

                Log.d(TAG, "SDLC Service onConnected() event." );
                modemInit();
            }

            @Override
            public void onDisconnected() {
                Log.w(TAG, "SDLE Service onDisconnected() event." );
            }
        });

        //connect to VF service
        sdlcServiceManager.connect();
    }

    private void modemInit() {

        if( sdlcServiceManager.isConnected() ){
            Log.d(TAG, "modemInit()");
            Bundle bundle = new Bundle();
//            bundle.putInt(ConstSDLCService.init.logLevel, Log.VERBOSE );
            bundle.putInt(ConstSDLCService.init.logLevel, Log.WARN );
//            bundle.putString(ConstSDLCService.init.mdmFW, "/sdcard/verifone/sdlc/HAR_CHIN.HEX" );
//            bundle.putString(ConstSDLCService.init.mdmVer, "F2000B10260001" );

            try {
                sdlcServiceManager.getIsdlcService().init( bundle );


                if( sdlcListener == null ){

                    Log.d(TAG, "sdlcListener init....");
                    sdlcListener = new SdlcListener.Stub() {
                        @Override
                        public void onConnect() throws RemoteException {
                            Log.i(TAG, "Line - onConnect() event");
                            if(delegate != null)
                            {
                                Log.d(TAG, "callback To onDialConnected()");
                                delegate.onDialConnected();
                            }
                        }

                        @Override
                        public void onDisconect() throws RemoteException {

                            Log.i(TAG, "Line onDisconnect() event.");


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


                                if(delegate != null)
                                {
                                    Log.i(TAG, "callback to onDialDisconnected()");
                                    delegate.onDialDisconnected();
                                }
//                            }
                        }

                        @Override
                        public void onFail(int code, String message) throws RemoteException {
                            Log.e(TAG, String.format("Line - onFail event. code=%d. message=%s", code, message) );
                            if(delegate != null)
                            {
                                Log.i(TAG, "callback to onDialFailed()");
                                delegate.onDialFailed(code, message);
                            }
                        }
                    };
                }

            } catch (RemoteException e) {
                Log.e(TAG, String.format("modemInit() Exception occurs. Exception=%s",e.toString()));
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "modemInit() - not connect with SDLC service");
        }
    }

    public void modemDeInit() {
        if( sdlcServiceManager.isConnected() ) {
            Bundle bundle = new Bundle();
            try {
                sdlcServiceManager.getIsdlcService().deinit(bundle);
            } catch (RemoteException e) {
                Log.e(TAG, String.format("modemDeInit Exception occurs. Exception:%s", e.toString()));
                e.printStackTrace();
            }
        }
        else
        {
            Log.e(TAG, "modemDeInit() - but SDLC Service status is disconnected");
        }
    }

    public void dialTo(String phoneNumber) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                dialto(phoneNumber);
            }
        }).start();

    }
    private void dialto(String phoneNumber) {

        if( sdlcServiceManager.isConnected() ){
            Bundle bundle = new Bundle();
            bundle.putString( "number", phoneNumber );
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
                Log.e(TAG, String.format("dialto() Exception occurs. Exception=%s", e.toString()));
                e.printStackTrace();
            }

        }

    }

    public void send(byte[] data) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                dialSend(data);
            }
        }).start();
    }
    void dialSend(byte[] data) {
        if( sdlcServiceManager.isConnected() ){
            try {
                Log.d(TAG,"Sending...");

                int resultCode = sdlcServiceManager.getIsdlcService().send( data );
                String sendString = HexDump.dumpHexString(data);
                Log.d(TAG, "sendString="+sendString);
                if(delegate != null)
                {
                    Log.d(TAG, "Callback onSendDone(). resultCode:"+resultCode);
                    delegate.onSendDone(resultCode);
                }

                //start to receive data
                dialReceive();

            } catch (RemoteException e) {
                Log.e(TAG, String.format("dial_send error. Exception:%s", e.toString()));
                e.printStackTrace();
            }
        }
        else
        {
            Log.e(TAG, "dialSend() - but SDLC Service status is disconnected");
        }
    }

    public void receive() {

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        dialReceive();
                    }
                }
        ).start();
    }

    private void dialReceive(){
        if( sdlcServiceManager.isConnected() ) {
            Log.d(TAG, "Receiving ...");

            Bundle bundle = new Bundle();
            try {
                byte[] buffer = new byte[4096];
                int ret = sdlcServiceManager.getIsdlcService().receive(buffer, buffer.length, 30);
                Log.d(TAG, "Line - received return: " + ret);
                if (ret > 0) {

                    byte[] out = new byte[ret];
                    System.arraycopy(buffer, 0, out, 0, ret);
                    if(delegate != null)
                    {
                        Log.d(TAG,"Callback onReceive()");
                        delegate.onReceive(out);
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
                Log.e(TAG, String.format("dialReceive error. Exception:%s", e.toString()));
                e.printStackTrace();
            }
        }
        else{
            Log.e(TAG, "Cant receive data, SDLC service is disconnected.");
        }
    }

    public void hangup() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                dialHangup();
            }
        }).start();

    }

    private void dialHangup() {
        if( sdlcServiceManager.isConnected() ){
            Log.d( TAG,"hang up ..." );
            Bundle bundle = new Bundle();
            try {
                sdlcServiceManager.getIsdlcService().hangup( sdlcListener );
            } catch (RemoteException e) {
                Log.e(TAG, String.format("dialHangup error. Exception:%s", e.toString()));
                e.printStackTrace();
            }
        }else{
            Log.e(TAG, "Cant hangup, SDLC service is disconnected.");
        }
    }

}
