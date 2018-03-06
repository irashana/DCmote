package com.rts.dcmote.dcmote;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class setup extends Activity implements View.OnClickListener {
    ImageView btnpanel, btndevice, btnaboutt, btnwifimode, btniot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup);
        btndevice = (ImageView) findViewById(R.id.btnconect);
        btnpanel = (ImageView) findViewById(R.id.btnsetup);
        btnaboutt = (ImageView) findViewById(R.id.btnaboutt);
        btnwifimode = (ImageView) findViewById(R.id.btnwifimode);
        btniot = (ImageView) findViewById(R.id.btniot);
        btndevice.setOnClickListener(this);
        btnpanel.setOnClickListener(this);
        btnaboutt.setOnClickListener(this);
        btnwifimode.setOnClickListener(this);
        btniot.setOnClickListener(this);


    }


    @Override
    public void onClick(View v) {
        if (v == btnpanel) {
            Intent i = new Intent(setup.this, panel.class);
            //Change the activity.
            // i.putExtra(EXTRA_ADDRESS, address); //this will be received at ledControl (class) Activity
            startActivity(i);
        }
        if (v == btndevice) {
            Intent i = new Intent(setup.this, device.class);
            //Change the activity.
            // i.putExtra(EXTRA_ADDRESS, address); //this will be received at ledControl (class) Activity
            startActivity(i);
        }
        if (v == btnaboutt) {
            Intent i = new Intent(setup.this, About.class);
            //Change the activity.
            // i.putExtra(EXTRA_ADDRESS, address); //this will be received at ledControl (class) Activity
            startActivity(i);
        }

        if (v == btnwifimode) {
            Intent i = new Intent(setup.this, Wifi.class);
            //Change the activity.
            // i.putExtra(EXTRA_ADDRESS, address); //this will be received at ledControl (class) Activity
            startActivity(i);
        }

        if (v == btniot) {
            Intent i = new Intent(setup.this, Iot.class);
            //Change the activity.
            // i.putExtra(EXTRA_ADDRESS, address); //this will be received at ledControl (class) Activity
            startActivity(i);
        }


    }
}
