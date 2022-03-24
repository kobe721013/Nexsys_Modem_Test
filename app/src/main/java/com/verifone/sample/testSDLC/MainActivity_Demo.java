package com.verifone.sample.testSDLC;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.verifone.sample.testSDLC.utils.HexDump;
import com.verifone.sample.testSDLC.utils.KrNrModemManager;

public class MainActivity_Demo extends AppCompatActivity {

    private String TAG="MainActivity_Demo";
    private KrNrModemManager krnrmodemManager;

    private TextView edLogOutput;
    private TextView tvLogMessage;

    int reSendCount = 1;
    static public byte[] sam_msg_out = {
            0x60, 0x09, 0x50, 0x00, 0x00,                            			/* TPDU*/
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

    private KrNrModemManager.KrNrModemManagerDelegate managerDelegate = new KrNrModemManager.KrNrModemManagerDelegate() {
        @Override
        public void onDialConnected() {
            log_output("onDialConnected() event.");
            log_output("send test data");
            krnrmodemManager.send(sam_msg_out);
        }

        @Override
        public void onDialDisconnected() {

            log_output("onDialDisconnected() event.");
            setDialButton(true);
        }

        @Override
        public void onDialFailed(int code, String message) {
            log_output(String.format("onDialFailed() event. code=%d. message=%s",code, message));
            setDialButton(true);
        }

        @Override
        public void onSendDone(int resultCode) {
            log_output(String.format("onSendDone() event. resultCode=%d., reSendCount=%d",resultCode, reSendCount));
            reSendCount--;
        }


        @Override
        public void onReceive(byte[] data) {
            String receiveHexString = HexDump.dumpHexString(data);
            log_output(String.format("onReceive() event. receiveHexString=%s.",receiveHexString));

            if(reSendCount >=0)
            {
                //simulation: send twice data in a row.
                Log.d(TAG, "reSend data again");
                krnrmodemManager.send(sam_msg_out);
            }
            else
            {
                Log.d(TAG, "No data needed to send, hangup it..");
                krnrmodemManager.hangup();
            }

        }

        @Override
        public void onErrors(String errorMessage) {
            log_output(String.format("onErrors() event. errorMessage=%s.",errorMessage));
            setDialButton(true);
        }
    };

    void log_output(String message){

        Log.d(TAG, message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvLogMessage.append("\n");
                tvLogMessage.append( message );

            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        krnrmodemManager = KrNrModemManager.getInstance(getApplicationContext());
        setDialButton(true);
        edLogOutput = findViewById(R.id.textViewMessage);
        tvLogMessage = findViewById(R.id.logmessage);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "register modem delegate");
        krnrmodemManager.delegate = managerDelegate;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy(), unRegister modem delegate.");
        krnrmodemManager.delegate = null;
    }

    public void btnClose(View view)
    {
        finish();
    }

    public  void btnDial(View view)
    {
        String number = "026310152";
        Log.d(TAG, "Dial buton click");
        log_output(String.format("Dial to [%s]",number));
        setDialButton(false);
        krnrmodemManager.dialTo(number);

        //start second activity
        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
    }

    public  void btnHangup(View view)
    {
        Log.d(TAG, "Dial buton click");
        setDialButton(false);
        krnrmodemManager.hangup();
    }

    void setDialButton(boolean enable)
    {
        setEnabled(R.id.btnDial, enable);
        setEnabled(R.id.btnHangup, !enable);
    }

    void setEnabled( int id, boolean enable){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((Button) findViewById( id )).setEnabled( enable );
            }
        });
    }
}