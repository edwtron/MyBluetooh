package co.ielectron.edwtron.mybluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by edwtron on 20/09/16.
 */
public class BluetoothDeviceArrayAdapter extends ArrayAdapter {
    private List<BluetoothDevice> deviceList;	// Contendra el listado de dispositivos
    private Context context;					// Contexto activo

    //constructor
    public BluetoothDeviceArrayAdapter(Context context, int textViewResourceId,
                                       List<BluetoothDevice> objects) {
        // Invocamos el constructor base
        super(context, textViewResourceId, objects);

        // Asignamos los parametros a los atributos
        this.deviceList = objects;
        this.context = context;
    }


    //getCount(): modificamos su comportamiento para que devuelva el número de dispositivos contenidos en la lista.
    @Override
    public int getCount()
    {
        if(deviceList != null)
            return deviceList.size();
        else
            return 0;
    }

    //getItem(): modificamos su comportamiento para evitar el riesgo de excepciones en caso de que la lista sea null.
    @Override
    public Object getItem(int position)
    {
        return (deviceList == null ? null : deviceList.get(position));
    }


    //getView(): Este método es el núcleo del adaptador. Se encarga, literalmente, de generar dinámicamente un objeto
    // de interfaz (View) personalizado a partir de cualquier otro tipo de dato para poder insertarlo en el ListView.
    // En el caso que nos ocupa, crearemos una vista con dos TextView superpuestos. El superior, de mayor tamaño,
    // mostrará el nombre del dispositivo. El inferior, de menor tamaño, mostrará la dirección del dispositivo
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {

        //comprobamos que la lista de dispositivos y el contexto están correctamente inicializados.
        if((deviceList == null) || (context == null))
            return null;
        // Usamos un LayoutInflater para crear las vistas
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // Creamos una vista a partir de simple_list_item_2, que contiene dos TextView.
        // El primero (text1) lo usaremos para el nombre, mientras que el segundo (text2)
        // lo utilizaremos para la direccion del dispositivo.
        View elemento = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);

        // Referenciamos los TextView
        TextView tvNombre = (TextView)elemento.findViewById(android.R.id.text1);
        TextView tvDireccion = (TextView)elemento.findViewById(android.R.id.text2);


        // Obtenemos el dispositivo del array y obtenemos su nombre y direccion, asociandosela
        // a los dos TextView del elemento
        BluetoothDevice dispositivo = (BluetoothDevice)getItem(position);
        if(dispositivo != null)
        {
            tvNombre.setText(dispositivo.getName());
            tvDireccion.setText(dispositivo.getAddress());
        }
        else
        {
            tvNombre.setText("ERROR");
        }

        // Devolvemos el elemento con los dos TextView cumplimentados
        return elemento;


    }

}
