package com.verifone.sample.testSDLC.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Simon on 2019/3/5.
 */

/**
 * step by step to connect service
 * step 1, copy the aidl folder to you project FOLDER main
 * step 2, add the path of aidl to you app build gradle, in section android
 * sourceSets {
 * main {
 * java.srcDirs = ['src/main/java', 'src/main/aidl']
 * }
 * }
 * <p>
 * ## sync ## after ## add ## those ## files ##
 * step 3, add source code in your project to connect the VFService
 * <p>
 * void connectVFService(){
 * ServiceManager vfService = new ServiceManager(this, "com.vfi.smartpos.deviceservice",
 * "com.vfi.smartpos.device_service", new ServiceManager.ServiceManagerIF() {
 *
 * @Override public void onBindSuccess() {
 * <p>
 * }
 * @Override public void onBindFails() {
 * <p>
 * }
 * @Override public void onConnect(IBinder service) {
 * // import com.vfi.smartpos.deviceservice.aidl.*;
 * IDeviceService idevice = IDeviceService.Stub.asInterface(service);
 * try {
 * IEMV iemv = idevice.getEMV();
 * IPinpad ipinpad = idevice.getPinpad(1);
 * IPrinter iPrinter = idevice.getPrinter();
 * } catch (RemoteException e) {
 * e.printStackTrace();
 * }
 * }
 * @Override public void onDisconnect() {
 * <p>
 * }
 * });
 * <p>
 * vfService.connect();
 * }
 * <p>
 * step 4, run the connectVFService to connect service in your source code.
 */

public class ServiceManager {
    private static final String TAG = "ServiceManager";

    Context context;
    protected ServiceManagerIF serviceManagerIF = null;
    String packageName;
    String action;
    String className;

    public interface ServiceManagerIF {

        void onBindSuccess();    // bind first , then connect

        void onBindFails();

        void onConnected(IBinder service);

        void onDisconnected();     // bind first , then connect
    }

    public enum Status {
        connected,
        disconnected,
    }

    Status status;


    public ServiceManager(Context context, String packageName, ServiceManagerIF serviceManagerIF) {
        Log.d(TAG, "Create to connect package name & action :" + packageName);
        this.context = context;
        if (null != serviceManagerIF) {
            this.serviceManagerIF = serviceManagerIF;
        }
        this.packageName = packageName;
        this.action = packageName;
        this.className = null;

        status = Status.disconnected;
    }

    public ServiceManager(Context context, String packageName, String action, ServiceManagerIF serviceManagerIF) {
        Log.d(TAG, "Create create to connect package name:" + packageName + ", action:" + action);
        this.context = context;
        if (null != serviceManagerIF) {
            this.serviceManagerIF = serviceManagerIF;
        }
        this.packageName = packageName;
        this.action = action;
        this.className = null;

        status = Status.disconnected;
    }

    public ServiceManager(Context context, String packageName, String className, String action,
                          ServiceManagerIF serviceManagerIF) {
        Log.d(TAG, "Create create to connect package name:" + packageName + ", action:" + action);
        this.context = context;
        if (null != serviceManagerIF) {
            this.serviceManagerIF = serviceManagerIF;
        }
        this.packageName = packageName;
        this.action = action;
        this.className = className;

        status = Status.disconnected;
    }

    protected void onBindSuccess() {
        // bind first , then connect
    }

    protected void onBindFails() {

    }

    protected void onConnected(IBinder service) {
        // bind first , then connect

    }

    protected void onDisconnected() {

    }

    // connect service -- start

    /**
     * connect is none-block method
     * NOT try to get the member value/attribute JUST after connect be called
     * ServiceConnection will be called after the connect Context QUEUE finish
     * So,
     * MUST get the member value/attribute in another Context QUEUE
     * Or, set the callback ServiceManagerIF to get the member value/attribute
     */
    public void connect() {

        Intent intent = new Intent();
        intent.setAction(action);
        if (null != className && !"".equals(className)) {
            intent.setClassName(packageName, className);
        } else {
            intent.setPackage(packageName);
        }
        boolean bindResult = context.bindService(intent, conn, Context.BIND_AUTO_CREATE);
        if (bindResult) {
            Log.i(TAG, "bind service success");
            onBindSuccess();
            if (null != serviceManagerIF) {
                serviceManagerIF.onBindSuccess();
            }
        } else {
            Log.e(TAG, "bind service fail!");
            onBindFails();
            if (null != serviceManagerIF) {
                serviceManagerIF.onBindFails();
            }
            this.status = Status.disconnected;
        }
    }

    public Status getStatus() {
        return status;
    }

    public boolean isConnected() {
        return status == Status.connected;
    }


    /**
     * \Brief connect to / disconnect from the service
     */
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected," + name.getPackageName() + " , " + name.getShortClassName());
            onConnected(service);
            if (null != serviceManagerIF) {
                serviceManagerIF.onConnected(service);
            }
            status = Status.connected;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected," + name.getPackageName() + " , " + name.getShortClassName());
            onDisconnected();
            if (null != serviceManagerIF) {
                serviceManagerIF.onDisconnected();
            }
            status = Status.disconnected;
        }
    };
    // connect service -- end

    public void disconnect() {
        context.unbindService(conn);
        Log.i(TAG, "unbindService");
    }
}
