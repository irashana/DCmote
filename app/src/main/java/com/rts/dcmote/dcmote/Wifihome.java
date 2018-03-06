package com.rts.dcmote.dcmote;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
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
import java.util.UUID;

public class Wifihome extends Activity {
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    ListView list;
    String IP = null, PORT = null, PV = null;
    Context x = null;
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
        dcmote.execSQL("CREATE TABLE IF NOT EXISTS ip(IP VARCHAR,PORT VARCHAR );");
        dcmote.execSQL("CREATE TABLE IF NOT EXISTS hostpot(hssid VARCHAR , hpw VARCHAR); ");
        setContentView(R.layout.home);


        Cursor cc = dcmote.rawQuery("SELECT * FROM switch", null);
        if (cc.getCount() == 0) {
            showMessage("Error", "Switch Not Found Please Create Switch");
            SystemClock.sleep(1000); //ms
            Intent i = new Intent(Wifihome.this, panel.class);
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
                CustomList(Wifihome.this, web, imageId);
        list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //   Toast.makeText(home.this, "You Clicked at " + web[+position], Toast.LENGTH_SHORT).show();
                //  Toast.makeText(home.this, "You Clicked at " + com[+position], Toast.).show();
                Cursor ipport = dcmote.rawQuery("SELECT * FROM ip ", null);
                if (ipport.moveToFirst()) {
                    IP = ipport.getString(0);
                    PORT = ipport.getString(1);
                    Log.w("IP Mode", "Your IP---" + IP + "-- Your Port--" + PORT);
                    x = view.getContext();
                    String tx = com[position];
                    Log.w("myApp", "vggfdgf" + "--------" + position + "------" + tx);
                    PV = tx;
                    new HttpRequestAsyncTask(x, "on", "192.168.43.242", "80", "pin").execute();
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
            //  URI website = new URI("http://"+ipAddress+":"+portNumber+"/?"+parameterName+"="+parameterValue);
            //   URI website = new URI("http://"+ipAddress+":"+portNumber+"/on");
            //   URI website = new URI("http://"+ipAddress+":"+portNumber+parameterValue);
            //    URI website = new URI("http://"+IP+":"+portNumber+"/"+PV+"");
            URI website = new URI("http://" + IP + ":/" + PV + "");

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


}