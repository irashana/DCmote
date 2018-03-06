package com.rts.dcmote.dcmote;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

public class home extends Activity {
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    ListView list;
    Button btnOn, btnOff, btnDis;
    SeekBar brightness;
    TextView lumn;
    String address = null;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    SQLiteDatabase dcmote;
    String[] u;
    String[] web, com;
    Integer[] imageId;
    TextToSpeech tts;
    private ProgressDialog progress;
    private boolean isBtConnected = false;

    // String icon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Intent newint = getIntent();
        address = newint.getStringExtra(device.EXTRA_ADDRESS); //receive the address of the bluetooth device
        dcmote = openOrCreateDatabase("dcm", Context.MODE_PRIVATE, null);
        dcmote.execSQL("CREATE TABLE IF NOT EXISTS switch(sname VARCHAR,cmd VARCHAR,icon VARCHAR );");
        dcmote.execSQL("CREATE TABLE IF NOT EXISTS device(dname VARCHAR,d1 VARCHAR,d2 VARCHAR );");
        dcmote.execSQL("CREATE TABLE IF NOT EXISTS cmode(conmode VARCHAR,mode VARCHAR);");
        dcmote.execSQL("CREATE TABLE IF NOT EXISTS hostpot(hssid VARCHAR , hpw VARCHAR); ");
        dcmote.execSQL("CREATE TABLE IF NOT EXISTS ip(IP VARCHAR,PORT VARCHAR );");
        setContentView(R.layout.home);
        tts = new TextToSpeech(home.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.US);
                }
            }
        });

        new ConnectBT().execute(); //Call the class to connect


        Cursor cc = dcmote.rawQuery("SELECT * FROM switch", null);
        if (cc.getCount() == 0) {
            showMessage("Error", "Switch Not Found Please Create Switch");
            SystemClock.sleep(1000); //ms
            Intent i = new Intent(home.this, panel.class);
            startActivity(i);


            return;
        }
        StringBuffer bbuffer = new StringBuffer();
        while (cc.moveToNext()) {
            bbuffer.append("sname: " + cc.getString(0) + "\n");
            bbuffer.append("cmd: " + cc.getString(1) + "\n");
            bbuffer.append("icon: " + cc.getString(2) + "\n\n");
        }


        Cursor c = dcmote.rawQuery("SELECT * FROM switch", null);
        StringBuffer buffer = new StringBuffer();

        int val = c.getCount();
        Integer[] putimg = new Integer[val];
        String[] put = new String[val];
        String[] cmd = new String[val];


        for (int l = 0; l < val; l++) {


            String[] array = new String[val];
            String[] comcmd = new String[val];

            int i = 0;
            while (c.moveToNext()) {
                String uname = c.getString(c.getColumnIndex("sname"));/// change as cname
                String icon = c.getString(c.getColumnIndex("icon"));
                String cm = c.getString(c.getColumnIndex("cmd"));

                array[i] = uname;
                comcmd[i] = cm;

                put = array;
                cmd = comcmd;

                switch (icon) {
                    case "on":
                        putimg[i] = R.drawable.btnon;
                        break;
                    case "off":
                        putimg[i] = R.drawable.btnoff;
                        break;
                    case "home":
                        putimg[i] = R.drawable.btnhome;
                        break;
                    case "fan":
                        putimg[i] = R.drawable.btnfan;
                        break;
                    case "room":
                        putimg[i] = R.drawable.btnroom;
                        break;
                    case "light":
                        putimg[i] = R.drawable.btnlight;
                        break;
                    case "outdoor":
                        putimg[i] = R.drawable.btnoutdoor;
                        break;
                    case "control":
                        putimg[i] = R.drawable.btncontrol;
                        break;

                    default:
                        putimg[i] = R.mipmap.ic_launcher;
                }


                i++;


            }

        }


        com = cmd;
        imageId = putimg;

        web = put;


        CustomList adapter = new
                CustomList(home.this, web, imageId);
        list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //   Toast.makeText(home.this, "You Clicked at " + web[+position], Toast.LENGTH_SHORT).show();
                //  Toast.makeText(home.this, "You Clicked at " + com[+position], Toast.).show();

                if (btSocket != null) {
                    try {
                        String tx = com[position];

                        Log.w("myApp", tx + "--------" + position + "------" + tx.toString().getBytes());

                        btSocket.getOutputStream().write(tx.toString().getBytes());


                    } catch (IOException e) {
                        msg("Error");
                    }
                }

            }
        });

    }

    public void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }


    // fast way to call Toast
    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }


    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(home.this, "Connecting...", "Please wait!!!");  //show a progress dialog
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


                Intent i = new Intent(home.this, device.class);
                // isBtConnected = true;/////////////////////

                startActivity(i);


                finish();
            } else {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }

}