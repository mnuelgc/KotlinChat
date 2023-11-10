package es.ua.eps.clientserver

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import es.ua.eps.clientserver.databinding.ActivityConversationBinding
import es.ua.eps.clientserver.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ConversationActivity se encarga de gestionar la conversación en una sala de chat.
// Permite al usuario desconectarse de la sala actual,
// realiza solicitudes al servidor para obtener información y gestiona la interfaz de usuario asociada a la conversación.

// La clase ConversationActivity hereda de AppCompatActivity
// Representa la actividad donde se lleva a cabo la conversación en una sala de chat.
class ConversationActivity : AppCompatActivity() {

    //    viewBinding: Instancia de la clase ActivityConversationBinding utilizada para acceder a las vistas de la actividad.
    //    buttonDisconnect: Botón que permite al usuario desconectarse de la sala de chat.
    //    chatSpaceView: Instancia de la clase ChatSpaceView que muestra los mensajes de la sala de chat.
    //    askChatRoomsCorroutine: Corrutina que se utiliza para realizar la solicitud de la lista de salas de chat.

    lateinit var viewBinding: ActivityConversationBinding

    lateinit var buttonDisconnet: Button
    lateinit var chatSpaceView: ChatSpaceView

    var askChatRoomsCorroutine: Job? = null


    // onCreate(savedInstanceState: Bundle?)
    //      Se realiza la inicialización de las vistas y variables en el método onCreate.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityConversationBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        chatSpaceView = viewBinding.chatSpace
        buttonDisconnet = viewBinding.buttonDisconnect

        SystemClient.setRootView(viewBinding.root)

        // Cuando se pulsa el botón para salir de la conversación
        // Se inicia una corrutina en el hilo de entrada/salida para solicitar la lista de salas de chat al servidor.
        buttonDisconnet.setOnClickListener {
                askChatRoomsCorroutine = lifecycleScope.launch(Dispatchers.IO) {
                    SystemClient.askForChatRoomList()
                }

            //    Se lanza otra corrutina en el hilo principal para realizar diversas acciones después de solicitar la lista de salas de chat.
            //    Se establece la vista principal para el cliente (SystemClient.setRootView).
            //    Se espera a que la corrutina de solicitud de salas de chat termine (askChatRoomsCorroutine!!.join()).
            //    Se espera a que la lista de salas de chat no esté vacía (while (SystemChatRoomList.mutableMap.count() == 0)).
            //    Se hace que el cliente salga de la sala de chat actual (SystemClient.goOutChatRoom()).
            //    Se finaliza la actividad (finish()).

                lifecycleScope.launch(Dispatchers.Main) {
                    SystemClient.setRootView(viewBinding.root)
                    askChatRoomsCorroutine!!.join()
                    withContext(Dispatchers.IO) {
                        while (SystemChatRoomList.mutableMap.count() == 0) {
                        }
                    }
                    SystemClient.goOutChatRoom()
                    finish()
                }
        }
    }
}