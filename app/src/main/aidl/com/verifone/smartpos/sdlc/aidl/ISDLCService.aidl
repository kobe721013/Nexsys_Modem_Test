// ISDLCService.aidl
package com.verifone.smartpos.sdlc.aidl;

// Declare any non-default types here with import statements

import com.verifone.smartpos.sdlc.aidl.SdlcListener;

interface ISDLCService {

    /**
         * init the sdlc, load modem profile, load new sdlc patch, open device, etc.
         * @param bundle
         * profile(String), the file of the modem profile to load
         * patch (String), the patch (full filename can be access by other apk) want to be loaded. By default, it check the /sdcard/verifone/sdlc/*.patch, and change the filename to append .load means the patch was loaded.
         * logLevel(int), Log.ASSERT = 7; ERROR = 6; WARN = 5; INFO = 4; DEBUG = 3; VERBOSE = 2;  Suggest using WARN on deloy and DEBUG on developing
         * mdmFw (String), load modem profile
         * mdmVer (String), modem profile version to check

         */
    	int init( in Bundle bundle );
        /**
         * @param bundle
         * number (String)
         * timeout (int), second
         * FixedAtCmdList(StringArrayList), the fixed at command send before dialup
         * FixedAtdCmd (String), the fixed ATD command to do the dialup
         * listener listener tell the dial up result
         */
    	int dialup(in Bundle bundle, SdlcListener listener);

        /**
         */
    	int send( in byte[] data);

        /**
         * @param buffer, the buffer received
         * @param expectLen, the length expected
         * @param timeout
         * @return >0 the length received, 0 timeout, <0 error happend
         */
        int receive(inout byte[] buffer, int expectLen, int timeout);

    	int hangup( SdlcListener listener );

        /**
         * close the device
         * need do the init after deinit.
         */
    	int deinit( in Bundle bundle );

}