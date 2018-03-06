package com.rts.dcmote.dcmote;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by RTS on 3/27/2017.
 */


public class Iot extends Activity {
    private static final String TAG_RESULTS = "result";
    private static final String TAG_ID = "id";
    private static final String TAG_EMAIL = "email";
    String myJSON;
    SQLiteDatabase dcmote;
    String MODE = null;
    private EditText txtemail;  // to add data
    private EditText txtserver;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.iot);
        JSONArray account = null;


        txtemail = (EditText) findViewById(R.id.email);
        txtserver = (EditText) findViewById(R.id.txtserver);

        dcmote = openOrCreateDatabase("dcm", Context.MODE_PRIVATE, null);
        dcmote.execSQL("CREATE TABLE IF NOT EXISTS cmode(conmode VARCHAR,mode VARCHAR);");
        dcmote.execSQL("CREATE TABLE IF NOT EXISTS iot(email VARCHAR,server VARCHAR);");


    }

    public void reg(View view) {
        String email = txtemail.getText().toString();
        String server = txtserver.getText().toString();


        Cursor hp = dcmote.rawQuery("SELECT * FROM iot ", null);
        if (hp.moveToFirst()) {
            dcmote.execSQL("UPDATE iot SET email='" + txtemail.getText() + "',server='" + txtserver.getText() +
                    "' WHERE email='no'");

            Log.w("iot", "iot..............." + hp.getString(0));
            insertToDatabase(email, server);


            ////// hostpoton();


        }


        //   showMessage("Success", "Switch Created ");


    }

    public void saveiot(View view) {


        dcmote.execSQL("UPDATE cmode SET conmode='iot'");
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

    private void insertToDatabase(String name, String add) {
        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                String paramUsername = params[0];
                String paramAddress = params[1];


                String email = txtemail.getText().toString();
                //    String server = txtserver.getText().toString();

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("email", email));
                //    nameValuePairs.add(new BasicNameValuePair("f1", server));

                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(
                            "http://dcmote.xyz/reg.php");
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse response = httpClient.execute(httpPost);

                    HttpEntity entity = response.getEntity();


                } catch (ClientProtocolException e) {

                } catch (IOException e) {

                }
                return "success";
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);

                //  Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
                // TextView textViewResult = (TextView) findViewById(R.id.email);
                //   textViewResult.setText("Inserted");
            }
        }
        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
        sendPostReqAsyncTask.execute(name, add);
    }


}
