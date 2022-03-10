// SdlcListener.aidl
package com.verifone.smartpos.sdlc.aidl;

// Declare any non-default types here with import statements

interface SdlcListener {
    void onConnect();  // for dialup
    void onDisconect(); // for hungup
    void onFail(int code, String message);

}