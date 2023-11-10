package es.ua.eps.clientserver

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView


// RoomsAdapter es responsable de tomar la lista de nombres de salas y adaptarla
// Para que pueda ser mostrada en la vista de la lista en ChatRoomListActivity.
// Este constructor inicializa la clase extendida (ArrayAdapter) con el contexto, el diseño del elemento (resource),
// y la lista de objetos (objects), que en este caso es la lista de nombres de salas.
class RoomsAdapter(context: Context?, resource: Int,
                   objects: List<String>?
) : ArrayAdapter<String>(context!!, resource, objects!!){

    //    El método getView se llama cuando se va a mostrar un elemento en la lista.
    //    position: la posición del elemento en la lista.
    //    convertView: la vista reciclada para reutilizar si está presente.
    //    parent: el ViewGroup al que pertenece la vista.
    //    view: se inicializa con la vista reciclada o inflada si es nula.
    //    tvRoomName: referencia al TextView en el diseño del elemento (item_room) que mostrará el nombre de la sala.
    //    getItem(position)?.let { tvRoomName.text = it }: establece el texto del TextView con el nombre de la sala en la posición actual de la lista.
    override fun getView(position: Int, convertView: View?,
                         parent: ViewGroup
    ) : View {
        var view : View = convertView?: LayoutInflater.from(this.context)
            .inflate(R.layout.item_room, parent, false)
        val tvRoomName = view.findViewById<TextView>(R.id.roomsName) as TextView

        getItem(position)?.let{
            tvRoomName.text = it
        }
        return view
    }
}