package es.ua.eps.clientserver

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class RoomsAdapter(context: Context?, resource: Int,
                   objects: List<String>?
) : ArrayAdapter<String>(context!!, resource, objects!!){
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