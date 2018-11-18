package moramaz.com.smartstick;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class Principal extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, LocationListener {

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String TAG = Principal.class.getName();
    public static String address = null;
    private final int handlerState = 0;
    private int countThree = 2;
    private String provider_info, dataInPrint, IDSesion = "", TipoSenal = "3", Longitud, Latitud,
            TipoObstaculo, Incidente, IMEI, IDCoach, IDPaciente;
    private double latitude, longitude;
    private boolean isGPSEnabled = false, isNetworkEnabled = false, isGPSTrackingEnabled = false;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private ConnectedThread mConnectedThread;
    private Gson gson;
    private Handler bluetoothIn;
    private LocationManager locationManager;
    private Location location;
    private MediaPlayer mp;
    private StringBuilder recDataString = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        checkGPSPermissions();
        char[] text = {'\0', '\0'};
        Switch sch;
        EditText txt;
        gson = new Gson();
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData(view);
            }
        });
        sch = findViewById(R.id.switch1);
        sch.setText(text, 0, 1);
        txt = findViewById(R.id.editText);
        txt.setText(text, 0, 1);
        txt = findViewById(R.id.editText2);
        txt.setText(text, 0, 1);
        txt = findViewById(R.id.editText3);
        txt.setText(text, 0, 1);
        dataInPrint = "";
        IMEI = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        bluetoothIn = new Handler(){
            public void handleMessage(android.os.Message msg) {
                sendData(msg);
            }
        };
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings)
            return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        BluetoothDevice device;
        try{
            device = btAdapter.getRemoteDevice(address);
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) { Toast.makeText(getBaseContext(),
                    "La creacción del Socket fallo", Toast.LENGTH_LONG).show(); }
        try{ btSocket.connect(); } catch (IOException e) { try{ btSocket.close();
        } catch (IOException e2){} }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        try{ btSocket.close(); } catch (IOException e2) { }
    }

    @Override
    public void onLocationChanged(Location location) { }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderEnabled(String provider) { }

    @Override
    public void onProviderDisabled(String provider) { }

    public void sendData(View view){
        try{
            mConnectedThread.write(String.valueOf(Double.valueOf(((EditText)
                    findViewById(R.id.editText)).getText().toString())));
            mConnectedThread.write(",");
        }catch(Exception e){
            Snackbar.make(view, "La altura debe ser un número.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    public void sendData(Message msg){
        if (msg.what == handlerState) {
            String readMessage = (String) msg.obj;
            recDataString.append(readMessage);
            int endOfLineIndex = recDataString.indexOf("~");
            if (endOfLineIndex > 0) {
                dataInPrint = recDataString.substring(0, endOfLineIndex);
                java.util.List<String> items = java.util.Arrays
                        .asList(dataInPrint.split(","));
                Latitud = String.valueOf(getLatitude());
                Longitud = String.valueOf(getLongitude());
                TipoObstaculo = items.get(0);
                switch(TipoObstaculo){
                    case "aereo":
                        TipoObstaculo = "4";
                        break;
                    case "frontal":
                        TipoObstaculo = "3";
                        break;
                    case "terrestre":
                        TipoObstaculo = "2";
                        break;
                    case "agujero":
                        TipoObstaculo = "1";
                        break;
                    default:
                        TipoObstaculo = "0";
                        break;
                }
                Incidente = items.get(1);
                IDCoach = ((EditText) findViewById(R.id.editText2)).getText().toString();
                IDPaciente = ((EditText) findViewById(R.id.editText3)).getText().toString();
                playSong();
                boolean activo = ((Switch) findViewById(R.id.switch1)).isChecked();
                if (TipoSenal.equals("3") && activo){
                    TipoSenal = "1";
                    countThree = 0;
                }else if(activo)
                    TipoSenal = "2";
                else {
                    TipoSenal = "3";
                    countThree++;
                }
                if(countThree < 2)
                    Request();
                recDataString.delete(0, recDataString.length());
                dataInPrint = "";
            }
        }
    }

    public void playSong(){
        try{ mp.stop(); }catch(Exception e){}
        int resid;
        switch(TipoObstaculo){
            case "4":
                resid = R.raw.song1;
                break;
            case "3":
                resid = R.raw.song2;
                break;
            case "2":
                resid = R.raw.song3;
                break;
            case "1":
                resid = R.raw.song4;
                break;
            default:
                return;
        }
        mp = MediaPlayer.create(this, resid);
        mp.start();
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    public void checkGPSPermissions(){
        ActivityCompat.requestPermissions(Principal.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            msg("Error al solicitar GPS.");
            return;
        }else {
            try {
                locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
                isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                if (isGPSEnabled) {
                    this.isGPSTrackingEnabled = true;
                    Log.d(TAG, "Application use GPS Service");
                    provider_info = LocationManager.GPS_PROVIDER;
                } else if (isNetworkEnabled) {
                    this.isGPSTrackingEnabled = true;
                    Log.d(TAG, "Application use Network State to get GPS coordinates");
                    provider_info = LocationManager.NETWORK_PROVIDER;
                }
                if (!provider_info.isEmpty()) {
                    locationManager.requestLocationUpdates(
                            provider_info,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            this
                    );
                    if (locationManager != null)
                        location = locationManager.getLastKnownLocation(provider_info);
                    try{
                        Longitud = String.valueOf(location.getLongitude());
                        Latitud = String.valueOf(location.getLatitude());
                    }catch(Exception e){
                        Log.e(TAG, "Impossible to connect to Location", e);
                        msg("No se ha podido utilizar el servicio " + provider_info);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Impossible to connect to LocationManager", e);
            }
        }
    }

    private void checkBTState() {
        if(btAdapter==null)
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth",
                    Toast.LENGTH_LONG).show();
        else
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
    }

    public double getLatitude() {
        if (location != null)
            latitude = location.getLatitude();
        return latitude;
    }

    public double getLongitude() {
        if (location != null)
            longitude = location.getLongitude();
        return longitude;
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run() {
            byte[] buffer = new byte[256];
            int bytes;
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) { break; }
            }
        }
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();
            try { mmOutStream.write(msgBuffer); } catch (IOException e) {
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    public void Request(){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://35.155.178.16/PACYE2/REST/registrarrecorrido";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(!response.contains("\"Codigo\":1")){
                            msg("error");
                        }else{
                            response.replace("\"", "'");
                            Recorrido r = new Recorrido();
                            r = gson.fromJson(response, Recorrido.class);
                            IDSesion = r.getIDSesion();
                            TipoObstaculo = r.getTipoObstaculo();
                            playSong();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        msg("Error de comunicacion");
                    }
                }) {
                    @Override
                    public String getBodyContentType() {
                        return "application/json; charset=utf-8";
                    }

                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        String     requestBody = "{\n" +
                                "     \"IDSesion\":\"" + IDSesion.trim() + "\",\n" +
                                "     \"TipoSeñal\":" + TipoSenal.trim() + ",\n" +
                                "     \"Longitud\": \"" + Longitud.trim() + "\",\n" +
                                "     \"Latitud\": \"" + Latitud.trim() + "\",\n" +
                                "     \"TipoObstaculo\":" + TipoObstaculo.trim() + ",\n" +
                                "     \"Incidente\":" + Incidente.trim() + ",\n" +
                                "     \"Emergencia\": 0,\n" +
                                "     \"IMEI\": \"" + IMEI.trim() + "\",\n" +
                                "     \"IDCoach\": \"" + IDCoach.trim() + "\",\n" +
                                "     \"IDPaciente\": \"" + IDPaciente.trim() + "\"\n" +
                                "}";
                        try { return requestBody == null ? null : requestBody
                                .getBytes("utf-8");
                        } catch (UnsupportedEncodingException uee) {
                            VolleyLog.wtf("Unsupported Encoding while trying to get the"
                                    + " bytes of %s using %s", requestBody, "utf-8");
                            return null;
                        }
                    }

                    @Override
                    protected Response<String> parseNetworkResponse(NetworkResponse response) {
                        String responseString = "";
                        if (response != null)
                            responseString = new String(response.data);
                        return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                    }
                };
        queue.add(stringRequest);
    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }
}