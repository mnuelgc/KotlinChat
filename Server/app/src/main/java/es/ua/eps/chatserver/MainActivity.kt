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

class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var launchServer_button: Button
    private lateinit var stopServer_button: Button
    private lateinit var serverPort_text: TextView
    private lateinit var serverIp_text: TextView
    private lateinit var serverInfo_text: TextView

    private var startServerCorroutine: Job? = null
    private var listenMessagesServerCorroutine: Job? = null

    lateinit var server: Server
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(viewBinding.root)

        launchServer_button = viewBinding.buttonLaunchServer
        stopServer_button = viewBinding.buttonStopServer

        serverPort_text = viewBinding.ServerPort
        serverIp_text = viewBinding.ServerIp
        serverInfo_text = viewBinding.ServerInfo

        server = Server(serverIp_text, serverInfo_text, serverPort_text)

        launchServer_button.setOnClickListener {
            startServerCorroutine = lifecycleScope.launch(Dispatchers.IO)
            {
                if (isActive) {
                    server.initSocket()
                }
            }

            if (startServerCorroutine!!.isActive) {
                ///         listenMessagesServerCorroutine = lifecycleScope.launch(Dispatchers.IO) {
                // delay(2000)
                //  startServerCorroutine!!.join()
                // if (isActive) {
                // while (true) {
                lifecycleScope.launch(Dispatchers.IO) {
                    if (isActive) {
                        startServerCorroutine!!.join()
                        server.waitConnection()
                    }
                }
                lifecycleScope.launch(Dispatchers.IO) {
                    if (isActive) {
                        startServerCorroutine!!.join()
                        server.readMessages()
                    }
                }
            }
        }




    stopServer_button.setOnClickListener{

        startServerCorroutine?.cancel()
        server.closeServer()
        listenMessagesServerCorroutine?.cancel()
    }
}

protected override fun onDestroy() {
    super.onDestroy()
    server.closeServer()
}
}



