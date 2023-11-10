package es.ua.eps.clientserver

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import es.ua.eps.clientserver.databinding.ActivityHomeBinding
import es.ua.eps.clientserver.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// La clase HomeActivity extiende AppCompatActivity y representa la actividad principal de la aplicación,
// donde los usuarios pueden crear o unirse a salas de chat.
class HomeActivity : AppCompatActivity() {

    //    viewBinding: Representa las vistas enlazadas de la actividad Home.
    //    buttonCreate: Representa el botón para crear una sala de chat.
    //    buttonJoin: Representa el botón para unirse a una sala de chat.
    //    buttonDisconnect: Representa el botón para desconectarse.
    //    askChatRoomsCorroutine: Representa el trabajo en segundo plano para solicitar la lista de salas de chat.
    lateinit var viewBinding: ActivityHomeBinding

    lateinit var buttonCreate: Button
    lateinit var buttonJoin: Button
    lateinit var buttonDisconnet: Button


    var askChatRoomsCorroutine: Job? = null

    // onCreate(savedInstanceState: Bundle?)
    //    Método llamado cuando se crea la actividad.
    //    Infla la interfaz de usuario y asigna vistas y eventos a los botones.
    //    Limpia el mapa de salas de chat (SystemChatRoomList.mutableMap).
    //    Define el comportamiento del botón "Crear", que inicia la actividad CreateChatRoomActivity.
    //    Define el comportamiento del botón "Unirse", que solicita la lista de salas de chat, luego inicia ChatRoomListActivity.
    //    Define el comportamiento del botón "Desconectar", que cierra la comunicación y finaliza la actividad.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityHomeBinding.inflate(layoutInflater)

        setContentView(viewBinding.root)

        buttonCreate = viewBinding.buttonCreate
        buttonJoin = viewBinding.buttonJoin
        buttonDisconnet = viewBinding.buttonDisconnect

        SystemChatRoomList.mutableMap.clear()

        //    Este bloque de código establece un listener para el botón "Crear Sala".
        //    Cuando se hace clic en el botón, se crea un Intent para iniciar la actividad CreateChatRoomActivity.
        //    startActivity(intentCreateChatRoom) inicia la actividad correspondiente.

        buttonCreate.setOnClickListener {
            val intentCreateChatRoom = Intent(this@HomeActivity, CreateChatRoomActivity::class.java)
            startActivity(intentCreateChatRoom)
        }

        //    Este bloque de código establece un listener para el botón "Unirse a Sala".
        //    Cuando se hace clic en el botón, se inicia un trabajo en segundo plano (askChatRoomsCorroutine) para solicitar la lista de salas de chat.
        //    Después de obtener la lista, se establece la vista raíz (SystemClient.setRootView(viewBinding.root))
        //    y se inicia la actividad ChatRoomListActivity.
        buttonJoin.setOnClickListener {
            askChatRoomsCorroutine = lifecycleScope.launch(Dispatchers.IO) {
                SystemClient.askForChatRoomList()
            }

            lifecycleScope.launch(Dispatchers.Main) {
                SystemClient.setRootView(viewBinding.root)
                askChatRoomsCorroutine!!.join()
                withContext(Dispatchers.IO) {
                    while (SystemChatRoomList.mutableMap.count() == 0) {
                    }
                }

                val intentOpenChat =
                    Intent(this@HomeActivity, ChatRoomListActivity::class.java)
                startActivity(intentOpenChat)
            }
        }

        //    Este bloque de código establece un listener para el botón "Desconectar".
        //    Cuando se hace clic en el botón, se lanza un trabajo en segundo plano para cerrar la comunicación (SystemClient.closeComunication())
        //    y finalizar la actividad (finish()).
        buttonDisconnet.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                SystemClient.closeComunication()
                finish()
            }
        }
    }
}