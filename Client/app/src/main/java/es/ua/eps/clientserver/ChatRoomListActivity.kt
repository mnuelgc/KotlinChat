package es.ua.eps.clientserver

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import es.ua.eps.clientserver.databinding.ActivityChatRoomListBinding
import es.ua.eps.clientserver.databinding.ActivityHomeBinding
import es.ua.eps.clientserver.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// Aquí se define la clase ChatRoomListActivity que extiende AppCompatActivity.
// Se declaran variables, incluyendo viewBinding, buttonGoBack,
// joinChatCorroutine y list para representar los elementos de la interfaz de usuario.

class ChatRoomListActivity : AppCompatActivity() {

    lateinit var viewBinding: ActivityChatRoomListBinding
    lateinit var buttonGoBack : Button


    var joinChatCorroutine : Job? = null

    lateinit var list: ListView

    // En el método onCreate, se infla el layout usando ActivityChatRoomListBinding y se establece el contenido de la vista con setContentView.
    // Además, se inicializan las variables list y buttonGoBack.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityChatRoomListBinding.inflate(layoutInflater)

        setContentView(viewBinding.root)

        list = viewBinding.list

        buttonGoBack = viewBinding.buttonBack


    // Aquí se prepara una lista de nombres de salas de chat (rooms) a partir de los datos en SystemChatRoomList.mutableMap.
    // Luego, se crea un adaptador (adapter) utilizando la clase RoomsAdapter y se establece en el ListView (list).

        val rooms = mutableListOf<String>()
        for (i in 0 ..<SystemChatRoomList.mutableMap.size) { // sumar 1
            rooms.add(SystemChatRoomList.mutableMap[i]!!)
        }

        val adapter = RoomsAdapter(
            this,
            R.layout.item_room, rooms
        )

        list.adapter = adapter


        // Aquí se configura un listener para el clic en elementos de la lista.
        // Cuando se hace clic en un elemento, se lanza una corrutina (joinChatCorroutine) en el hilo de entrada-salida (Dispatchers.IO).
        // La corrutina intenta unirse a la sala de chat utilizando SystemClient.joinChatRoom(position + 1) siendo position +1 el id de la sala en el servidor.

        list.setOnItemClickListener { parent: AdapterView<*>, view: View,
                                      position: Int, id: Long ->

            var joined: Boolean
            joined = false
            joinChatCorroutine = lifecycleScope.launch(Dispatchers.IO) {
                joined = SystemClient.joinChatRoom(position + 1)
            }

        // Aquí, en el hilo principal (Dispatchers.Main), se espera a que la corrutina termine (joinChatCorroutine!!.join()).
        // Luego, si el usuario se ha unido correctamente a la sala, se crea un intent para abrir la ConversationActivity y se inicia esa actividad.
        // Además, se limpia el SystemChatRoomList.mutableMap.

            lifecycleScope.launch(Dispatchers.Main) {
                joinChatCorroutine!!.join()
                if (joined) {
                    val intentOpenChat =
                        Intent(this@ChatRoomListActivity, ConversationActivity::class.java)

                    SystemChatRoomList.mutableMap.clear()
                    startActivity(intentOpenChat)
                }
            }
        }

        // Finalmente, se configura un listener para el clic en el botón de retroceso (buttonGoBack).
        // Cuando se hace clic, se limpia el SystemChatRoomList.mutableMap y se finaliza la actividad actual.

        buttonGoBack.setOnClickListener{
            SystemChatRoomList.mutableMap.clear()
            finish()
        }
    }
}