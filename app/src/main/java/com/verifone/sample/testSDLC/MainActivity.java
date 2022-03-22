package com.verifone.sample.testSDLC;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.verifone.sample.testSDLC.utils.HexDump;
import com.verifone.sample.testSDLC.utils.InstallReceiverManager;
import com.verifone.sample.testSDLC.utils.SDLCServiceManager;
import com.verifone.sample.testSDLC.utils.ServiceManager;
import com.verifone.smartpos.sdlc.aidl.SdlcListener;
import com.verifone.smartpos.sdlc.constdefine.ConstSDLCService;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TestSDLC";

    SDLCServiceManager sdlcServiceManager;

    private InstallReceiverManager installReceiver;

    TextView edLogOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate()");
        //test hex to string
        String hexstring = HexDump.dumpHexString(sam_msg_out);
        Log.d(TAG, String.format("length=[%d], hexstring=[%s]", sam_msg_out.length ,hexstring));

        setContentView(R.layout.activity_main);

        edLogOutput = findViewById(R.id.logmessage);

        sdlcServiceManager = new SDLCServiceManager(this, new ServiceManager.ServiceManagerIF() {
            @Override
            public void onBindSuccess() {
                //
                Log.d(TAG, "onBindSuccess" );
            }

            @Override
            public void onBindFails() {
                Log.e(TAG, "onBindFails" );
            }

            @Override
            public void onConnected(IBinder service) {
                Log.d(TAG, "onConnected" );

                setEnabled(R.id.btnInit, true);

            }

            @Override
            public void onDisconnected() {
                Log.w(TAG, "onDisconnected" );

            }
        });

        sdlcServiceManager.connect();

        installReceiver = new InstallReceiverManager(this, new InstallReceiverManager.InstallCallback() {
            @Override
            public void installSuccessCallback(String packageName) {
                if( packageName == sdlcServiceManager.SERVICE_PACKAGE ){
                    sdlcServiceManager.connect();
                }
            }
        });
        installReceiver.registerInstallReceiver();
    }

    void setEnabled( int id, boolean enable){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((Button) findViewById( id )).setEnabled( enable );
            }
        });
    }

    byte[] sam_msg_out = {
            //0x00, 0x4A,//length
            0x60, (byte)0x95, 0x00, 0x00, 0x00,                            			/* TPDU*/
            0x02, 0x00,                                        						/* msg_out Type ID = sale */
            0x70, 0x24, 0x05, (byte) 0x80, 0x00, (byte) 0xC0, 0x00, 0x04, /* Bit map            */
            0x16, 0x52, 0x41, 0x06, (byte)0x92, 0x79, (byte)0x99, (byte)0x81, 0x19,        /* Field 2 pan                */
            0x00, 0x00, 0x00,                                      /* Field 3 proc_code checking */
            0x00, 0x00, 0x00, 0x01, 0x00, 0x00,                    /* Field 4 tran-amt = $100.00 */
            0x00, 0x00, 0x01,                                      /* Field 11 sys_trace_num  									6 digits, 3 bytes    */
            0x22, 0x05, 	                                         /* Field 14 Expiration Date 									4 digits, 2 bytes YY MM */
            0x50, 0x11,                                            /* Field 22 Point of Service Entry Mode 				4 digits, 2 bytes  */
            0x09, 0x50,                                            /* Field 24 Network International Identifier  	3 digits, 2 bytes */
            0x00, 			                                           /* Field 25 Point of Service Condition Code   	2 digits, 1 byte  */
            0x30, 0x30, 0x30, 0x31, 0x30, 0x30, 0x30, 0x31,        /* Filed 41 Card Acceptor Terminal Identification   8 digits, 8 bytes */
            0x30, 0x30, 0x36, 0x30, 0x30, 0x36, 0x31, 0x31, 0x31, 0x31, 0x31, 0x30, 0x30, 0x30, 0x31, /* Field 42 Card Acceptor Identification Code  15 digits, 15 bytes    */
            0x00, 0x06, 0x30, 0x30, 0x30, 0x30, 0x30, 0x31				/* Field 62 Invoice/ECR Reference Number LLLVAR, n 3, then an⋯6 */

    };


//            0x60, 0x00, 0x01, 0x00, 0x00,                            /* TPDU               */
    byte[] msg_out3 = {
            0x60, 0x00, 0x01, 0x00, 0x00,                            /* TPDU               */
            0x02, 0x00,                                        /* msg_out Type ID = sale */
            0x70, 0x24, 0x05, (byte) 0x80, 0x00, (byte) 0xC0, 0x00, 0x04,            /* Bit map            */
            0x16, 0x45, 0x63, 0x01, 0x10, 0x00, 0x01, 0x77, 0x13,        /* pan                */
            0x00, 0x00, 0x00,                                        /* proc_code checking */
            0x00, 0x00, 0x00, 0x01, 0x00, 0x00,                        /* tran-amt = $123.45 */
            0x00, 0x00, 0x03,                                        /* sys_trace_num      */
            0x10, 0x12, 0x00,                                        /* time = 09:25:30    */
            0x11, 0x08,                                            /* date = 01/18       */
            0x22, 0x00,                                            /* date_exp = 12/91   */
            0x32, 0x36,                                            /* pos_entry_mode     */
            0x30, 0x30,                                            /* network_intl_id    */
            0x30,                                                /* pos_cond_code      */
            0x33, 0x33, 0x39, 0x30, 0x30, 0x30, 0x38, 0x32, 0x32, 0x30,
            0x31, 0x38, 0x38, 0x38, 0x30, 0x30, 0x30, 0x31, 0x00, 0x06,
            0x30, 0x30, 0x30, 0x30, 0x30, 0x33

    };

    SdlcListener sdlcListener;

    public void btnBind(View view) {
        if( sdlcServiceManager.getStatus() != ServiceManager.Status.connected ){
            sdlcServiceManager.connect();
        }

    }

    int loop_count = 0;

    public void btnInit(View view) {
        setEnabled( R.id.btnInit, false );
        new Thread(new Runnable() {
            @Override
            public void run() {
                dial_Init();
            }
        }).start();

    }
    public void dial_Init() {

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
                            Log.i(TAG, "onConnect: "+ autoDialstep );
                            log_output("Line - Connected");
                            if( autoDialstep == 1 ){
                                // call send
                                Message msg = new Message();
                                msg.getData().putString("type", "value" );
                                sdlc_auto_test.sendMessage(msg);

                            } else {
                                setEnabled( R.id.btnRecv, true );
                                setEnabled( R.id.btnSend, true );

                                setEnabled( R.id.btnHangup, true );

                                setEnabled( R.id.btnDial, false );
                            }

                        }

                        @Override
                        public void onDisconect() throws RemoteException {
                            Log.i(TAG, "onDisconect, loop count: " + loop_count );
                            log_output("Line - hanguped");
                            autoDialstep = 0;

                            if( loop_count > 1 ) {
                                try {
                                    Thread.sleep( 1000 );
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                autoDialstep = 1;

                                dial_dial();

                            } else {
                                setEnabled( R.id.btnSend, false );
                                setEnabled( R.id.btnRecv, false );
                                setEnabled( R.id.btnHangup, false );

                                setEnabled( R.id.btnDial, true );
                                setEnabled( R.id.btnAuto, true );

                            }
                        }

                        @Override
                        public void onFail(int code, String message) throws RemoteException {
                            Log.e(TAG, "onFail: " + code + ", "+ message );
                            log_output( "Line - fail: " + code + ", message: " + message );
                            autoDialstep = 0;
                        }
                    };
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "not connect with SDLC service");
        }

        setEnabled( R.id.btnAuto, true );
        setEnabled( R.id.btnDial, true );
        setEnabled( R.id.btnInit, true);

    }

    public void btnDeinit(View view) {
        if( sdlcServiceManager.isConnected() ){
            Bundle bundle = new Bundle();
            try {
                sdlcServiceManager.getIsdlcService().deinit( bundle );
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        setEnabled( R.id.btnSend, false );
        setEnabled( R.id.btnRecv, false );
        setEnabled( R.id.btnHangup, false );

        setEnabled( R.id.btnDial, false );

    }

    public void btnDial(View view) {
        setEnabled( R.id.btnDial, false );
        setEnabled( R.id.btnHangup, true );

        setEnabled( R.id.btnAuto, false );

        new Thread(new Runnable() {
            @Override
            public void run() {
                dial_dial();
            }
        }).start();

    }
    void dial_dial() {

        if( sdlcServiceManager.isConnected() ){
            Bundle bundle = new Bundle();
            String number = ((EditText)findViewById(R.id.edNumber)).getText().toString();
            bundle.putString( "number",  number);
            bundle.putInt( "timeout", 100 );

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
                log_output(String.format("Dialing to [%s]",number ));
                sdlcServiceManager.getIsdlcService().dialup(bundle, sdlcListener );
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }

    }

    byte[] msg_sent;

    public void btnSend(View view) {
        setEnabled( R.id.btnSend, false );
        setEnabled( R.id.btnRecv, false );

        new Thread(new Runnable() {
            @Override
            public void run() {
                dial_send();
            }
        }).start();
    }
    void dial_send() {
        if( sdlcServiceManager.isConnected() ){
            try {
                log_output("Sending");
//                int fixedLength = Integer.valueOf( ( (EditText) findViewById( R.id.edFixedSize) ).getText().toString() );
//                if( fixedLength == 0  ){
//                    fixedLength = msg_out3.length;
//                }
//                msg_sent = new byte[fixedLength];
//                System.arraycopy( msg_out3, 0, msg_sent, 0, msg_out3.length );
//                if( msg_sent.length > msg_out3.length ){
//                    byte j='a';
//                    for( int i = msg_out3.length; i< msg_sent.length; i++){
//                        msg_sent[i] = (byte) (j);
//                        if( ++j == 'z') {
//                            j = 'a';
//                        }
//
//                    }
//                }

                int resultCode = sdlcServiceManager.getIsdlcService().send( sam_msg_out );
                Log.d(TAG, "send resultcode="+resultCode);

                setEnabled( R.id.btnSend, true );
                setEnabled( R.id.btnRecv, true );

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void btnRecv(View view) {
        setEnabled( R.id.btnSend, false );
        setEnabled( R.id.btnRecv, false );

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        dial_recv();
                    }
                }
        ).start();
    }
    void dial_recv(){
        if( sdlcServiceManager.isConnected() ){
            log_output("Receiving ...");

            Bundle bundle = new Bundle();
            try {
                byte[] buffer = new byte[512];
                int ret = sdlcServiceManager.getIsdlcService().receive( buffer, buffer.length, 30 );
                log_output( "Line - received return: " + ret );
                if( ret > 0 ){
                        String responseString = HexDump.dumpHexString(buffer);
                        log_output(responseString);
//                    Log.i(TAG, "receive length: " + ret);
//                    byte[] out = new byte[ ret-5 ];
//                    System.arraycopy(buffer, 5, out, 0, ret-5 );
//                    Log.d(TAG, "Buffer size: " + out.length );
//                    Log.v(TAG, "read text: " + HexDump.dumpHexString( out ));
//
//                    byte[] msg = new byte[msg_sent.length-5];
//                    System.arraycopy( msg_sent, 5, msg, 0, msg_sent.length-5);
//
//                    if( Arrays.equals( out, msg) ) {
//                        Log.d(TAG, "Received - pass");
//                        log_output( "Received - pass" );
//                    } else {
//                        Log.e(TAG, "Received - Data fails");
//                        log_output( "Received - Data fails" );
//                    }
                } else if( ret == 0 ){
                    Log.e(TAG, "receive timeout");
                    String responseString = HexDump.dumpHexString(buffer);
                    log_output(responseString);
                } else {
                    Log.e(TAG, "Error happen: " + ret );
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        setEnabled( R.id.btnSend, true );
        setEnabled( R.id.btnRecv, true );
    }

    public void btnHangup(View view) {
        setEnabled( R.id.btnDial, true );
        setEnabled( R.id.btnSend, false );
        setEnabled( R.id.btnRecv, false );

        new Thread(new Runnable() {
            @Override
            public void run() {
                dial_hangup();
            }
        }).start();

    }

    void dial_hangup() {
        if( sdlcServiceManager.isConnected() ){
            log_output( "hang up ..." );
            Bundle bundle = new Bundle();
            try {
                sdlcServiceManager.getIsdlcService().hangup( sdlcListener );
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    int autoDialstep = 0;
    int repeat_count = 0;
    public void btnDialAuto(View view) {

        setEnabled( R.id.btnDial, false );
        setEnabled( R.id.btnAuto, false );

        loop_count = Integer.valueOf (  ((EditText)findViewById( R.id.edLoop)).getText().toString() );
        repeat_count = Integer.valueOf (  ((EditText)findViewById( R.id.edRepeat)).getText().toString() );
        new Thread(new Runnable() {
            @Override
            public void run() {
                dial_dial();
                autoDialstep = 1;

            }
        }).start();
    }


    Handler sdlc_auto_test = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    int repeat = repeat_count;
                    do {

                        Log.i(TAG, "call send");
                        dial_send();
                        autoDialstep = 2;

                        int delay = Integer.valueOf(((EditText) findViewById(R.id.edDelay)).getText().toString());
                        if (delay > 0) {
                            try {
                                Thread.sleep(delay * 1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        Log.i(TAG, "call recv");
                        dial_recv();
                        autoDialstep = 2;
                    } while ((--repeat) > 0);

                    autoDialstep = 3;
                    dial_hangup();


                    --loop_count;

                }
            }).start();
        }
    };

    void log_output(String message){
        runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
//        StringBuffer text = new StringBuffer(edLogOutput.getText());
//        edLogOutput.setText(message + "\n");
//        edLogOutput.append(text);
                              edLogOutput.append("\n");
                              edLogOutput.append( message );

                          }
                      });
        }

}