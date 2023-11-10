package es.ua.eps.chatserver

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import es.ua.eps.chatserver.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.PrintWriter
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException

// Declaración de la clase MainActivity que extiende AppCompatActivity.
class MainActivity : AppCompatActivity() {

    // Declaración de variables miembro para referenciar elementos de la interfaz de usuario.

    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var launchServer_button: Button
    private lateinit var stopServer_button: Button
    private lateinit var serverPort_text: TextView
    private lateinit var serverIp_text: TextView
    private lateinit var serverInfo_text: TextView

    private var startServerCorroutine: Job? = null
    private var listenMessagesServerCorroutine: Job? = null

    // Declaración de una variable miembro para referenciar un objeto Server.
    lateinit var  server: Server

    // onCreate
    //       Este es el método se llama cuando se crea la actividad.
    //       Aquí se infla el diseño de la actividad utilizando ActivityMainBinding (que está vinculado al archivo de diseño XML)
    //       y se establece como el contenido de la actividad.


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(viewBinding.root)

        // Se obtienen referencias a los elementos de la interfaz de usuario a través del viewBinding para el botón de inicio del servidor,
        // el botón de detención del servidor, y tres TextView para mostrar la información del servidor.

        launchServer_button = viewBinding.buttonLaunchServer
        stopServer_button = viewBinding.buttonStopServer

        serverPort_text = viewBinding.ServerPort
        serverIp_text = viewBinding.ServerIp
        serverInfo_text = viewBinding.ServerInfo

        // Se instancia un objeto de la clase Server y se le pasan las referencias de las TextView para que pueda actualizar la información del servidor
        // en la interfaz de usuario.

        server = Server(serverIp_text, serverInfo_text, serverPort_text)

        // Cuando se hace clic en el botón de iniciar el servidor, se lanza una coroutine utilizando el lifecycleScope.
        // Dentro de esta coroutine, se llama a la función initSocket() del objeto server para iniciar el socket del servidor en un hilo de fondo (Dispatchers.IO).

        launchServer_button.setOnClickListener {
            startServerCorroutine = lifecycleScope.launch(Dispatchers.IO)
            {
                if (isActive) {
                    server.initSocket()
                }
            }

            // Si la coroutine startServerCorroutine está activa (es decir, el servidor se ha iniciado),
            // se lanza otra coroutine para escuchar mensajes en un bucle infinito.
            // Esta coroutine espera a que startServerCorroutine se complete antes de comenzar.
            // Dentro del bucle, se llama a las funciones waitConnection() y readMessages() del objeto server para manejar las conexiones y leer mensajes.

            if (startServerCorroutine!!.isActive) {
                listenMessagesServerCorroutine = lifecycleScope.launch(Dispatchers.IO) {
                   // delay(2000)
                    startServerCorroutine!!.join()
                    if (isActive) {
                        while (true) {
                            server.waitConnection()
                            server.readMessages()
                        }
                    }
                }
            }
        }

        // Cuando se hace clic en el botón de detener el servidor,
        // se cancelan las coroutines startServerCorroutine y listenMessagesServerCorroutine
        // y se llama a la función closeServer() del objeto server para cerrar el servidor.

        stopServer_button.setOnClickListener {

            startServerCorroutine?.cancel()
            server.closeServer()
            listenMessagesServerCorroutine?.cancel()
        }
    }

    // En el método onDestroy, se asegura de cerrar el servidor cuando la actividad se destruye para liberar recursos y evitar posibles fugas de memoria.
    // La función closeServer() del objeto server es llamada nuevamente.
    protected override fun onDestroy() {
        super.onDestroy()
        server.closeServer()
    }
}



