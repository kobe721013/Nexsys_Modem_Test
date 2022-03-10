package com.verifone.sample.testSDLC.utils;

import android.content.Context;
import android.util.Log;

public class SystemServiceManager extends ServiceManager {
    private static final String TAG = "SystemServiceManager";
    public static final String ACTION_SYSSERVICE = "com.vfi.smartpos.system_service";
    public static final String PACKAGE_SYSSERVICE = "com.vfi.smartpos.system_service";
    public static final String CLASSNAME_SYSSERVICE = "com.vfi.smartpos.system_service.SystemService";

    public SystemServiceManager(Context context) {
        super(context, PACKAGE_SYSSERVICE, CLASSNAME_SYSSERVICE, ACTION_SYSSERVICE,
                null);
        Log.d(TAG, "Create connection to System Service ");
    }

    public SystemServiceManager(Context context, ServiceManagerIF serviceManagerIF) {
        super(context, PACKAGE_SYSSERVICE, CLASSNAME_SYSSERVICE, ACTION_SYSSERVICE,
                serviceManagerIF);
        Log.d(TAG, "Create to connection to System Service ");
    }
}
