package moramaz.com.smartstick;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter mBtAdapter;
    private static final String TAG = "MainActivity";
    private ArrayAdapter<String> dispositivoAdaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onResume(){
        super.onResume();
        estadoBlue();
        TextView textView1 = findViewById(R.id.conectando);
        textView1.setTextSize(40);
        textView1.setText(" ");
        dispositivoAdaptador = new ArrayAdapter<>(this,R.layout.dispositivos);
        ListView listaDispo = findViewById(R.id.listView);
        listaDispo.setAdapter(dispositivoAdaptador);
        listaDispo.setOnItemClickListener(clickDispositivo);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> dispoEmpa = mBtAdapter.getBondedDevices();
        if(dispoEmpa.size()>0){
            findViewById(R.id.listView).setVisibility(View.VISIBLE);
            for(BluetoothDevice dis: dispoEmpa){
                dispositivoAdaptador.add(dis.getName()+"\n"+dis.getAddress());
            }
        }else
            dispositivoAdaptador.add("No hay dispositivos conectados.");
    }


    private AdapterView.OnItemClickListener clickDispositivo = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            TextView textView1 = findViewById(R.id.conectando);
            textView1.setText("Conectando......");
            String info = ((TextView)v).getText().toString();
            Principal.address = info.substring(info.length()-17);
            Intent i = new Intent(getApplicationContext(), Principal.class);
            startActivity(i);
        }
    };

    private void estadoBlue(){
        mBtAdapter=BluetoothAdapter.getDefaultAdapter();
        if(mBtAdapter==null){
            Toast.makeText(getBaseContext(),"El dispositivo no soporta Bluetooth",
                    Toast.LENGTH_SHORT).show();
        }else{
            if(mBtAdapter.isEnabled()){
                Log.d(TAG,"...Bluetooth Activado...");
            }else{
                Intent activar = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(activar,1);
            }
        }
    }


}