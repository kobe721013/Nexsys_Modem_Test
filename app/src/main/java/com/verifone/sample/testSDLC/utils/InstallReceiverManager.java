package com.verifone.sample.testSDLC.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.Objects;

public class InstallReceiverManager {
    private static final String TAG = "InstallReceiverManager";

    private InstallReceiver mInstallReceiver;
    private InstallCallback mInstallCallback;
    private Context mContext;

    public InstallReceiverManager(Context context, InstallCallback installCallback) {
        mContext = context;
        mInstallCallback = installCallback;
    }

    public interface InstallCallback {
        void installSuccessCallback(String packageName);
    }

    private class InstallReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Receive Intent.action=" + action);

            //Intent.ACTION_PACKAGE_NEEDS_VERIFICATION
            if (Intent.ACTION_PACKAGE_ADDED.equals(action)
                    || Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
                String packageName =
                        Objects.requireNonNull(intent.getData()).getSchemeSpecificPart();
                mInstallCallback.installSuccessCallback(packageName);
            }
        }
    }

    public void registerInstallReceiver() {
        // 注册安装应用广播接收器
        if (mInstallReceiver == null) {
            mInstallReceiver = new InstallReceiver();
        }
        IntentFilter filterInstall = new IntentFilter();
        filterInstall.addAction(Intent.ACTION_PACKAGE_ADDED);
        filterInstall.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filterInstall.addDataScheme("package");

        mContext.registerReceiver(mInstallReceiver, filterInstall);
        Log.d(TAG, "registerReceiver InstallReceiver success");
    }

    public void unregisterInstallReceiver() {
        // 注销广播接收器
        if (mInstallReceiver != null) {
            Log.d(TAG, "unregisterReceiver InstallReceiver");
            mContext.unregisterReceiver(mInstallReceiver);
            mInstallReceiver = null;
        }
    }
}
