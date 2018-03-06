package com.rts.dcmote.dcmote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import java.lang.reflect.Method;


public class Wifi extends Activity {


    public static String myssid;
    public static String mypw;
    static WifiAP wifiAp;
    static Button btnWifiToggle;
    boolean wasAPEnabled = false;
    SQLiteDatabase dcmote;
    Button btnsavewifi1, btnsaveip1, btnhostpot;
    private WifiManager wifi;
    private EditText editTextIPAddress, editTextPortNumber, essid, epw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi);
        dcmote = openOrCreateDatabase("dcm", Context.MODE_PRIVATE, null);

        wifiAp = new WifiAP();
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        dcmote.execSQL("CREATE TABLE IF NOT EXISTS cmode(conmode VARCHAR,mode VARCHAR);");
        dcmote.execSQL("CREATE TABLE IF NOT EXISTS ip(IP VARCHAR,PORT VARCHAR );");
        dcmote.execSQL("CREATE TABLE IF NOT EXISTS hostpot(hssid VARCHAR , hpw VARCHAR); ");
        btnsavewifi1 = (Button) findViewById(R.id.btnsavewifi);
        btnsaveip1 = (Button) findViewById(R.id.btnsaveip);
        editTextIPAddress = (EditText) findViewById(R.id.editTextIPAddress);
        editTextPortNumber = (EditText) findViewById(R.id.editTextPortNumber);
        essid = (EditText) findViewById(R.id.editSSID);
        epw = (EditText) findViewById(R.id.editPW);
        btnhostpot = (Button) findViewById(R.id.btnhostpot);

        Cursor ip = dcmote.rawQuery("SELECT * FROM ip", null);

        Log.w("ipcount", "ip count is" + ip.getCount());


        btnhostpot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (essid.getText().toString().trim().length() == 0) {
                    showMessage("Error", "Please enter SSID and Password  ");
                    return;
                }
                Cursor hp = dcmote.rawQuery("SELECT * FROM hostpot ", null);
                if (hp.moveToFirst()) {
                    String ssid = hp.getString(0);
                    dcmote.execSQL("UPDATE hostpot SET hssid='" + essid.getText() + "',hpw='" + epw.getText() +
                            "' WHERE hssid='" + ssid + "'");
                    showMessage("Success", " Hostpot created ");
                    Log.w("Hostpot", "SSID..............." + hp.getString(0));

                    ////// hostpoton();


                } else {
                    showMessage("Error", "Invalid Hostpot");
                }
            }
        });

        btnsaveip1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (editTextIPAddress.getText().toString().trim().length() == 0) {
                    showMessage("Error", "Please enter IP adn Port  ");
                    return;
                }
                Cursor c = dcmote.rawQuery("SELECT * FROM ip ", null);
                if (c.moveToFirst()) {
                    String ip = c.getString(0);
                    dcmote.execSQL("UPDATE ip SET IP='" + editTextIPAddress.getText() + "',PORT='" + editTextPortNumber.getText() +
                            "' WHERE IP='" + ip + "'");
                    showMessage("Success", "IP Modified");
                    Log.w("DCmote", "IP..............." + c.getString(0));
                } else {
                    showMessage("Error", "Invalid IP");
                }
            }
        });


        btnsavewifi1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dcmote.execSQL("UPDATE cmode SET conmode='WIFI'");
                Cursor mode = dcmote.rawQuery("SELECT * FROM cmode", null);
                //
                if (mode.getCount() > 0) {
                    Log.w("DCmote", "Have a  some device  ");
                    if (mode.moveToFirst()) {
                        Log.w("DCmote", "Device..............." + mode.getString(0));
                        //  Log.w("DCmote", "Device is found but this is null ");

                        //   c.getString(0)
                    }
                }
                //    Log.w("mode", "mode updated to " +mode.getString(0));

            }
        });
    }

    public void hostpoton() {

        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);

        Method[] methods = wifiManager.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals("setWifiApEnabled")) {
                try {
                    method.invoke(wifiManager, null, false);
                } catch (Exception ex) {
                }
                break;
            }
        }


        Cursor hp = dcmote.rawQuery("SELECT * FROM hostpot", null);
        if (hp.getCount() > 0) {
            Log.w("DCmote", "Have a  created host pot   ");
            if (hp.moveToFirst()) {
                Log.w("DCmote", "ssid IS..............." + hp.getString(0));
                //  Log.w("DCmote", "Device is found but this is null ");

                WifiAP.myssid = hp.getString(0);
                WifiAP.mypw = hp.getString(1);
                Log.w("HOSTPOT", "ssid ----- " + hp.getString(0));
                Log.w("HOSTPOT", "pw ----- " + hp.getString(1));

            }
        }


        Log.w("WIFISTART ", "canbe somthing");
        wifiAp.toggleWiFiAP(wifi, Wifi.this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_DIM_BEHIND);


    }


    @Override
    public void onResume() {
        super.onResume();
        if (wasAPEnabled) {
            if (wifiAp.getWifiAPState() != wifiAp.WIFI_AP_STATE_ENABLED && wifiAp.getWifiAPState() != wifiAp.WIFI_AP_STATE_ENABLING) {
                //   wifiAp.toggleWiFiAP(wifi, Wifi.this);
                Log.w("wifi status", "idont ");
            }
        }

    }

    //  @Override
    public void onPause() {
        super.onPause();
        boolean wifiApIsOn = wifiAp.getWifiAPState() == wifiAp.WIFI_AP_STATE_ENABLED || wifiAp.getWifiAPState() == wifiAp.WIFI_AP_STATE_ENABLING;
        if (wifiApIsOn) {
            wasAPEnabled = true;
            Log.w("wifi status", "idont true ");
            wifiAp.toggleWiFiAP(wifi, Wifi.this);
        } else {
            wasAPEnabled = false;
            Log.w("wifi status", "idont false  ");
        }

    }


    public void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }


}