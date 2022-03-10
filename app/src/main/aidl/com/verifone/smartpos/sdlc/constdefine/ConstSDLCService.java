package com.verifone.smartpos.sdlc.constdefine;

/**
 * Created by Simon on 2021/6/17
 */
public class ConstSDLCService {

    public static int ERR_CODE_ = -1;

    public static int ERR_CODE_DIALUP = -1000;
    public static int ERR_CODE_DIALUP_No_Number = ERR_CODE_DIALUP -1;

    public static class init{
        public static String logLevel = "logLevel";
        public static String patch = "patch";
        public static String mdmVer = "mdmVer";
        public static String mdmFW  = "mdmFw";
    }



    public class dialup {
        public static final String bundle_number_string ="number";
        public static final String bundle_timeout_int ="timeout";
        public static final String bundle_fixed_at_command_list ="FixedAtCmdList";
        public static final String bundle_fixed_atd_command ="FixedAtdCmd";
    }
}
