package com.verifone.sample.testSDLC.utils;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.verifone.smartpos.sdlc.aidl.ISDLCService;

/**
 * Created by Simon on 2021/6/16
 */
public class SDLCServiceManager extends ServiceManager {
    private static final String TAG = "SDLCServiceManager";
    public static final String SERVICE_ACTION =  "com.verifone.smartpos.sdlc_service";
    public static final String SERVICE_PACKAGE = "com.verifone.smartpos.sdlc";

    ISDLCService isdlcService;

    public SDLCServiceManager(Context context) {
        super(context, SERVICE_PACKAGE, SERVICE_ACTION, null);
    }

    public SDLCServiceManager(Context context, ServiceManagerIF serviceManagerIF) {
        super(context, SERVICE_PACKAGE, SERVICE_ACTION, serviceManagerIF);
    }


    @Override
    protected void onConnected(IBinder iBinder) {
        Log.d(TAG, "onConnected");
        Log.i(TAG, "SDLC service bind success");
        isdlcService = ISDLCService.Stub.asInterface( iBinder );

        if (isdlcService == null) {
            Log.e(TAG, "no sdlc Service found");
        } else {
//            Bundle bundle = new Bundle();
//            try {
//                isdlcService.init( bundle );
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
        }
    }

    @Override
    protected void onDisconnected() {
        Log.w(TAG, "onDisconnected");

    }

    public ISDLCService getIsdlcService(){
        return isdlcService;
    }

}
