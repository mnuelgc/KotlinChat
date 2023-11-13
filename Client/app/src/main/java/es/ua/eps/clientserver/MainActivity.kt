package es.ua.eps.clientserver

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import es.ua.eps.clientserver.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.Socket


// MainActivity se encarga de la configuración inicial y establece un botón de conexión.
// Al hacer clic en ese botón, se valida la entrada del usuario (nombre, dirección IP y puerto),
// se configuran los detalles en un cliente (myClient), se inicia el proceso de conexión al servidor en segundo plano y,
// finalmente, se abre la HomeActivity cuando la conexión se establece con éxito.
class MainActivity : AppCompatActivity() {

    lateinit var viewBinding: ActivityMainBinding

    lateinit var editTextUserName: EditText
    lateinit var editTextAddress: EditText
    lateinit var editTextPort: EditText

    lateinit var buttonConnect: Button
    //lateinit var buttonDisconnet: Button


    lateinit var myClient: Client

    //    Se realiza la inicialización de la actividad, inflando la vista y obteniendo referencias a los elementos de la interfaz de usuario.
    //    Se configuran valores predeterminados en los campos de dirección IP y puerto.
    //    Se crea una instancia de la clase Client llamada myClient.
    //    Se establece un setOnClickListener para el botón "Conectar", que contiene la lógica para conectarse al servidor.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        editTextUserName = viewBinding.userName
        editTextAddress = viewBinding.editTextAdress
        editTextPort = viewBinding.editTextPort

        buttonConnect = viewBinding.buttonConnect

        //editTextAddress.setText("192.168.1.47")
        //editTextAddress.setText("172.20.10.5")
        //editTextPort.setText("8080")

        myClient = Client()

        //    Este bloque de código establece el comportamiento cuando se hace clic en el botón "Conectar".
        //    Se verifica que se hayan ingresado un nombre de usuario, una dirección IP y un puerto.
        //    Se configuran la dirección y el puerto en la instancia de myClient.
        //    Se establece myClient como el cliente en el sistema (SystemClient.setClient(myClient)).
        //    Se inicia un trabajo en segundo plano para conectar el cliente al servidor (SystemClient.connectClientToServer(editTextUserName.text.toString())).
        //    Se utiliza GlobalScope.launch(Dispatchers.IO) para ejecutar un bucle que espera hasta que myClient esté conectado al servidor.
        //    Cuando la conexión se establece, se inicia la actividad HomeActivity.
        buttonConnect.setOnClickListener {
            if (editTextUserName.text.toString() != ""
                && editTextAddress.text.toString() != ""
                && editTextPort.text.toString() != ""
            ) {
                myClient.setAddress(editTextAddress.text.toString())
                myClient.setPort(editTextPort.text.toString().toInt())

                SystemClient.setClient(myClient)
                lifecycleScope.launch(Dispatchers.IO) {
                    SystemClient.connectClientToServer(editTextUserName.text.toString())
                }

                GlobalScope.launch(Dispatchers.IO) {
                    while (true) {
                        if (myClient.isConnectedToServer) {
                            GlobalScope.launch(Dispatchers.Main) {

                                val intentOpenChat =
                                    Intent(this@MainActivity, HomeActivity::class.java)
                                startActivity(intentOpenChat)
                            }
                            break
                        }
                    }
                }
            }
        }
    }
}
