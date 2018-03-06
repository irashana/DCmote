package com.rts.dcmote.dcmote;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by TurboBoost on 9/2/2016.
 */
public class device extends ActionBarActivity {
    public static String EXTRA_ADDRESS = "device_address";
    //widgets
    Button btnPaired, setbtmode1;
    ListView devicelist;
    SQLiteDatabase dcmote;
    //Bluetooth
    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> pairedDevices;
    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Make an intent to start next activity.
            Intent i = new Intent(device.this, home.class);
            String h;

            //   Cursor c=dcmote.rawQuery("SELECT * FROM device WHERE dname='Device'", null);
            Cursor c = dcmote.rawQuery("SELECT * FROM device ", null);
            if (c.moveToFirst()) {
                dcmote.execSQL("UPDATE device SET dname='" + address + "'");
                showMessage("Success", "Device Modified");
                // Log.w("DCmote", "Device is " + c.getString(0));
                //  h=c.getString(0);
                Log.w("DCmote", "this is   " + c.getString(0));
                SystemClock.sleep(1000); //ms
                i.putExtra(EXTRA_ADDRESS, address); //this will be received at ledControl (class) Activity
                startActivity(i);
                //  finish();

            } else {
                // Cursor cc=dcmote.rawQuery("SELECT * FROM device WHERE dname='"+c.getString(0)+"'", null);
                // if(cc.moveToFirst()) {
                //   Log.w("DCmote", "Device is " + cc.getString(0));
                // }
                ///   showMessage("Error", "Invalid Rollno");
            }


            //Change the activity.

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device);
        dcmote = openOrCreateDatabase("dcm", Context.MODE_PRIVATE, null);
        dcmote.execSQL("CREATE TABLE IF NOT EXISTS switch(sname VARCHAR,cmd VARCHAR,icon VARCHAR );");
        dcmote.execSQL("CREATE TABLE IF NOT EXISTS device(dname VARCHAR,d1 VARCHAR,d2 VARCHAR );");
        dcmote.execSQL("CREATE TABLE IF NOT EXISTS cmode(conmode VARCHAR,mode VARCHAR);");
        dcmote.execSQL("CREATE TABLE IF NOT EXISTS ip(IP VARCHAR,PORT VARCHAR );");
        showMessage("DCmote", "Set Default Device");

        //Calling widgets
        btnPaired = (Button) findViewById(R.id.button);
        setbtmode1 = (Button) findViewById(R.id.setbtmode);

        devicelist = (ListView) findViewById(R.id.listView);

        //if the device has bluetooth
        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        if (myBluetooth == null) {
            //Show a mensag. that the device has no bluetooth adapter
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();

            //finish apk
            finish();
        } else if (!myBluetooth.isEnabled()) {
            //Ask to the user turn the bluetooth on
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon, 1);
        }

        btnPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pairedDevicesList();
            }
        });

        setbtmode1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dcmote.execSQL("UPDATE cmode SET conmode='BT'");
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


            }
        });


    }

    private void pairedDevicesList() {
        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice bt : pairedDevices) {
                list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        devicelist.setAdapter(adapter);
        devicelist.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked

    }

    public void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();


    }


}
