package com.rts.dcmote.dcmote;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;


public class Lunch extends Activity implements View.OnClickListener {

    public final static String PREF_IP = "PREF_IP_ADDRESS";
    public final static String PREF_PORT = "PREF_PORT_NUMBER";
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static String myssid;
    public static String mypw;
    public static String EXTRA_ADDRESS = "device_address";
    static WifiAP wifiAp;
    static Button btnWifiToggle;
    private final int SPEECH_REQUEST_CODE = 123;
    ImageView btnhome, btnsetup, btnabout;
    SQLiteDatabase dcmote;
    Context x;
    boolean wasAPEnabled = false;
    String address = null;
    String MODE = null;
    String IP = null;
    String PORT = null;
    String PV = null;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    TextToSpeech tts;
    String parameterValue = "";
    String hSSID, hPW;
    private WifiManager wifi;
    private ProgressDialog progress;
    private boolean isBtConnected = false;
    private TextView speech_output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lunch);
        btnhome = (ImageView) findViewById(R.id.btnconect);
        btnabout = (ImageView) findViewById(R.id.btnabout);
        btnsetup = (ImageView) findViewById(R.id.btnsetup);
        speech_output = (TextView) findViewById(R.id.speech_output);
        btnhome.setOnClickListener(this);
        btnsetup.setOnClickListener(this);
        btnabout.setOnClickListener(this);
        wifiAp = new WifiAP();
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);


        dbcalling();


        tts = new TextToSpeech(Lunch.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.US);
                }
            }
        });
    }

    private void dbcalling() {

        dcmote = openOrCreateDatabase("dcm", Context.MODE_PRIVATE, null);
        dcmote.execSQL("CREATE TABLE IF NOT EXISTS switch(sname VARCHAR,cmd VARCHAR,icon VARCHAR );");
        dcmote.execSQL("CREATE TABLE IF NOT EXISTS device(dname VARCHAR,d1 VARCHAR,d2 VARCHAR );");
        dcmote.execSQL("CREATE TABLE IF NOT EXISTS cmode(conmode VARCHAR,mode VARCHAR);");
        dcmote.execSQL("CREATE TABLE IF NOT EXISTS ip(IP VARCHAR,PORT VARCHAR );");
        dcmote.execSQL("CREATE TABLE IF NOT EXISTS hostpot(hssid VARCHAR , hpw VARCHAR); ");
        dcmote.execSQL("CREATE TABLE IF NOT EXISTS iot(email VARCHAR,server VARCHAR);");
        Cursor c = dcmote.rawQuery("SELECT * FROM device", null); /////// this is bt device list
        Cursor cmode = dcmote.rawQuery("SELECT * FROM cmode ", null); //// this is wifi , internet or bluetooth mode
        Cursor ip = dcmote.rawQuery("SELECT * FROM ip", null);
        Cursor hp = dcmote.rawQuery("SELECT * FROM hostpot ", null);
        Cursor iot = dcmote.rawQuery("SELECT * FROM iot ", null);

        //  WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);

        //  Method[] methods = wifiManager.getClass().getDeclaredMethods();
        //  for (Method method : methods) {
        //    if (method.getName().equals("setWifiApEnabled")) {
        //       try {
        //           method.invoke(wifiManager, null, false);
        //       } catch (Exception ex) {
        //      }
        //      break;
        //   }
        //  }

        if (iot.getCount() == 0) {
            dcmote.execSQL("INSERT INTO iot VALUES('no','no'); ");
            Log.w("iot", "iot created ");
        }

        if (hp.getCount() == 0) {
            dcmote.execSQL("INSERT INTO hostpot VALUES('no','no'); ");
            Log.w("HOSTPOT", "hostpot created ");
        }

        if (cmode.getCount() == 0) {
            dcmote.execSQL("INSERT INTO cmode VALUES('nomode','nomode');");
            Log.w("MODE", "mode created ");
        }
        if (ip.getCount() == 0) {
            dcmote.execSQL("INSERT INTO ip VALUES('noip','noport');");
            Log.w("MODE", "mode created ");

        }
        if (c.getCount() == 0) {
            Log.w("DCmote", "No Device Found");                  //////// check have a data
            dcmote.execSQL("INSERT INTO device VALUES('Device','Device','Device');");   //// insert data if no avilable data
            Log.w("DCmote", "Insert first First Data ");
        }


        Cursor mode = dcmote.rawQuery("SELECT * FROM cmode", null);
        //
        if (mode.getCount() > 0) {
            Log.w("DCmote", "Have a  created mode  ");
            if (mode.moveToFirst()) {
                Log.w("DCmote", "MODE IS..............." + mode.getString(0));
                //  Log.w("DCmote", "Device is found but this is null ");
                MODE = mode.getString(0);
                Log.w("MODE", "Mode ----- " + MODE);
                switch (MODE) {
                    case "WIFI":
                        Log.w("MODE", "This Mode Is WIFI---" + MODE);
                        Cursor ipport = dcmote.rawQuery("SELECT * FROM ip ", null);
                        if (ipport.moveToFirst()) {
                            IP = ipport.getString(0);
                            PORT = ipport.getString(1);

                            Log.w("IP Mode", "Your IP---" + IP + "-- Your Port--" + PORT);
                            parameterValue = "/on";
                            //  hostpoton();

                        }
                        break;
                    case "BT":
                        Log.w("MODE", "This Mode Is BT---" + MODE);
                        BluetoothAdapter.getDefaultAdapter().enable();
                        break;
                }
            }
        }

    }

    public void hostpoton() {

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
        wifiAp.toggleWiFiAP(wifi, Lunch.this);
        // getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|WindowManager.LayoutParams.FLAG_DIM_BEHIND);


    }


    @Override
    public void onResume() {
        super.onResume();
        if (wasAPEnabled) {
            if (wifiAp.getWifiAPState() != wifiAp.WIFI_AP_STATE_ENABLED && wifiAp.getWifiAPState() != wifiAp.WIFI_AP_STATE_ENABLING) {
                //  wifiAp.toggleWiFiAP(wifi, Lunch.this);
                Log.w("wifi status", "idont ");
            }
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        boolean wifiApIsOn = wifiAp.getWifiAPState() == wifiAp.WIFI_AP_STATE_ENABLED || wifiAp.getWifiAPState() == wifiAp.WIFI_AP_STATE_ENABLING;
        //    if (wifiApIsOn) {
        //        wasAPEnabled = true;
        //        Log.w("wifi status", "idont true ");
        //       wifiAp.toggleWiFiAP(wifi, Lunch.this);
        //    } else {
        //        wasAPEnabled = false;
        //        Log.w("wifi status", "idont false  ");
        //    }

    }


    /**
     * Description: Send an HTTP Get request to a specified ip address and port.
     * Also send a parameter "parameterName" with the value of "parameterValue".
     *
     * @param parameterValue the pin number to toggle
     * @param ipAddress      the ip address to send the request to
     * @param portNumber     the port number of the ip address
     * @param parameterName
     * @return The ip address' reply text, or an ERROR message is it fails to receive one
     */
    public String sendRequest(String parameterValue, String ipAddress, String portNumber, String parameterName) {
        String serverResponse = "ERROR";

        try {

            HttpClient httpclient = new DefaultHttpClient(); // create an HTTP client
            // define the URL e.g. http://myIpaddress:myport/?pin=13 (to toggle pin 13 for example)
            //   URI website = new URI("http://"+ipAddress+":"+portNumber+"/?"+parameterName+"="+parameterValue);
            //   URI website = new URI("http://"+ipAddress+":"+portNumber+"/on");
            //   URI website = new URI("http://"+IP+":"+PORT+PV);
            URI website = new URI("http://" + IP + ":" + portNumber + "/" + PV + "");
            HttpGet getRequest = new HttpGet(); // create an HTTP GET object
            getRequest.setURI(website); // set the URL of the GET request
            HttpResponse response = httpclient.execute(getRequest); // execute the request
            // get the ip address server's reply
            InputStream content = null;
            content = response.getEntity().getContent();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    content
            ));
            serverResponse = in.readLine();
            // Close the connection
            content.close();
        } catch (ClientProtocolException e) {
            // HTTP error
            serverResponse = e.getMessage();
            e.printStackTrace();
        } catch (IOException e) {
            // IO error
            serverResponse = e.getMessage();
            e.printStackTrace();
        } catch (URISyntaxException e) {
            // URL syntax error
            serverResponse = e.getMessage();
            e.printStackTrace();
        }
        // return the server's reply/response text
        return serverResponse;
    }
///////////////////////////////////////////

    @Override
    public void onClick(View view) {
        if (view == btnhome) {
            switch (MODE) {
                case "BT":
                    Cursor c = dcmote.rawQuery("SELECT * FROM device", null);
                    if (c.getCount() > 0) {
                        Log.w("DCmote", "Have a  some device  ");
                        if (c.moveToFirst()) {
                            Log.w("DCmote", "Device..............." + c.getString(0));
                        }
                    }
                    Log.w("DCmote", "test----" + c.getCount());
                    Cursor chk = dcmote.rawQuery("SELECT * FROM device WHERE dname='Device'", null);
                    if (chk.moveToFirst()) {
                        Log.w("DCmote", "Device....." + chk.getString(1));
                        Log.w("DCmote", "Device is found but this is null ");
                        Intent i = new Intent(Lunch.this, device.class);
                        showMessage("No Default Device", "Please Choose Device");
                        tts.speak("sir!.. you have no selected device ! , you need to configure!   frist you need to add custom switch.. then you can choose default device  ", TextToSpeech.QUEUE_FLUSH, null);
                    } else {
                        if (btSocket != null) {
                            tts.speak("sir!. please restart this application   ", TextToSpeech.QUEUE_FLUSH, null);
                            //    BluetoothAdapter.getDefaultAdapter().disable();
                        } else {//new ConnectBT().execute();
                            Intent i = new Intent(Lunch.this, home.class);
                            //Change the activity.
                            i.putExtra(EXTRA_ADDRESS, c.getString(0)); //this will be received at ledControl (class) Activity
                            startActivity(i);
                        }
                    }
                    break;
                case "WIFI":
                    Intent i = new Intent(Lunch.this, Wifihome.class);
                    startActivity(i);
                    break;
                case "iot":
                    Intent iot = new Intent(Lunch.this, Iothome.class);
                    startActivity(iot);
                    break;
            }
        }
        if (view == btnsetup) {
            Intent i = new Intent(Lunch.this, setup.class);
            //Change the activity.
            // i.putExtra(EXTRA_ADDRESS, address); //this will be received at ledControl (class) Activity
            startActivity(i);
        }

        if (view.getId() == btnabout.getId()) {
            x = view.getContext();
            switch (MODE) {
                case "BT":
                    Cursor c = dcmote.rawQuery("SELECT * FROM device", null);

                    if (c.getCount() > 0) {
                        Log.w("DCmote", "Have a  some device  ");
                        if (c.moveToFirst()) {
                            Log.w("DCmote", "Device..............." + c.getString(0));
                        }
                    }
                    Log.w("DCmote", "test----" + c.getCount());
                    Cursor chk = dcmote.rawQuery("SELECT * FROM device WHERE dname='Device'", null);
                    if (chk.moveToFirst()) {
                        Log.w("DCmote", "Device....." + chk.getString(1));
                        Log.w("DCmote", "Device is found but this is null ");
                        Intent i = new Intent(Lunch.this, device.class);
                        showMessage("No Default Device", "Please Choose Device");
                        tts.speak("sir!.. you have no selected device ! , you need to configure!   frist you need to add custom switch.. then you can choose default device  ", TextToSpeech.QUEUE_FLUSH, null);
                    } else {
                        if (btSocket != null) {
                            showGoogleInputDialog(c.getString(0));
                        } else {
                            new ConnectBT().execute();
                        }
                        address = c.getString(0);
                        Log.w("to voice class ", "Device....." + c.getString(0));
                    }
                case "WIFI":

                    String x;
                    x = "sss";
                    showGoogleInputDialog(x);


                    break;

            }


        }

    }

    public void showGoogleInputDialog(String bt) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "Your device is not supported!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SPEECH_REQUEST_CODE: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    speech_output.setText(result.get(0));
                    switch (MODE) {
                        case "BT":
                            voicebt(result.get(0));
                            break;
                        case "WIFI":
                            voicewifi(result.get(0));
                            break;
                    }
                }
                break;
            }
        }
    }

    private void voicewifi(String code) {

        Log.w("myApp", code);
////////////////this section for get sotered data
        Cursor mv = dcmote.rawQuery("SELECT * FROM switch WHERE sname ='" + code + "'", null);
        if (mv.moveToFirst()) {
            Log.w("Switch", " " + mv.getString(0));
            Log.w("Cmd", " " + mv.getString(1));
            Log.w("after voice", "MY IP " + IP + PORT);
            PV = mv.getString(1);
            new HttpRequestAsyncTask(x, "on", "192.168.43.242", "80", "pin").execute();

        }


    }

    private void voicebt(String code) {

        Log.w("myApp", code);
////////////////this section for get sotered data
        Cursor mv = dcmote.rawQuery("SELECT * FROM switch WHERE sname ='" + code + "'", null);
        if (mv.moveToFirst()) {
            Log.w("Switch", " " + mv.getString(0));
            Log.w("Cmd", " " + mv.getString(1));
            Log.w("after voice", "Device....." + address);
            //Call the class to connect
            if (btSocket != null) {
                try {
                    btSocket.getOutputStream().write(mv.getString(1).toString().getBytes());
                    Log.w("Cmd", " " + mv.getString(1));
                } catch (IOException e) {
                    msg("Error");
                }
            } else {//new ConnectBT().execute();

            }
        }
    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    public void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    /**
     * An AsyncTask is needed to execute HTTP requests in the background so that they do not
     * block the user interface.
     */
    private class HttpRequestAsyncTask extends AsyncTask<Void, Void, Void> {

        // declare variables needed
        private String requestReply, ipAddress, portNumber;
        private Context context;
        private AlertDialog alertDialog;
        private String parameter;
        private String parameterValue;

        /**
         * Description: The asyncTask class constructor. Assigns the values used in its other methods.
         *
         * @param context        the application context, needed to create the dialog
         * @param parameterValue the pin number to toggle
         * @param ipAddress      the ip address to send the request to
         * @param portNumber     the port number of the ip address
         */
        public HttpRequestAsyncTask(Context context, String parameterValue, String ipAddress, String portNumber, String parameter) {
            this.context = context;

            alertDialog = new AlertDialog.Builder(this.context)
                    .setTitle("HTTP Response From IP Address:")
                    .setCancelable(true)
                    .create();

            this.ipAddress = ipAddress;
            this.parameterValue = parameterValue;
            this.portNumber = portNumber;
            this.parameter = parameter;
        }

        /**
         * Name: doInBackground
         * Description: Sends the request to the ip address
         *
         * @param voids
         * @return
         */
        @Override
        protected Void doInBackground(Void... voids) {
            alertDialog.setMessage("Data sent, waiting for reply from server...");
            if (!alertDialog.isShowing()) {
                alertDialog.show();
            }
            requestReply = sendRequest(parameterValue, ipAddress, portNumber, parameter);
            return null;
        }

        /**
         * Name: onPostExecute
         * Description: This function is executed after the HTTP request returns from the ip address.
         * The function sets the dialog's message with the reply text from the server and display the dialog
         * if it's not displayed already (in case it was closed by accident);
         *
         * @param aVoid void parameter
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            alertDialog.setMessage(requestReply);
            if (!alertDialog.isShowing()) {
                alertDialog.show(); // show dialog
            }
        }

        /**
         * Name: onPreExecute
         * Description: This function is executed before the HTTP request is sent to ip address.
         * The function will set the dialog's message and display the dialog.
         */
        @Override
        protected void onPreExecute() {
            alertDialog.setMessage("Sending data to server, please wait...");
            if (!alertDialog.isShowing()) {
                alertDialog.show();
            }
        }
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(Lunch.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            } catch (IOException e) {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                /// tts.speak("Connection Failed. Is it a SPP Bluetooth? Try again..  Please Make sure Power on device   ", TextToSpeech.QUEUE_FLUSH, null);

                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");


                //   Intent i = new Intent(Lunch.this, device.class);
                // isBtConnected = true;/////////////////////

                //     startActivity(i);


                finish();
            } else {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }

    }


}
