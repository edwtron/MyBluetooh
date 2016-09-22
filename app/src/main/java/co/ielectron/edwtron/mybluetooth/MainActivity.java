package co.ielectron.edwtron.mybluetooth;

/*Uso del Bluetooth
*
* 1 Activamos los permisos en el manifest
*       <uses-permission android:name="android.permission.BLUETOOTH" />
*       <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
*
*
*
* */

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity{

    private TextView lblConsola;
    private ListView lvDispositivos;
    private Button btnBluetooth;
    private Button btnBuscarDispositivo;
    private BluetoothAdapter bAdapter;
    private ArrayList<BluetoothDevice> arrayDevices;
    private BroadcastReceiver bReceiver;
    private static final int 	REQUEST_ENABLE_BT 	= 1;
    private static final String TAG = "depuracion";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lblConsola = (TextView)findViewById(R.id.lblConsola);
        lvDispositivos = (ListView)findViewById(R.id.lvDispositivos);
        btnBluetooth = (Button)findViewById(R.id.btnBluetooth);
        btnBuscarDispositivo = (Button)findViewById(R.id.btnBuscarDispositivo);


        disponibilidadBT();
        verificarEstadoBT();
        detectarCambiosEstadoBT();
        registrarEventosBluetooth();


        btnBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activarDesactivarBT();

            }
        });
        btnBuscarDispositivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buscarBT();
            }
        });


    }

    private void disponibilidadBT(){

        //Obtenemos el adaptador del BT, si es null el dispositivo no tiene BT
        bAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bAdapter == null)
        {
            btnBluetooth.setEnabled(false);
            lblConsola.setText("No tenemos BT");
        }else
        {
            lblConsola.setText("Si tenemos BT");
        }
    }

    private void verificarEstadoBT(){
        // Comprobamos si el Bluetooth esta activo y cambiamos el texto del
        // boton dependiendo del estado.
        if(bAdapter.isEnabled())
            btnBluetooth.setText(R.string.DesactivarBluetooth);
        else
            btnBluetooth.setText(R.string.ActivarBluetooth);
    }

    private void detectarCambiosEstadoBT(){
        // Instanciamos un BroadcastReceiver que se encargara de detectar si el estado
        // del Bluetooth del dispositivo ha cambiado mediante su handler onReceive
        bReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                final String action = intent.getAction();

                // Filtramos por la accion. Nos interesa detectar BluetoothAdapter.ACTION_STATE_CHANGED
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
                {
                    // Solicitamos la informacion extra del intent etiquetada como BluetoothAdapter.EXTRA_STATE
                    // El segundo parametro indicara el valor por defecto que se obtendra si el dato extra no existe
                    final int estado = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                            BluetoothAdapter.ERROR);
                    switch (estado)
                    {
                        // Apagado
                        case BluetoothAdapter.STATE_OFF:
                        {
                            ((Button)findViewById(R.id.btnBluetooth)).setText(R.string.ActivarBluetooth);
                            break;
                        }

                        // Encendido
                        case BluetoothAdapter.STATE_ON:
                        {
                            ((Button)findViewById(R.id.btnBluetooth)).setText(R.string.DesactivarBluetooth);


                            // Lanzamos un Intent de solicitud de visibilidad Bluetooth, al que añadimos un par
                            // clave-valor que indicara la duracion de este estado, en este caso 120 segundos
                            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
                            startActivity(discoverableIntent);
                            break;
                        }
                        default:
                            break;
                    }
                }

/*               BluetoothDevice.ACTION_FOUND: cada vez que se descubra un nuevo dispositivo. Nos servirá para añadirlo al array.
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED: lanzado cuando finalice el proceso de descubrimiento.*/

                if (BluetoothDevice.ACTION_FOUND.equals(action))
                {
                    Log.d(TAG, "Dispositivo encontrado");
                    // Acciones a realizar al descubrir un nuevo dispositivo
                    // Si el array no ha sido aun inicializado, lo instanciamos
                    if(arrayDevices == null)
                        arrayDevices = new ArrayList<BluetoothDevice>();

                    // Extraemos el dispositivo del intent mediante la clave BluetoothDevice.EXTRA_DEVICE
                    BluetoothDevice dispositivo = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    // Añadimos el dispositivo al array
                    arrayDevices.add(dispositivo);

                    // Le asignamos un nombre del estilo NombreDispositivo [00:11:22:33:44]
                    String descripcionDispositivo = dispositivo.getName() + " [" + dispositivo.getAddress() + "]";

                    // Mostramos que hemos encontrado el dispositivo por el Toast
                    Toast.makeText(getBaseContext(), getString(R.string.DetectadoDispositivo)
                            + ": " + descripcionDispositivo, Toast.LENGTH_SHORT).show();

                }

                // Codigo que se ejecutara cuando el Bluetooth finalice la busqueda de dispositivos.
                else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
                {
                    Log.d(TAG, "Termino de buscar BT");
                    // Acciones a realizar al finalizar el proceso de descubrimiento
                    // Instanciamos un nuevo adapter para el ListView mediante la clase que acabamos de crear
                    ArrayAdapter arrayAdapter = new BluetoothDeviceArrayAdapter(getBaseContext(), android.R.layout.simple_list_item_2, arrayDevices);

                    lvDispositivos.setAdapter(arrayAdapter);
                    Toast.makeText(getBaseContext(), "Fin de la búsqueda", Toast.LENGTH_SHORT).show();
                }
            }


        };

    }

    private void registrarEventosBluetooth() {
        // Registramos el BroadcastReceiver que instanciamos previamente para
        // detectar los distintos eventos que queremos recibir
        Log.d(TAG, "Registrando");
        IntentFilter filtro = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        //asi registro varios eventos para detectar
        filtro.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filtro.addAction(BluetoothDevice.ACTION_FOUND);

        this.registerReceiver(bReceiver, filtro);
    }

    private  void activarDesactivarBT(){
        if(bAdapter.isEnabled())
            bAdapter.disable();
        else
        {
            // Lanzamos el Intent que mostrara la interfaz de activacion del
            // Bluetooth. La respuesta de este Intent se manejara en el metodo
            // onActivityResult
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

    }

    private void buscarBT(){
        Log.d(TAG, "Boton Buscar BT");
        if(arrayDevices != null)
            arrayDevices.clear();

        // Comprobamos si existe un descubrimiento en curso. En caso afirmativo, se cancela.
        if(bAdapter.isDiscovering())
            bAdapter.cancelDiscovery();

        // Iniciamos la busqueda de dispositivos y mostramos el mensaje de que el proceso ha comenzado
        if(bAdapter.startDiscovery())
            Toast.makeText(this, "Iniciando búsqueda de dispositivos bluetooth", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "Error al iniciar búsqueda de dispositivos bluetooth", Toast.LENGTH_SHORT).show();

    }

    // Ademas de realizar la destruccion de la actividad, eliminamos el registro del
    // BroadcastReceiver.
    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(bReceiver);
    }
}
