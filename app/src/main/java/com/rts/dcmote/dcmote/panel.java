package com.rts.dcmote.dcmote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by TurboBoost on 9/2/2016.
 */
public class panel extends Activity implements View.OnClickListener {

    ImageView btnhome1, btnon, btnoff, btnlight, btncontrol, btnoutdoor, btnroom, btnfan, btnadd, btnedit, btndelete, btnview;
    TextView btn;
    EditText txtname, txtcmd;
    SQLiteDatabase dcmote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.panel);
        btnhome1 = (ImageView) findViewById(R.id.btnhome1);
        btnon = (ImageView) findViewById(R.id.btnon);
        btnlight = (ImageView) findViewById(R.id.btnlight);
        btnoff = (ImageView) findViewById(R.id.btnoff);
        btncontrol = (ImageView) findViewById(R.id.btncontrol);
        btnoutdoor = (ImageView) findViewById(R.id.btnoutdoor);
        btnroom = (ImageView) findViewById(R.id.btnroom);
        btnfan = (ImageView) findViewById(R.id.btnfan);
        btnadd = (ImageView) findViewById(R.id.btnadd);
        btnedit = (ImageView) findViewById(R.id.btnedit);
        btndelete = (ImageView) findViewById(R.id.btndelete);
        btndelete = (ImageView) findViewById(R.id.btndelete);
        btnview = (ImageView) findViewById(R.id.btnview);

        btnview.setOnClickListener(this);

        btnhome1.setOnClickListener(this);
        btnon.setOnClickListener(this);
        btnlight.setOnClickListener(this);
        btnoff.setOnClickListener(this);
        btnroom.setOnClickListener(this);
        btncontrol.setOnClickListener(this);
        btnoutdoor.setOnClickListener(this);
        btncontrol.setOnClickListener(this);
        btnadd.setOnClickListener(this);
        btnedit.setOnClickListener(this);
        btndelete.setOnClickListener(this);
        btnfan.setOnClickListener(this);
        btn = (TextView) findViewById(R.id.btn);
        txtcmd = (EditText) findViewById(R.id.txtcmd);
        txtname = (EditText) findViewById(R.id.txtsname);

        dcmote = openOrCreateDatabase("dcm", Context.MODE_PRIVATE, null);
        dcmote.execSQL("CREATE TABLE IF NOT EXISTS switch(sname VARCHAR,cmd VARCHAR,icon VARCHAR );");
        //   dcmote.execSQL("CREATE TABLE IF NOT EXISTS device(dname VARCHAR,d1 VARCHAR,d2 VARCHAR );");
        dcmote.execSQL("CREATE TABLE IF NOT EXISTS cmode(conmode VARCHAR,mode VARCHAR);");
        dcmote.execSQL("CREATE TABLE IF NOT EXISTS ip(IP VARCHAR,PORT VARCHAR );");


    }


    @Override
    public void onClick(View vv) {
        if (vv == btnon) {
            btn.setText("on");
        }
        if (vv == btnoff) {
            btn.setText("off");
        }
        if (vv == btnhome1) {
            btn.setText("home");
        }
        if (vv == btnroom) {
            btn.setText("room");
        }
        if (vv == btnlight) {
            btn.setText("light");
        }
        if (vv == btnfan) {
            btn.setText("fan");
        }
        if (vv == btnoutdoor) {
            btn.setText("outdoor");
        }
        if (vv == btncontrol) {
            btn.setText("control");
        }

        if (vv == btnview) {

            Cursor cc = dcmote.rawQuery("SELECT * FROM switch", null);
            if (cc.getCount() == 0) {
                showMessage("Error", "No records found");
                return;

            }
            StringBuffer bbuffer = new StringBuffer();
            while (cc.moveToNext()) {
                bbuffer.append("sname: " + cc.getString(0) + "\n");
                bbuffer.append("cmd: " + cc.getString(1) + "\n");
                bbuffer.append("icon: " + cc.getString(2) + "\n\n");
            }
            showMessage("Switch", bbuffer.toString());


        }


        if (vv == btnadd) {
            if (txtname.getText().toString().trim().length() == 0 ||
                    txtcmd.getText().toString().trim().length() == 0 ||
                    btn.getText().toString().trim().length() == 0) {
                showMessage("Error", "Please enter all values");
                return;


            }
            dcmote.execSQL("INSERT INTO switch VALUES('" + txtname.getText() + "','" + txtcmd.getText() +
                    "','" + btn.getText() + "');");//////////////////////////////////////////////////////////////////
            showMessage("Success", "Switch Created ");
            clearText();
        }
        if (vv == btnedit) {

            if (txtname.getText().toString().trim().length() == 0) {
                showMessage("Error", "Please enter Switch Name ");
                return;
            }
            Cursor c = dcmote.rawQuery("SELECT * FROM switch WHERE sname='" + txtname.getText() + "'", null);
            if (c.moveToFirst()) {
                dcmote.execSQL("UPDATE switch SET icon='" + btn.getText() + "',cmd='" + txtcmd.getText() +
                        "' WHERE sname='" + txtname.getText() + "'");
                showMessage("Success", "Switch Modified");
            } else {
                showMessage("Error", "Invalid Switch Name");
            }
            clearText();

        }

        if (vv == btndelete) {

            if (txtname.getText().toString().trim().length() == 0) {
                showMessage("Error", "Please enter Switch Name");
                return;
            }
            Cursor c = dcmote.rawQuery("SELECT * FROM switch WHERE sname='" + txtname.getText() + "'", null);
            if (c.moveToFirst()) {
                dcmote.execSQL("DELETE FROM switch WHERE sname='" + txtname.getText() + "'");
                showMessage("Success", "Switch Removed");
            } else {
                showMessage("Error", "Invalid Switch Name");
            }
            clearText();


        }


    }

    public void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    public void clearText() {
        txtcmd.setText("");
        txtname.setText("");
        btn.setText("");
        txtname.requestFocus();
    }


}
