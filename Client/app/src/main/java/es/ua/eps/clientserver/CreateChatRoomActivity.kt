package es.ua.eps.clientserver

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import es.ua.eps.clientserver.databinding.ActivityChatRoomListBinding
import es.ua.eps.clientserver.databinding.ActivityCreateChatRoomBinding
import es.ua.eps.clientserver.databinding.ActivityHomeBinding
import es.ua.eps.clientserver.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// CreateChatRoomActivity se encarga de gestionar la creación de una nueva sala de chat. Permite al usuario ingresar el nombre de la sala,
// Crearla y luego redirigirse a la actividad de conversación.
// La clase CreateChatRoomActivity hereda de AppCompatActivity y representa la actividad donde se crea una nueva sala de chat.
class CreateChatRoomActivity : AppCompatActivity() {

    //    viewBinding: Instancia de la clase ActivityCreateChatRoomBinding utilizada para acceder a las vistas de la actividad.
    //    buttonCreateRoom: Botón que permite al usuario crear una nueva sala de chat.
    //    buttonGoBack: Botón que permite al usuario regresar atrás y cancelar la creación de la sala.
    //    editTextRoomsName: Campo de texto donde el usuario ingresa el nombre de la nueva sala.
    //    createChatCorroutine: Corrutina que se utiliza para crear una nueva sala de chat.

    lateinit var viewBinding: ActivityCreateChatRoomBinding

    lateinit var buttonCreateRoom: Button
    lateinit var buttonGoBack : Button

    lateinit var editTextRoomsName: EditText

    var createChatCorroutine: Job? = null

    // onCreate
    //     Se realiza la inicialización de las vistas y variables en el método onCreate.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityCreateChatRoomBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        buttonCreateRoom = viewBinding.buttonCreateRoom
        buttonGoBack = viewBinding.buttonBack

        editTextRoomsName = viewBinding.editTextRoomsName

        // Este bloque de código define el comportamiento cuando se hace clic en el botón de creación de sala.
        buttonCreateRoom.setOnClickListener {
            // Se inicia una corrutina en el hilo de entrada/salida para crear una nueva sala de chat si el nombre de la sala no está vacío.
            createChatCorroutine = lifecycleScope.launch(Dispatchers.IO) {
                if (editTextRoomsName.text.toString() != "") {
                    SystemClient.createChatRoom(editTextRoomsName.text.toString())
                }
            }
            //    Se lanza otra corrutina en el hilo principal para realizar acciones después de crear la sala de chat.
            //    Se espera a que la corrutina de creación de sala de chat termine (createChatCorroutine!!.join()).
            //    Se crea un intent para abrir la actividad de conversación (Intent(this@CreateChatRoomActivity, ConversationActivity::class.java)).
            //    Se inicia la actividad de conversación (startActivity(intentOpenChat)).
            lifecycleScope.launch(Dispatchers.Main) {
                createChatCorroutine!!.join()
                val intentOpenChat = Intent(this@CreateChatRoomActivity, ConversationActivity::class.java)
                startActivity(intentOpenChat)
            }
        }

        // Este bloque de código cierra la actividad actual cuando se hace clic en el botón de regreso.
        buttonGoBack.setOnClickListener{
            finish()
        }

    }
}