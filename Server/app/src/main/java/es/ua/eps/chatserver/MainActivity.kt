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
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.PrintWriter
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
    private var startServerCorroutine: Job? = null
    private var listenMessagesServerCorroutine: Job? = null

    var count = 0

    var serverRunning = false
    var message : String? = ""

    var clients : ArrayList <Socket> = ArrayList<Socket>()

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
            startServerCorroutine = lifecycleScope.launch(Dispatchers.IO)
            {
                if (isActive) {
                    initSocket()
                }
            }

            if (startServerCorroutine!!.isActive)
            {
                listenMessagesServerCorroutine = lifecycleScope.launch(Dispatchers.IO){
                    if(isActive) {
                        while(true)
                        {
                            withContext(Dispatchers.Main) {
                              //  message += "Listen\n"
                               // serverInfo_text.text = message
                                readMessages()

                            }
                        }
                    }
                }
            }



            //     launchServer()
        }

        stopServer_button.setOnClickListener{
            closeServer()
        }

    }


    protected override fun onDestroy() {
        super.onDestroy()
        closeServer()
    }

    suspend fun initSocket() {
    if (!serverRunning)
    {
        try {
                withContext(Dispatchers.IO) {
                    serverSocket =
                        ServerSocket(SocketServerPORT)

                    withContext(Dispatchers.Main) {
                        serverIp_text.text = getIpAddress()

                        serverPort_text.text = ("I'm waiting here: "
                                + serverSocket.localPort)
                     //  serverInfo_text.text = ""

                    }
                    serverRunning = true
                    count = 0
                    while (true) {
                        val socket = serverSocket!!.accept()

                        if (clients.contains(socket)){
                            message += "USER IN SERVER BEFORE\n"
                        }
                        count++
                        message += """"#$count from ${socket.inetAddress}:${socket.port}"""


                        withContext(Dispatchers.Main) {
                            serverInfo_text.text = message
                        }

                        socketServerReplyThread(socket, count)
                        clients.add(socket)

                        withContext(Dispatchers.Main) {
                            message += clients.count().toString() + "\n"
                            serverInfo_text.text = message

                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    suspend fun socketServerReplyThread (hostThreadSocket: Socket, cnt: Int){
        val outputStream: OutputStream
            val msgReply = "Hello from Pepe, you are #$cnt \n"
            try {
                outputStream = hostThreadSocket.getOutputStream()
                val printWriter = PrintWriter(outputStream)
                printWriter.write(msgReply)
                printWriter.flush()

                message += "replayed: $msgReply\n"


            /*    val byteArrayOutputStream = ByteArrayOutputStream(1024)
                val buffer = ByteArray(1024)
                var bytesRead : Int

               val inputStream = hostThreadSocket.getInputStream()

                while (inputStream.read(buffer).also {bytesRead = it} != -1){
                    byteArrayOutputStream.write(buffer, 0, bytesRead)
                    message += byteArrayOutputStream.toString("UTF-8")

                }
                printStream.close()
                */
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
                message += "Something wrong! $e\n"
            }

            withContext(Dispatchers.Main) {
                serverInfo_text.text = message

            }
        }



    suspend fun readMessages(){
        for(socket in clients) {
            var reader: BufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))

            var line = withContext(Dispatchers.IO) {
                reader.readLine()
            }

            withContext(Dispatchers.Main) {
                serverInfo_text.text = line

            }

        }
        /* withContext(Dispatchers.IO) {
                while(true)
                {
                    for(socket in clients)
                    {
                        val reader = BufferedReader(InputStreamReader(socket.getInputStream(), "UTF-8"))

                        val inputStream = socket!!.getInputStream()
                        val b = readLine()!!
                        socket.getOutputStream().write((b.toByteArray()))
                    }
                }
            }*/
    }

    private fun closeServer()
    {
        if (serverRunning) {
            try {


                serverRunning = false
                startServerCorroutine?.cancel()
                listenMessagesServerCorroutine?.cancel()
                serverSocket.close()
                clients.clear()


                serverPort_text.text = "Server Closed"
                serverIp_text.text = "Server Closed"
                serverInfo_text.text = ""
                message = ""
            } catch (e: IOException) {
                e.printStackTrace()
            }
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