package es.ua.eps.clientserver

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import es.ua.eps.clientserver.databinding.ActivityChatRoomListBinding
import es.ua.eps.clientserver.databinding.ActivityHomeBinding
import es.ua.eps.clientserver.databinding.ActivityMainBinding

class ChatRoomListActivity : AppCompatActivity() {

    lateinit var viewBinding: ActivityChatRoomListBinding

    lateinit var list: ListView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityChatRoomListBinding.inflate(layoutInflater)

        setContentView(viewBinding.root)

        list = viewBinding.list

        val rooms = mutableListOf<String>()
        for (i in 0 ..SystemChatRoomList.mutableMap.size) { // sumar 1
            rooms.add(SystemChatRoomList.mutableMap[i]!!)
        }

        val adapter = RoomsAdapter(
            this,
            R.layout.item_room, rooms
        )

/*        list.adapter = adapter

        val intentFilm = Intent(this@FilmListActivity, FilmDataActivity::class.java)

        list.setOnItemClickListener { parent: AdapterView<*>, view: View,
                                      position: Int, id: Long ->

            intentFilm.putExtra(FilmDataActivity.EXTRA_FILM_ID, position)
            startActivity(intentFilm)
        }
        */

    }
}