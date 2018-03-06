package com.rts.dcmote.dcmote;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.reflect.Method;


public class WifiAP extends Activity {
    private static final int WIFI_AP_STATE_UNKNOWN = -1;
    public static String myssid;
    public static String mypw;
    private static int constant = 0;
    private static int WIFI_AP_STATE_DISABLING = 0;
    private static int WIFI_AP_STATE_DISABLED = 1;
    private static int WIFI_AP_STATE_FAILED = 4;
    private final String[] WIFI_STATE_TEXTSTATE = new String[]{
            "DISABLING", "DISABLED", "ENABLING", "ENABLED", "FAILED"

    };
    public int WIFI_AP_STATE_ENABLING = 2;
    public int WIFI_AP_STATE_ENABLED = 3;
    SQLiteDatabase dcmote;
    String hSSID, hPW;
    private WifiManager wifi;
    private String TAG = "WifiAP";

    private int stateWifiWasIn = -1;

    private boolean alwaysEnableWifi = false; //set to false if you want to try and set wifi state back to what it was before wifi ap enabling, true will result in the wifi always being enabled after wifi ap is disabled

    /**
     * Toggle the WiFi AP state
     *
     * @param wifihandler
     * @author http://stackoverflow.com/a/7049074/1233435
     */
    public void toggleWiFiAP(WifiManager wifihandler, Context context) {
        if (wifi == null) {
            wifi = wifihandler;
        }

        boolean wifiApIsOn = getWifiAPState() == WIFI_AP_STATE_ENABLED || getWifiAPState() == WIFI_AP_STATE_ENABLING;
        new SetWifiAPTask(!wifiApIsOn, false, context).execute();
    }

    /**
     * Enable/disable wifi
     *
     * @param true or false
     * @return WifiAP state
     * @author http://stackoverflow.com/a/7049074/1233435
     */


    private int setWifiApEnabled(boolean enabled) {

        //   Cursor hp =dcmote.rawQuery("SELECT * FROM hostpot",null );
        //   if(hp.getCount()>0){Log.w("DCmote", "Have a  created host pot   ");
        //       if(hp.moveToFirst()) {
        //           Log.w("DCmote", "ssid IS..............." + hp.getString(0));
        // /          //  Log.w("DCmote", "Device is found but this is null ");
        //           hSSID = hp.getString(0);
        //           hPW = hp.getString(1);
        //           Log.w("HOSTPOT", "ssid ----- " + hSSID);
        //           Log.w("HOSTPOT", "pw ----- " + hPW);
        //
        //       }}


        Log.d(TAG, "*** setWifiApEnabled CALLED **** " + enabled);

        WifiConfiguration config = new WifiConfiguration();
        //  WifiAP.myssid="rio";
        //  WifiAP.mypw="rioset";

        hSSID = WifiAP.myssid;
        hPW = WifiAP.mypw;


        config.BSSID = "hh";
        config.SSID = hSSID;
        config.hiddenSSID = true;

        config.preSharedKey = hPW;

        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        //   config.allowedKeyManagement.set(WPA_PSK );
        //remember wirelesses current state
        if (enabled && stateWifiWasIn == -1) {
            stateWifiWasIn = wifi.getWifiState();
        }

        //disable wireless
        if (enabled && wifi.getConnectionInfo() != null) {
            Log.d(TAG, "disable wifi: calling");
            //   wifi.setWifiEnabled(false);
            //  int loopMax = 10;
            //   while(loopMax>0 && wifi.getWifiState()!=WifiManager.WIFI_STATE_DISABLED){
            //    Log.d(TAG, "disable wifi: waiting, pass: " + (10-loopMax));
            //     try {
            //       //  Thread.sleep(500);
            // loopMax--;
            //     } catch (Exception e) {

            //     }
            //  }
            //   Log.d(TAG, "disable wifi: done, pass: " + (10-loopMax));
        }

        //enable/disable wifi ap
        int state = WIFI_AP_STATE_UNKNOWN;
        try {
            Log.d(TAG, (enabled ? "enabling" : "disabling") + " wifi ap: calling");
            //  wifi.setWifiEnabled(false);
            Method method1 = wifi.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            //method1.invoke(wifi, null, enabled); // true
            method1.invoke(wifi, config, enabled); // true
            Method method2 = wifi.getClass().getMethod("getWifiApState");
            state = (Integer) method2.invoke(wifi);
        } catch (Exception e) {
            Log.e(WIFI_SERVICE, e.getMessage());
            // toastText += "ERROR " + e.getMessage();
        }


        //hold thread up while processing occurs
        if (!enabled) {
            int loopMax = 10;
            while (loopMax > 0 && (getWifiAPState() == WIFI_AP_STATE_DISABLING || getWifiAPState() == WIFI_AP_STATE_ENABLED || getWifiAPState() == WIFI_AP_STATE_FAILED)) {
                Log.d(TAG, (enabled ? "enabling" : "disabling") + " wifi ap: waiting, pass: " + (10 - loopMax));
                try {
                    Thread.sleep(500);
                    loopMax--;
                } catch (Exception e) {

                }
            }
            Log.d(TAG, (enabled ? "enabling" : "disabling") + " wifi ap: done, pass: " + (10 - loopMax));

            //enable wifi if it was enabled beforehand
            //this is somewhat unreliable and app gets confused and doesn't turn it back on sometimes so added toggle to always enable if you desire
            if (stateWifiWasIn == WifiManager.WIFI_STATE_ENABLED || stateWifiWasIn == WifiManager.WIFI_STATE_ENABLING || stateWifiWasIn == WifiManager.WIFI_STATE_UNKNOWN || alwaysEnableWifi) {
                Log.d(TAG, "enable wifi: calling");
                wifi.setWifiEnabled(true);
                //don't hold things up and wait for it to get enabled
            }

            stateWifiWasIn = -1;
        } else if (enabled) {
            int loopMax = 10;
            while (loopMax > 0 && (getWifiAPState() == WIFI_AP_STATE_ENABLING || getWifiAPState() == WIFI_AP_STATE_DISABLED || getWifiAPState() == WIFI_AP_STATE_FAILED)) {
                Log.d(TAG, (enabled ? "enabling" : "disabling") + " wifi ap: waiting, pass: " + (10 - loopMax));
                try {
                    Thread.sleep(500);
                    loopMax--;
                } catch (Exception e) {

                }
            }
            Log.d(TAG, (enabled ? "enabling" : "disabling") + " wifi ap: done, pass: " + (10 - loopMax));
        }
        return state;
    }

    /**
     * Get the wifi AP state
     *
     * @return WifiAP state
     * @author http://stackoverflow.com/a/7049074/1233435
     */
    public int getWifiAPState() {
        int state = WIFI_AP_STATE_UNKNOWN;
        try {
            Method method2 = wifi.getClass().getMethod("getWifiApState");
            state = (Integer) method2.invoke(wifi);
        } catch (Exception e) {

        }

        if (state >= 10) {
            //using Android 4.0+ (or maybe 3+, haven't had a 3 device to test it on) so use states that are +10
            constant = 10;
        }

        //reset these in case was newer device
        WIFI_AP_STATE_DISABLING = 0 + constant;
        WIFI_AP_STATE_DISABLED = 1 + constant;
        WIFI_AP_STATE_ENABLING = 2 + constant;
        WIFI_AP_STATE_ENABLED = 3 + constant;
        WIFI_AP_STATE_FAILED = 4 + constant;

        Log.d(TAG, "getWifiAPState.state " + (state == -1 ? "UNKNOWN" : WIFI_STATE_TEXTSTATE[state - constant]));
        return state;
    }

    /**
     * the AsyncTask to enable/disable the wifi ap
     *
     * @author http://stackoverflow.com/a/7049074/1233435
     */
    class SetWifiAPTask extends AsyncTask<Void, Void, Void> {
        boolean mMode; //enable or disable wifi AP
        boolean mFinish; //finalize or not (e.g. on exit)
        ProgressDialog d;

        /**
         * enable/disable the wifi ap
         *
         * @param mode    enable or disable wifi AP
         * @param finish  finalize or not (e.g. on exit)
         * @param context the context of the calling activity
         * @author http://stackoverflow.com/a/7049074/1233435
         */
        public SetWifiAPTask(boolean mode, boolean finish, Context context) {
            mMode = mode;
            mFinish = finish;
            d = new ProgressDialog(context);
        }

        /**
         * do before background task runs
         *
         * @author http://stackoverflow.com/a/7049074/1233435
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //  d.setTitle("Turning WiFi AP " + (mMode?"on":"off") + "...");
            //  d.setMessage("...please wait a moment.");
            //  d.show();
        }

        /**
         * do after background task runs
         *
         * @param aVoid
         * @author http://stackoverflow.com/a/7049074/1233435
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                d.dismiss();
                //  Wifi.updateStatusDisplay();
            } catch (IllegalArgumentException e) {

            }
            if (mFinish) {
                finish();
            }
        }

        /**
         * the background task to run
         *
         * @param params
         * @author http://stackoverflow.com/a/7049074/1233435
         */
        @Override
        protected Void doInBackground(Void... params) {
            setWifiApEnabled(mMode);
            return null;
        }
    }
}