package es.ua.eps.chatserver

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import es.ua.eps.chatserver.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.io.PrintStream
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException

class MainActivity : AppCompatActivity() {
    val SocketServerPORT = 8080

    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var launchServer_button: Button
    private lateinit var stopServer_button: Button
    private lateinit var serverPort_text: TextView
    private lateinit var serverIp_text: TextView
    private lateinit var serverInfo_text: TextView

    private lateinit var serverSocket: ServerSocket
    private var serverCorroutine: Job? = null

    var count = 0

    var message : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(viewBinding.root)

        launchServer_button = viewBinding.buttonLaunchServer
        stopServer_button = viewBinding.buttonStopServer

        serverPort_text = viewBinding.ServerPort
        serverIp_text = viewBinding.ServerIp
        serverInfo_text = viewBinding.ServerInfo


        /* socketServerThread = Thread(SocketServerThread)
        socketServerThread.start()
*/


      /*  serverCorroutine = lifecycleScope.launch(Dispatchers.IO)
        {
            if (isActive) {
                initSocket()
            }
               lifecycleScope.launch(Dispatchers.Main)
                {
                    serverInfo_text.text = ("aaaaa " )
                }

        }
        */

        launchServer_button.setOnClickListener {
            serverCorroutine = lifecycleScope.launch(Dispatchers.IO)
            {
                if (isActive) {
                    initSocket()
                }
                /*    lifecycleScope.launch(Dispatchers.Main)
                    {
                        serverInfo_text.text = ("aaaaa " )
                    }
        */
            }
            //     launchServer()
        }

    }


    protected override fun onDestroy() {
        super.onDestroy()
        try {
            serverCorroutine?.cancel()
            serverSocket.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    suspend fun initSocket() {
        try {
            withContext(Dispatchers.IO) {
                serverSocket =
                    ServerSocket(SocketServerPORT)

                withContext(Dispatchers.Main) {
                    serverIp_text.text = getIpAddress()

                    serverPort_text.text = ("I'm waiting here: "
                            + serverSocket.localPort)
                }
                count = 0
                while (true){
                    val socket = serverSocket!!.accept()
                    count++
                    message += """"#$count from ${socket.inetAddress}:${socket.port}"""


                    withContext(Dispatchers.Main) {
                        serverInfo_text.text = message
                    }
                    //val socketServerReply

                    val socketServerReplyThread: SocketServerReplyThread = SocketServerReplyThread(
                        socket, count
                    )
                    socketServerReplyThread.run()

                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private inner class SocketServerReplyThread internal constructor(
        private val hostThreadSocket: Socket,
        var cnt: Int
    ) :
        Thread() {
        override fun run() {
            val outputStream: OutputStream
            val msgReply = "Hello from Pepe, you are #$cnt"
            try {
                outputStream = hostThreadSocket.getOutputStream()
                val printStream = PrintStream(outputStream)
                printStream.print(msgReply)
                printStream.close()
                message += "replayed: $msgReply\n"
                runOnUiThread { serverInfo_text.text = message }
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
                message += "Something wrong! $e\n"
            }
            runOnUiThread { serverInfo_text.text = message }
        }
    }


    private fun getIpAddress() : String {
        var ip = ""

        try{
            val enumNetworkInterface = NetworkInterface.getNetworkInterfaces()
            while (enumNetworkInterface.hasMoreElements()){
                val networkInterface = enumNetworkInterface.nextElement()
                val enumInetAddress = networkInterface.inetAddresses
                while (enumInetAddress.hasMoreElements()){
                    val inetAddress = enumInetAddress.nextElement()

                    if (inetAddress.isSiteLocalAddress) {
                        ip += "SiteLocalAddress: " + inetAddress.hostAddress + "\n"
                    }
                }
            }

        }catch(se : SocketException){
            se.printStackTrace()
            ip += "Something Wrong! " + se.toString() + "\n"
        }

        return ip
    }
}

/*
    private fun launchServer(){

    }


  /*  class SocketServerThread : Thread() {
        val SocketServerPORT = 8000
        var count = 0
        override fun run() {
        }
    }
*/

    private fun getIpAdress() : String {
        var ip = ""

        try{
            val enumNetworkInterface = NetworkInterface.getNetworkInterfaces()
            while (enumNetworkInterface.hasMoreElements()){
                val networkInterface = enumNetworkInterface.nextElement()
                val enumInetAddress = networkInterface.inetAddresses
                while (enumInetAddress.hasMoreElements()){
                    val inetAddress = enumInetAddress.nextElement()

                    if (inetAddress.isSiteLocalAddress) {
                        ip += "SiteLocalAddress: " + inetAddress.hostAddress + "\n"
                    }
                }
            }

        }catch(se : SocketException){
            se.printStackTrace()
            ip += "Something Wrong! " + se.toString() + "\n"
        }

        return ip
    }
}

 */